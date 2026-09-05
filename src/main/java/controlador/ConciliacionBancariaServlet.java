/*
 * Conciliacion bancaria (modulo Tesoreria, §F del plan y CONCILIACION_BANCARIA_PLAN.md).
 * Patron Session+Token calcado de FondoFijoRendicionServlet: la conciliacion en edicion vive en la
 * sesion bajo una clave con token, porque la grilla de movimientos se sostiene entre pedidos.
 * Primero se elige la cuenta y el periodo, y con eso se arma la grilla; despues se tilda contra el
 * extracto y se graba.
 */
package controlador;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import modelo.ConciliacionBancaria;
import modelo.ConciliacionBancariaDAO;
import modelo.ConciliacionBancariaDetalle;
import modelo.Cuenta;
import modelo.EntidadFinanciera;
import modelo.Usuario;
import service.ConciliacionBancariaService;
import service.CuentaService;

@WebServlet(name = "ConciliacionBancariaServlet", urlPatterns = {"/ConciliacionBancariaServlet"})
public class ConciliacionBancariaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ConciliacionBancariaServlet.class.getName());
    private static final String JSP_CONCILIACION = "conciliacionBancaria.jsp";
    private static final String SESSION_PREFIX = "conciliacion_";
    private static final String MENU = "ConciliacionBancaria";
    private static final String FORMATO_FECHA = "yyyy-MM-dd";

    private final ConciliacionBancariaService conciliacionService = new ConciliacionBancariaService();
    private final CuentaService cuentaService = new CuentaService();

    /** Documento en edicion. Serializable porque vive en la HttpSession. */
    private static class ConciliacionState implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        ConciliacionBancaria conciliacion = new ConciliacionBancaria();
        List<ConciliacionBancariaDetalle> listaDetalle = new ArrayList<>();

        boolean esNuevo = false;
        Long idConciliacionExistente;
        /** Con la grilla armada la cuenta y el periodo quedan fijos: cambiarlos la invalidaria. */
        boolean movimientosCargados = false;

        List<Cuenta> listaCuentas;
        List<EntidadFinanciera> listaEntidades;
        List<ConciliacionBancaria> listaConciliaciones;
    }

    // ==================== HELPERS DE SESIÓN ====================

    private String generarToken() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private ConciliacionState obtenerEstado(HttpSession session, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return (ConciliacionState) session.getAttribute(SESSION_PREFIX + token);
    }

    private void guardarEstado(HttpSession session, String token, ConciliacionState estado) {
        session.setAttribute(SESSION_PREFIX + token, estado);
    }

    private void limpiarEstado(HttpSession session, String token) {
        if (token != null) {
            session.removeAttribute(SESSION_PREFIX + token);
        }
    }

    private ConciliacionState obtenerEstadoORedireccionar(HttpServletRequest request,
            HttpServletResponse response, HttpSession session, String token) throws IOException {
        ConciliacionState estado = obtenerEstado(session, token);
        if (estado == null) {
            mostrarMensaje(request, "Sesión expirada, inicie de nuevo", "alert-warning");
            response.sendRedirect("ConciliacionBancariaServlet?menu=" + MENU + "&accion=ListarModal");
        }
        return estado;
    }

    // ==================== PROCESO PRINCIPAL ====================

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String menu = request.getParameter("menu");
        String accion = request.getParameter("accion");
        String token = request.getParameter("token");

        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        if (!MENU.equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }
        if (accion == null) {
            accion = "ListarModal";
        }

        Boolean puedeInsertar = (Boolean) request.getAttribute("puedeInsertar");
        Boolean puedeBorrar = (Boolean) request.getAttribute("puedeBorrar");

        try {
            switch (accion) {
                case "Nuevo":
                case "CargarMovimientos":
                case "Grabar":
                    if (!Boolean.TRUE.equals(puedeInsertar)) {
                        mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                        accionListarModal(request, response);
                        return;
                    }
                    break;
                case "Anular":
                    if (!Boolean.TRUE.equals(puedeBorrar)) {
                        mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                        accionListarModal(request, response);
                        return;
                    }
                    break;
                default:
                    break;
            }

            switch (accion) {
                case "Nuevo":               accionNuevo(request, response, session); break;
                case "CargarMovimientos":   accionCargarMovimientos(request, response, session, token); break;
                case "Grabar":              accionGrabar(request, response, session, token); break;
                case "CargarConciliacion":  accionCargarConciliacion(request, response, session); break;
                case "Anular":              accionAnular(request, response, session, token); break;
                case "Cancelar":            accionCancelar(request, response, session, token); break;
                case "ListarModal":
                default:                    accionListarModal(request, response); break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en ConciliacionBancariaServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            accionListarModal(request, response);
        }
    }

    // ==================== ACCIONES ====================

    /** Pantalla inerte: sin conciliación abierta solo se puede usar Nuevo y Buscar Conciliación. */
    private void accionListarModal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("listaConciliaciones", conciliacionService.listarConciliaciones());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar conciliaciones", e);
        }
        forward(request, response, JSP_CONCILIACION);
    }

    private void accionNuevo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws ServletException, IOException, SQLException {

        String token = generarToken();
        ConciliacionState estado = new ConciliacionState();
        estado.esNuevo = true;
        estado.conciliacion.setFecha(new Date());
        estado.conciliacion.setEstado(ConciliacionBancariaDAO.ESTADO_VIGENTE);
        cargarListas(estado);

        volverAVista(request, response, session, estado, token);
    }

    /**
     * Arma la grilla con los movimientos de la cuenta y el periodo elegidos. A partir de aca la
     * cabecera queda fija: cambiar la cuenta o el periodo cambiaria los movimientos, asi que para
     * eso se cancela y se empieza de nuevo.
     */
    private void accionCargarMovimientos(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        ConciliacionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (estado.movimientosCargados) {
            mostrarMensaje(request, "Los movimientos ya están cargados", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        Long idCuenta = leerId(request.getParameter("idCuenta"));
        if (idCuenta == null) {
            mostrarMensaje(request, "Debe seleccionar la cuenta bancaria", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        Cuenta cuenta = cuentaService.getCuenta(idCuenta);
        if (cuenta == null) {
            mostrarMensaje(request, "No se pudo cargar la cuenta bancaria", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        Date hasta = leerFecha(request.getParameter("fechaHasta"));
        if (hasta == null) {
            mostrarMensaje(request, "Debe indicar la fecha hasta del período", "alert-warning");
            estado.conciliacion.setCuenta(cuenta);
            volverAVista(request, response, session, estado, token);
            return;
        }

        // El desde no se elige salvo en la primera conciliacion de la cuenta: despues es el dia
        // siguiente al cierre de la anterior, porque el saldo se encadena.
        Date desde = conciliacionService.fechaDesdeEsperada(idCuenta);
        if (desde == null) {
            desde = leerFecha(request.getParameter("fechaDesde"));
            if (desde == null) {
                mostrarMensaje(request, "Debe indicar la fecha desde del período", "alert-warning");
                estado.conciliacion.setCuenta(cuenta);
                volverAVista(request, response, session, estado, token);
                return;
            }
        }
        if (desde.after(hasta)) {
            mostrarMensaje(request, "La fecha desde no puede ser posterior a la fecha hasta", "alert-warning");
            estado.conciliacion.setCuenta(cuenta);
            volverAVista(request, response, session, estado, token);
            return;
        }

        estado.conciliacion.setCuenta(cuenta);
        estado.conciliacion.setFechaDesde(desde);
        estado.conciliacion.setFechaHasta(hasta);
        estado.conciliacion.setSaldoInicial(conciliacionService.obtenerSaldoInicial(idCuenta));
        estado.listaDetalle = conciliacionService.listarMovimientosAConciliar(idCuenta, hasta);
        if (estado.listaDetalle == null) {
            estado.listaDetalle = new ArrayList<>();
        }
        estado.movimientosCargados = true;

        if (estado.listaDetalle.isEmpty()) {
            mostrarMensaje(request, "No hay movimientos pendientes para esa cuenta y período", "alert-warning");
        }
        volverAVista(request, response, session, estado, token);
    }

    private void accionGrabar(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        ConciliacionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (!estado.esNuevo) {
            mostrarMensaje(request, "La conciliación ya fue grabada", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        if (!estado.movimientosCargados) {
            mostrarMensaje(request, "Primero cargue los movimientos del período", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        if (!leerDatosFormulario(request, estado)) {
            volverAVista(request, response, session, estado, token);
            return;
        }

        try {
            Long id = conciliacionService.guardarConciliacionCompleta(
                    estado.conciliacion, estado.listaDetalle);
            limpiarEstado(session, token);
            mostrarMensaje(request, "Conciliación grabada correctamente (id " + id + ")", "alert-success");
            accionListarModal(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Conciliación rechazada: {0}", e.getMessage());
            mostrarMensaje(request, e.getMessage(), "alert-warning");
            volverAVista(request, response, session, estado, token);
        }
    }

    /** Trae una conciliación ya grabada para verla o anularla. */
    private void accionCargarConciliacion(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws ServletException, IOException, SQLException {

        Long id = leerId(request.getParameter("id"));
        if (id == null) {
            mostrarMensaje(request, "Debe seleccionar una conciliación", "alert-warning");
            accionListarModal(request, response);
            return;
        }
        ConciliacionBancaria conciliacion = conciliacionService.getConciliacion(id);
        if (conciliacion == null) {
            mostrarMensaje(request, "No se pudo cargar la conciliación", "alert-warning");
            accionListarModal(request, response);
            return;
        }

        String token = generarToken();
        ConciliacionState estado = new ConciliacionState();
        estado.esNuevo = false;
        estado.conciliacion = conciliacion;
        estado.idConciliacionExistente = id;
        estado.movimientosCargados = true;
        estado.listaDetalle = conciliacionService.listarDetalles(id);
        if (estado.listaDetalle == null) {
            estado.listaDetalle = new ArrayList<>();
        }
        cargarListas(estado);

        volverAVista(request, response, session, estado, token);
    }

    private void accionAnular(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        ConciliacionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (estado.idConciliacionExistente == null) {
            mostrarMensaje(request, "Cargue una conciliación para anularla", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        try {
            conciliacionService.anularConciliacionCompleta(estado.idConciliacionExistente);
            limpiarEstado(session, token);
            mostrarMensaje(request, "Conciliación anulada correctamente", "alert-success");
            accionListarModal(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Anulación rechazada: {0}", e.getMessage());
            mostrarMensaje(request, e.getMessage(), "alert-warning");
            volverAVista(request, response, session, estado, token);
        }
    }

    private void accionCancelar(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {
        limpiarEstado(session, token);
        accionListarModal(request, response);
    }

    // ==================== FORMULARIO ====================

    /**
     * Lee lo que el usuario cargo en la pantalla: el saldo del extracto y los tildes de la grilla.
     * Los tildes vienen como checkbox por posicion —los destildados no se mandan— asi que se
     * recorre la lista entera y no los parametros.
     */
    private boolean leerDatosFormulario(HttpServletRequest request, ConciliacionState estado) {
        String saldoBancoStr = request.getParameter("saldoBanco");
        if (saldoBancoStr == null || saldoBancoStr.trim().isEmpty()) {
            mostrarMensaje(request, "Debe cargar el saldo del extracto bancario", "alert-warning");
            return false;
        }
        if (!saldoBancoStr.trim().matches("-?\\d+")) {
            mostrarMensaje(request, "El saldo del extracto debe ser numérico", "alert-danger");
            return false;
        }
        estado.conciliacion.setSaldoBanco(Long.valueOf(saldoBancoStr.trim()));

        for (int i = 0; i < estado.listaDetalle.size(); i++) {
            boolean tildado = request.getParameter("conciliado_" + i) != null;
            estado.listaDetalle.get(i).setConciliado(tildado);
        }
        return true;
    }

    // ==================== HELPERS ====================

    private void cargarListas(ConciliacionState estado) throws SQLException {
        estado.listaCuentas = cuentaService.listarCuenta();
        estado.listaEntidades = bancosConCuenta(estado.listaCuentas);
        estado.listaConciliaciones = conciliacionService.listarConciliaciones();
    }

    /**
     * Bancos que aparecen en alguna cuenta registrada, sin repetir. Igual que en movimientos: el
     * combo de banco sirve para llegar a una cuenta, no es el referencial de entidades.
     */
    private List<EntidadFinanciera> bancosConCuenta(List<Cuenta> cuentas) {
        Map<Long, EntidadFinanciera> bancos = new LinkedHashMap<>();
        if (cuentas != null) {
            for (Cuenta cuenta : cuentas) {
                EntidadFinanciera banco = cuenta.getEntidadFinanciera();
                if (banco != null) {
                    bancos.put(banco.getIdEntidadFinanciera(), banco);
                }
            }
        }
        return new ArrayList<>(bancos.values());
    }

    private void volverAVista(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ConciliacionState estado, String token)
            throws ServletException, IOException {

        guardarEstado(session, token, estado);
        request.setAttribute("token", token);
        request.setAttribute("conciliacion", estado.conciliacion);
        request.setAttribute("listaDetalle", estado.listaDetalle);
        request.setAttribute("esNuevo", estado.esNuevo);
        request.setAttribute("idConciliacionExistente", estado.idConciliacionExistente);
        request.setAttribute("movimientosCargados", estado.movimientosCargados);
        request.setAttribute("listaCuentas", estado.listaCuentas);
        request.setAttribute("listaEntidades", estado.listaEntidades);
        request.setAttribute("listaConciliaciones", estado.listaConciliaciones);

        // El desde lo fija el encadenado salvo en la primera conciliacion de la cuenta.
        try {
            if (estado.esNuevo && estado.conciliacion.getCuenta() != null) {
                request.setAttribute("fechaDesdeFija", conciliacionService.fechaDesdeEsperada(
                        estado.conciliacion.getCuenta().getIdCuenta()));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se pudo calcular la fecha desde esperada", e);
        }

        Long saldoInicial = estado.conciliacion.getSaldoInicial();
        request.setAttribute("saldoLibro", ConciliacionBancariaService.calcularSaldoLibro(
                saldoInicial == null ? 0L : saldoInicial, estado.listaDetalle,
                estado.conciliacion.getFechaDesde(), estado.conciliacion.getFechaHasta()));
        request.setAttribute("saldoAjustado", ConciliacionBancariaService.calcularSaldoAjustado(
                estado.conciliacion.getSaldoBanco() == null ? 0L : estado.conciliacion.getSaldoBanco(),
                estado.listaDetalle));
        request.setAttribute("diferencia", ConciliacionBancariaService.calcularDiferencia(
                estado.conciliacion, estado.listaDetalle));

        forward(request, response, JSP_CONCILIACION);
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

    private Date leerFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(FORMATO_FECHA).parse(fechaStr.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private void mostrarMensaje(HttpServletRequest request, String mensaje, String tipoAlert) {
        request.setAttribute("Message", mensaje);
        request.setAttribute("tipoAlert", tipoAlert);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        request.getRequestDispatcher(vista).forward(request, response);
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
