/*
 * Carga de otros débitos y créditos bancarios (módulo Tesorería, §D del plan).
 * Un solo servlet para los dos: es el mismo formulario y la única diferencia es la tabla donde cae
 * el movimiento, igual que NotaCreditoDebitoServlet con las notas. El tipo viaja por request.
 * Servlet sin estado en variables de instancia (thread-safe): no hay carrito que sostener entre
 * pedidos, el movimiento es una sola fila, así que tampoco hace falta el token de sesión.
 */
package controlador;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import modelo.Credito;
import modelo.Cuenta;
import modelo.Debito;
import modelo.MovimientoBancario;
import modelo.Usuario;
import service.CreditoService;
import service.CuentaService;
import service.DebitoService;
import service.EntidadFinancieraService;

@WebServlet(name = "MovimientoBancarioServlet",
        urlPatterns = {"/MovimientoBancarioServlet", "/DebitoServlet", "/CreditoServlet"})
public class MovimientoBancarioServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MovimientoBancarioServlet.class.getName());
    private static final String JSP_MOVIMIENTO = "movimientoBancario.jsp";

    private static final String TIPO_DEBITO = "debito";
    private static final String TIPO_CREDITO = "credito";

    private final DebitoService debitoService = new DebitoService();
    private final CreditoService creditoService = new CreditoService();
    private final CuentaService cuentaService = new CuentaService();
    private final EntidadFinancieraService entidadFinancieraService = new EntidadFinancieraService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String menu = request.getParameter("menu");
        String accion = request.getParameter("accion");
        // Cada tipo entra por su propia URL: dos entradas de menu al mismo servlet se pintan las
        // dos como activas, porque el resaltado compara el nombre del servlet.
        String tipo = tipoDesdeRuta(request.getServletPath());
        if (tipo == null) {
            tipo = request.getParameter("tipo");
        }

        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        if (!"MovimientoBancario".equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }
        if (!TIPO_CREDITO.equals(tipo)) {
            tipo = TIPO_DEBITO;
        }
        if (accion == null) {
            accion = "Listar";
        }

        Boolean puedeInsertar = (Boolean) request.getAttribute("puedeInsertar");
        Boolean puedeBorrar = (Boolean) request.getAttribute("puedeBorrar");

        try {
            switch (accion) {
                case "Nuevo":
                    mostrarVista(request, response, tipo, null, true);
                    break;
                case "Cargar":
                    accionCargar(request, response, tipo);
                    break;
                case "Generar":
                    if (!Boolean.TRUE.equals(puedeInsertar)) {
                        sinPermiso(request);
                        mostrarVista(request, response, tipo, null, true);
                    } else {
                        accionGenerar(request, response, tipo);
                    }
                    break;
                case "Anular":
                    if (!Boolean.TRUE.equals(puedeBorrar)) {
                        sinPermiso(request);
                        mostrarVista(request, response, tipo, null, false);
                    } else {
                        accionAnular(request, response, tipo);
                    }
                    break;
                case "Cancelar":
                case "Listar":
                default:
                    mostrarVista(request, response, tipo, null, false);
                    break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en MovimientoBancarioServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            mostrarVista(request, response, tipo, null, false);
        }
    }

    // ==================== ACCIONES ====================

    /** Trae un movimiento ya generado para verlo o anularlo. */
    private void accionCargar(HttpServletRequest request, HttpServletResponse response, String tipo)
            throws ServletException, IOException, SQLException {

        Long id = leerId(request.getParameter("id"));
        if (id == null) {
            mostrarMensaje(request, "Debe seleccionar un movimiento", "alert-warning");
            mostrarVista(request, response, tipo, null, false);
            return;
        }
        MovimientoBancario mov = buscar(tipo, id);
        if (mov == null) {
            mostrarMensaje(request, "No se pudo cargar el movimiento", "alert-warning");
        }
        mostrarVista(request, response, tipo, mov, false);
    }

    private void accionGenerar(HttpServletRequest request, HttpServletResponse response, String tipo)
            throws ServletException, IOException, SQLException {

        MovimientoBancario mov = leerMovimientoFormulario(request, tipo);
        if (mov == null) {
            // El mensaje de validación ya fue seteado. La vista se rearma con lo que vino en el
            // request, así que el usuario no pierde lo que ya había cargado.
            mostrarVista(request, response, tipo, null, true);
            return;
        }
        if (TIPO_CREDITO.equals(tipo)) {
            creditoService.insertarCredito((Credito) mov);
        } else {
            debitoService.insertarDebito((Debito) mov);
        }
        mostrarMensaje(request, etiqueta(tipo) + " N° " + mov.getId() + " generado correctamente", "alert-success");
        mostrarVista(request, response, tipo, mov, false);
    }

    private void accionAnular(HttpServletRequest request, HttpServletResponse response, String tipo)
            throws ServletException, IOException, SQLException {

        Long id = leerId(request.getParameter("id"));
        if (id == null) {
            mostrarMensaje(request, "Debe cargar un movimiento para anularlo", "alert-warning");
            mostrarVista(request, response, tipo, null, false);
            return;
        }
        try {
            if (TIPO_CREDITO.equals(tipo)) {
                creditoService.anularCredito(id);
            } else {
                debitoService.anularDebito(id);
            }
            mostrarMensaje(request, etiqueta(tipo) + " anulado correctamente", "alert-success");
        } catch (SQLException e) {
            // El Service rechaza anular dos veces o sobre un id inexistente: es un aviso, no un error.
            LOGGER.log(Level.WARNING, "Anulación rechazada: {0}", e.getMessage());
            mostrarMensaje(request, e.getMessage(), "alert-warning");
        }
        mostrarVista(request, response, tipo, buscar(tipo, id), false);
    }

    // ==================== HELPERS ====================

    /**
     * Lee y valida los campos del formulario. Devuelve null si algo falta (y setea el mensaje).
     * El tipo de cambio es opcional: sólo tiene sentido cuando la cuenta es en moneda extranjera.
     */
    private MovimientoBancario leerMovimientoFormulario(HttpServletRequest request, String tipo) {
        String idCuentaStr = request.getParameter("idCuenta");
        String comprobanteStr = request.getParameter("comprobante");
        String fechaStr = request.getParameter("fecha");
        String detalle = request.getParameter("detalle");
        String importeStr = request.getParameter("importe");
        String tipoCambioStr = request.getParameter("tipoCambio");

        if (idCuentaStr == null || idCuentaStr.isEmpty()
                || comprobanteStr == null || comprobanteStr.trim().isEmpty()
                || fechaStr == null || fechaStr.trim().isEmpty()
                || detalle == null || detalle.trim().isEmpty()
                || importeStr == null || importeStr.trim().isEmpty()) {
            mostrarMensaje(request, "Complete todos los campos del movimiento", "alert-warning");
            return null;
        }
        if (!comprobanteStr.trim().matches("\\d+") || !importeStr.trim().matches("\\d+")) {
            mostrarMensaje(request, "El comprobante y el importe deben ser numéricos", "alert-danger");
            return null;
        }
        long importe = Long.parseLong(importeStr.trim());
        if (importe <= 0) {
            mostrarMensaje(request, "El importe debe ser mayor a cero", "alert-danger");
            return null;
        }
        if (detalle.trim().length() > 255) {
            mostrarMensaje(request, "El detalle no puede superar los 255 caracteres", "alert-danger");
            return null;
        }

        Date fecha;
        try {
            fecha = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr.trim());
        } catch (ParseException e) {
            mostrarMensaje(request, "La fecha de emisión no es válida", "alert-danger");
            return null;
        }

        Double tipoCambio = null;
        if (tipoCambioStr != null && !tipoCambioStr.trim().isEmpty()) {
            try {
                tipoCambio = Double.valueOf(tipoCambioStr.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                mostrarMensaje(request, "El tipo de cambio no es válido", "alert-danger");
                return null;
            }
            if (tipoCambio <= 0) {
                mostrarMensaje(request, "El tipo de cambio debe ser mayor a cero", "alert-danger");
                return null;
            }
        }

        Cuenta cuenta = new Cuenta(Long.parseLong(idCuentaStr));
        long comprobante = Long.parseLong(comprobanteStr.trim());

        if (TIPO_CREDITO.equals(tipo)) {
            Credito credito = new Credito();
            credito.setNumeroComprobante(comprobante);
            credito.setFecha(fecha);
            credito.setDetalle(detalle.trim());
            credito.setCuenta(cuenta);
            credito.setMonto(importe);
            credito.setTipoCambio(tipoCambio);
            // id_cobro queda vacío: el enlace con cobros llega recién con Ventas.
            return credito;
        }
        Debito debito = new Debito();
        debito.setNumeroComprobante(comprobante);
        debito.setFecha(fecha);
        debito.setDetalle(detalle.trim());
        debito.setCuenta(cuenta);
        debito.setMonto(importe);
        debito.setTipoCambio(tipoCambio);
        return debito;
    }

    private MovimientoBancario buscar(String tipo, Long id) throws SQLException {
        return TIPO_CREDITO.equals(tipo) ? creditoService.getCredito(id) : debitoService.getDebito(id);
    }

    /**
     * Deja la vista lista: el tipo y sus textos, el movimiento en pantalla (o ninguno), si el
     * formulario está habilitado para cargar, y los combos y el listado del buscador.
     */
    private void mostrarVista(HttpServletRequest request, HttpServletResponse response,
            String tipo, MovimientoBancario mov, boolean esNuevo) throws ServletException, IOException {

        request.setAttribute("tipo", tipo);
        request.setAttribute("ruta", ruta(tipo));
        request.setAttribute("etiqueta", etiqueta(tipo));
        request.setAttribute("titulo", TIPO_CREDITO.equals(tipo) ? "CARGAR CRÉDITOS" : "CARGAR DÉBITOS");
        request.setAttribute("movimiento", mov);
        request.setAttribute("esNuevo", esNuevo);
        // Las dos entradas del menu apuntan a este servlet: sin esto no hay forma de saber cual
        // pintar como activa, porque la URL despues de un POST no trae el tipo.
        request.setAttribute("menuActivo", TIPO_CREDITO.equals(tipo) ? "movimientoCredito" : "movimientoDebito");

        try {
            request.setAttribute("listaCuentas", cuentaService.listarCuenta());
            request.setAttribute("listaEntidades", entidadFinancieraService.listarEntidadFinanciera());
            request.setAttribute("listaMovimientos", TIPO_CREDITO.equals(tipo)
                    ? creditoService.listarCreditos() : debitoService.listarDebitos());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar los datos de la vista", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
        }
        request.getRequestDispatcher(JSP_MOVIMIENTO).forward(request, response);
    }

    private String etiqueta(String tipo) {
        return TIPO_CREDITO.equals(tipo) ? "Crédito" : "Débito";
    }

    /** URL propia de cada tipo; null si se entro por la generica. */
    private String tipoDesdeRuta(String servletPath) {
        if ("/CreditoServlet".equals(servletPath)) {
            return TIPO_CREDITO;
        }
        if ("/DebitoServlet".equals(servletPath)) {
            return TIPO_DEBITO;
        }
        return null;
    }

    private String ruta(String tipo) {
        return TIPO_CREDITO.equals(tipo) ? "CreditoServlet" : "DebitoServlet";
    }

    private Long leerId(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(idStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void sinPermiso(HttpServletRequest request) {
        mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
    }

    private void mostrarMensaje(HttpServletRequest request, String mensaje, String tipoAlert) {
        request.setAttribute("Message", mensaje);
        request.setAttribute("tipoAlert", tipoAlert);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
