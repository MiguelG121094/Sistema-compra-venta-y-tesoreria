/*
 * OrdenPagoServlet - Implementación con patrón Session + Token
 *
 * Orden de Pago (Tesorería). Parte SIEMPRE de una provisión de cuenta a pagar en estado
 * 'Pendiente': trae sus facturas como detalle (solo lectura) y se le cargan N formas de pago
 * (transferencia + cheque(s), multi-cuenta) hasta igualar el total. Al Generar, OrdenPagoService
 * descuenta el saldo de las cuentas a pagar, emite los cheques reales de la chequera y marca la
 * provisión como 'Procesada' en una sola transacción. Ver MODULO_TESORERIA_PLAN.md §C.
 *
 * Estructura: Switch-Case con Métodos Delegados (calcado de FacturaCompraServlet).
 * Vista: ordenPago.jsp (formulario único + JS; el contrato de atributos está en su encabezado).
 */
package controlador;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import modelo.*;
import service.*;

@WebServlet(name = "OrdenPagoServlet", urlPatterns = {"/OrdenPagoServlet"})
public class OrdenPagoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(OrdenPagoServlet.class.getName());
    private static final String SESSION_PREFIX = "ordenPago_";
    private static final String JSP_ORDEN_PAGO = "ordenPago.jsp";

    // Estado con el que nace una OP generada: pendiente de conciliación bancaria.
    private static final String ESTADO_OP_PENDIENTE = "Pendiente";
    private static final String ESTADO_PROV_PENDIENTE = "Pendiente";

    // Services (stateless, pueden ser de instancia)
    private final OrdenPagoService ordenPagoService = new OrdenPagoService();
    private final ProvisionCuentaPagarService provisionService = new ProvisionCuentaPagarService();
    private final SucursalService sucursalService = new SucursalService();
    private final CuentaService cuentaService = new CuentaService();
    private final FormaPagoCabeceraService formaPagoCabeceraService = new FormaPagoCabeceraService();
    private final ChequeraService chequeraService = new ChequeraService();
    private final TipoChequeService tipoChequeService = new TipoChequeService();

    // ==================== CLASE DE ESTADO ====================

    /**
     * Encapsula todo el estado de trabajo de una orden de pago. Se guarda en sesión con un token
     * único (una entrada por pestaña/documento). Serializable para permitir persistencia de sesión.
     */
    private static class OrdenPagoState implements Serializable {
        private static final long serialVersionUID = 1L;

        OrdenPago ordenPago = new OrdenPago();
        List<OrdenPagoDetalle> listaDetalle = new ArrayList<>();   // facturas de la provisión (solo lectura)
        List<FormaPagoDetalle> listaFormasPago = new ArrayList<>(); // carrito de formas de pago
        Sucursal sucursalSeleccionada;

        // Editor del modal de forma de pago (se repuebla solo si hubo error de validación)
        FormaPagoDetalle formaEnEditor;

        boolean esNuevo = false;
        Long idOrdenPagoExistente;   // seteado al cargar una OP guardada (para ver/anular)

        // Datos para modales y combos
        List<Sucursal> listaSucursales;
        List<Cuenta> listaCuentas;
        List<FormaPagoCabecera> listaFormaPago;
        List<Chequera> listaChequeras;
        List<TipoCheque> listaTipoCheque;
        List<OrdenPago> listaOrdenesPago;
        List<ProvisionCuentaPagar> listaProvisiones;
    }

    // ==================== MÉTODOS HELPER PARA SESIÓN ====================

    private String generarToken() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private OrdenPagoState obtenerEstado(HttpSession session, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return (OrdenPagoState) session.getAttribute(SESSION_PREFIX + token);
    }

    private void guardarEstado(HttpSession session, String token, OrdenPagoState estado) {
        session.setAttribute(SESSION_PREFIX + token, estado);
    }

    private void limpiarEstado(HttpSession session, String token) {
        if (token != null) {
            session.removeAttribute(SESSION_PREFIX + token);
        }
    }

    /**
     * Obtiene el estado de la sesión o redirige si expiró
     * @return null si la sesión expiró (ya se hizo redirect)
     */
    private OrdenPagoState obtenerEstadoORedireccionar(HttpServletRequest request,
            HttpServletResponse response, HttpSession session, String token) throws IOException {

        OrdenPagoState estado = obtenerEstado(session, token);
        if (estado == null) {
            mostrarMensaje(request, "Sesión expirada, inicie de nuevo", "alert-warning");
            response.sendRedirect("OrdenPagoServlet?menu=OrdenPago&accion=ListarModal");
        }
        return estado;
    }

    /** Carga los combos y las listas de los modales en el estado (una vez por documento). */
    private void cargarListas(OrdenPagoState estado) throws SQLException {
        estado.listaSucursales = sucursalService.listarSucursles();
        estado.listaCuentas = cuentaService.listarCuenta();
        estado.listaFormaPago = formaPagoCabeceraService.listarFormaPago();
        estado.listaChequeras = chequeraService.listarChequeras();
        estado.listaTipoCheque = tipoChequeService.listarTipoCheque();
        estado.listaOrdenesPago = ordenPagoService.listarOrdenesPago();
        estado.listaProvisiones = provisionService.listarProvisionesPendientes();
    }

    private void cargarDatosParaVista(HttpServletRequest request, OrdenPagoState estado, String token) {
        request.setAttribute("token", token);
        request.setAttribute("ordenPago", estado.ordenPago);
        request.setAttribute("listaDetalle", estado.listaDetalle);
        request.setAttribute("listaFormasPago", estado.listaFormasPago);
        request.setAttribute("sucursalSeleccionada", estado.sucursalSeleccionada);
        request.setAttribute("formaEnEditor", estado.formaEnEditor);
        request.setAttribute("esNuevo", estado.esNuevo);
        request.setAttribute("idOrdenPagoExistente", estado.idOrdenPagoExistente);

        // Totales para la vista (la validación dura la repite el Service)
        request.setAttribute("totalOrden", calcularTotalDetalle(estado.listaDetalle));
        request.setAttribute("sumaFormas", calcularSumaFormas(estado.listaFormasPago));

        // Listas para modales y combos
        request.setAttribute("listaSucursales", estado.listaSucursales);
        request.setAttribute("listaCuentas", estado.listaCuentas);
        request.setAttribute("listaFormaPago", estado.listaFormaPago);
        request.setAttribute("listaChequeras", estado.listaChequeras);
        request.setAttribute("listaTipoCheque", estado.listaTipoCheque);
        request.setAttribute("listaOrdenesPago", estado.listaOrdenesPago);
        request.setAttribute("listaProvisiones", estado.listaProvisiones);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        request.getRequestDispatcher(vista).forward(request, response);
    }

    private void mostrarMensaje(HttpServletRequest request, String mensaje, String tipoAlert) {
        request.setAttribute("Message", mensaje);
        request.setAttribute("tipoAlert", tipoAlert);
    }

    /** Vuelve a la vista conservando el estado (usado en los caminos de validación fallida). */
    private void volverAVista(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, OrdenPagoState estado, String token) throws ServletException, IOException {
        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_ORDEN_PAGO);
    }

    private long calcularTotalDetalle(List<OrdenPagoDetalle> detalles) {
        long total = 0;
        for (OrdenPagoDetalle det : detalles) {
            if (det.getMonto() != null) {
                total += det.getMonto();
            }
        }
        return total;
    }

    private long calcularSumaFormas(List<FormaPagoDetalle> formas) {
        long suma = 0;
        for (FormaPagoDetalle fp : formas) {
            if (fp.getMonto() != null) {
                suma += fp.getMonto();
            }
        }
        return suma;
    }

    /**
     * Lee los datos de la cabecera del formulario y los guarda en el estado. Se llama en cada
     * acción para no perder lo cargado (patrón formulario único + JS). Los campos readonly/
     * disabled de una OP ya generada no se envían, por eso solo se aplica lo que llega.
     */
    private void leerDatosFormulario(HttpServletRequest request, OrdenPagoState estado) throws SQLException {
        String reciboStr = request.getParameter("recibo");
        String idSucursalStr = request.getParameter("idSucursal");
        String tipoPago = request.getParameter("tipoPago");

        if (reciboStr != null && !reciboStr.trim().isEmpty()) {
            try {
                estado.ordenPago.setNumeroRecibo(Integer.parseInt(reciboStr.trim()));
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Nro de recibo no numérico en leerDatosFormulario: {0}", reciboStr);
            }
        }

        if (idSucursalStr != null && !idSucursalStr.isEmpty()) {
            Sucursal sucursal = sucursalService.getSucursal(Long.parseLong(idSucursalStr));
            if (sucursal != null) {
                estado.sucursalSeleccionada = sucursal;
                estado.ordenPago.setSucursal(sucursal);
            }
        }

        if (tipoPago != null && !tipoPago.isEmpty()) {
            estado.ordenPago.setTipoPago(tipoPago);
        }
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

        if (!"OrdenPago".equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }

        if (accion == null) {
            accion = "ListarModal";
        }

        // Leer permisos del filter
        Boolean puedeInsertar = (Boolean) request.getAttribute("puedeInsertar");
        Boolean puedeBorrar = (Boolean) request.getAttribute("puedeBorrar");

        try {
            // Validar permisos para acciones de escritura
            switch (accion) {
                case "Nuevo":
                case "CargarProvision":
                case "CambiarTipoPago":
                case "AgregarForma":
                case "EliminarForma":
                case "Generar":
                    if (puedeInsertar == null || !puedeInsertar) {
                        mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                        accionListarModal(request, response);
                        return;
                    }
                    break;
                case "Anular":
                    if (puedeBorrar == null || !puedeBorrar) {
                        mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                        accionListarModal(request, response);
                        return;
                    }
                    break;
            }

            switch (accion) {
                case "Nuevo":
                    accionNuevo(request, response, session);
                    break;
                case "ListarModal":
                    accionListarModal(request, response);
                    break;
                case "CargarOrdenPago":
                    accionCargarOrdenPago(request, response, session);
                    break;
                case "CargarProvision":
                    accionCargarProvision(request, response, session, token);
                    break;
                case "CambiarTipoPago":
                    accionCambiarTipoPago(request, response, session, token);
                    break;
                case "AgregarForma":
                    accionAgregarForma(request, response, session, token, usuario);
                    break;
                case "EliminarForma":
                    accionEliminarForma(request, response, session, token);
                    break;
                case "Generar":
                    accionGenerar(request, response, session, token, usuario);
                    break;
                case "Anular":
                    accionAnular(request, response, session, token);
                    break;
                case "Cancelar":
                    accionCancelar(request, response, session, token);
                    break;
                default:
                    accionListarModal(request, response);
                    break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en OrdenPagoServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            forward(request, response, JSP_ORDEN_PAGO);
        }
    }

    // ==================== ACCIONES ====================

    /**
     * Listar órdenes de pago y provisiones pendientes para los modales de búsqueda.
     */
    private void accionListarModal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        request.setAttribute("token", "");
        request.setAttribute("listaOrdenesPago", ordenPagoService.listarOrdenesPago());
        request.setAttribute("listaProvisiones", provisionService.listarProvisionesPendientes());

        forward(request, response, JSP_ORDEN_PAGO);
    }

    /**
     * Crear nueva orden de pago (vacía: el detalle llega al seleccionar la provisión).
     */
    private void accionNuevo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws ServletException, IOException, SQLException {

        String nuevoToken = generarToken();
        OrdenPagoState estado = new OrdenPagoState();

        estado.esNuevo = true;
        estado.ordenPago.setNumero(ordenPagoService.obtenerProximoNumero());
        estado.ordenPago.setFechaEmision(new Date());
        estado.ordenPago.setEstado(ESTADO_OP_PENDIENTE);

        cargarListas(estado);

        guardarEstado(session, nuevoToken, estado);
        cargarDatosParaVista(request, estado, nuevoToken);

        forward(request, response, JSP_ORDEN_PAGO);
    }

    /**
     * Selecciona la provisión a pagar: trae sus facturas como detalle de la OP (solo lectura),
     * fija el proveedor y el monto total, y reinicia las formas de pago cargadas.
     * Si se llega sin token (desde la pantalla recién abierta) se inicia un documento nuevo.
     */
    private void accionCargarProvision(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        OrdenPagoState estado = obtenerEstado(session, token);
        if (estado == null) {
            // Sin documento abierto: equivale a "Nuevo" + selección de provisión.
            token = generarToken();
            estado = new OrdenPagoState();
            estado.esNuevo = true;
            estado.ordenPago.setNumero(ordenPagoService.obtenerProximoNumero());
            estado.ordenPago.setFechaEmision(new Date());
            estado.ordenPago.setEstado(ESTADO_OP_PENDIENTE);
            cargarListas(estado);
        } else {
            leerDatosFormulario(request, estado);
        }

        if (!estado.esNuevo) {
            mostrarMensaje(request, "No se puede cambiar la provisión de una orden de pago ya generada", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        String idProvisionStr = request.getParameter("idProvision");
        if (idProvisionStr == null || idProvisionStr.isEmpty()) {
            mostrarMensaje(request, "Debe seleccionar una provisión", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        Long idProvision = Long.parseLong(idProvisionStr);
        ProvisionCuentaPagar provision = provisionService.getProvision(idProvision);
        if (provision == null) {
            mostrarMensaje(request, "No se encontró la provisión", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        // La OP solo puede partir de una provisión 'Pendiente'; el Service lo revalida con
        // la fila bloqueada (FOR UPDATE) al generar, esto es solo para avisar antes.
        if (!ESTADO_PROV_PENDIENTE.equals(provision.getEstado())) {
            mostrarMensaje(request, "La provisión no está disponible para pagar (estado: "
                    + provision.getEstado() + ")", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        List<ProvisionCuentaPagarDetalle> detallesProvision =
                provisionService.listarDetallesPorProvision(idProvision);
        if (detallesProvision == null || detallesProvision.isEmpty()) {
            mostrarMensaje(request, "La provisión no tiene facturas para pagar", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        // El detalle de la OP es un espejo del de la provisión (mismo importe por factura).
        estado.listaDetalle.clear();
        for (ProvisionCuentaPagarDetalle pd : detallesProvision) {
            OrdenPagoDetalle det = new OrdenPagoDetalle();
            det.setCuentaPagar(pd.getCuentaPagar());
            det.setFacturaCompra(pd.getCuentaPagar() != null ? pd.getCuentaPagar().getFacturaCompra() : null);
            det.setMonto(pd.getMonto());
            estado.listaDetalle.add(det);
        }

        // Cambiar de provisión invalida las formas ya cargadas (cambia el total a pagar).
        estado.listaFormasPago.clear();
        estado.formaEnEditor = null;

        estado.ordenPago.setIdProvisionCtaPagar(idProvision);
        estado.ordenPago.setProveedor(provision.getProveedor());
        estado.ordenPago.setMonto(calcularTotalDetalle(estado.listaDetalle));

        mostrarMensaje(request, "Provisión cargada correctamente", "alert-success");
        volverAVista(request, response, session, estado, token);
    }

    /**
     * Cargar una orden de pago existente para visualización/anulación.
     */
    private void accionCargarOrdenPago(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws ServletException, IOException, SQLException {

        String idStr = request.getParameter("idOrdenPago");
        if (idStr == null || idStr.isEmpty()) {
            accionListarModal(request, response);
            return;
        }

        Long idOrdenPago = Long.parseLong(idStr);
        OrdenPago orden = ordenPagoService.getOrdenPago(idOrdenPago);
        if (orden == null) {
            mostrarMensaje(request, "Orden de pago no encontrada", "alert-danger");
            accionListarModal(request, response);
            return;
        }

        String nuevoToken = generarToken();
        OrdenPagoState estado = new OrdenPagoState();

        estado.esNuevo = false;
        estado.idOrdenPagoExistente = idOrdenPago;
        estado.ordenPago = orden;
        estado.sucursalSeleccionada = orden.getSucursal();

        estado.listaDetalle = ordenPagoService.listarDetallePorOrden(idOrdenPago);
        if (estado.listaDetalle == null) {
            estado.listaDetalle = new ArrayList<>();
        }
        estado.listaFormasPago = ordenPagoService.listarFormasPagoPorOrden(idOrdenPago);
        if (estado.listaFormasPago == null) {
            estado.listaFormasPago = new ArrayList<>();
        }

        cargarListas(estado);

        guardarEstado(session, nuevoToken, estado);
        cargarDatosParaVista(request, estado, nuevoToken);

        forward(request, response, JSP_ORDEN_PAGO);
    }

    /**
     * Cambiar el tipo de pago (reposición de fondo fijo / otros gastos) sin perder lo cargado.
     */
    private void accionCambiarTipoPago(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        OrdenPagoState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        leerDatosFormulario(request, estado);
        volverAVista(request, response, session, estado, token);
    }

    /**
     * Agrega una forma de pago al carrito. Si el tipo es cheque, arma también el cheque a emitir
     * (chequera, tipo y fechas); el número se asigna del rango de la chequera recién al Generar.
     */
    private void accionAgregarForma(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token, Usuario usuario)
            throws ServletException, IOException, SQLException {

        OrdenPagoState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        leerDatosFormulario(request, estado);

        if (!estado.esNuevo) {
            mostrarMensaje(request, "No se pueden agregar formas de pago a una orden ya generada", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        String idFormaPagoCabStr = request.getParameter("idFormaPagoCab");
        String idCuentaStr = request.getParameter("idCuenta");
        String montoStr = request.getParameter("montoForma");
        String tipoCambioStr = request.getParameter("tipoCambioForma");
        String referencia = request.getParameter("referenciaForma");
        String idChequeraStr = request.getParameter("idChequera");
        String idTipoChequeStr = request.getParameter("idTipoCheque");
        String fechaPagoStr = request.getParameter("fechaPagoCheque");
        String fechaVenciStr = request.getParameter("fechaVenciCheque");

        // El total a pagar lo fija la provisión: sin provisión no hay contra qué validar las formas.
        if (estado.ordenPago.getIdProvisionCtaPagar() == null || estado.listaDetalle.isEmpty()) {
            mostrarMensaje(request, "Primero debe seleccionar la provisión a pagar", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        if (idFormaPagoCabStr == null || idFormaPagoCabStr.isEmpty()) {
            errorEnEditorForma(request, response, session, estado, token,
                "Debe seleccionar el tipo de forma de pago");
            return;
        }
        if (idCuentaStr == null || idCuentaStr.isEmpty()) {
            errorEnEditorForma(request, response, session, estado, token,
                "Debe seleccionar la cuenta bancaria de origen");
            return;
        }
        if (montoStr == null || !montoStr.trim().matches("\\d+")) {
            errorEnEditorForma(request, response, session, estado, token,
                "Ingrese un monto válido para la forma de pago");
            return;
        }

        long monto = Long.parseLong(montoStr.trim());
        if (monto <= 0) {
            errorEnEditorForma(request, response, session, estado, token,
                "El monto de la forma de pago debe ser mayor a 0");
            return;
        }

        long totalOrden = calcularTotalDetalle(estado.listaDetalle);
        long sumaActual = calcularSumaFormas(estado.listaFormasPago);
        if (sumaActual + monto > totalOrden) {
            errorEnEditorForma(request, response, session, estado, token,
                "La suma de las formas de pago (" + (sumaActual + monto)
                + ") superaría el total a pagar (" + totalOrden + ")");
            return;
        }

        FormaPagoCabecera formaCab = formaPagoCabeceraService.getFormaPago(Long.parseLong(idFormaPagoCabStr));
        Cuenta cuenta = cuentaService.getCuenta(Long.parseLong(idCuentaStr));
        if (formaCab == null || cuenta == null) {
            errorEnEditorForma(request, response, session, estado, token,
                "Tipo de forma de pago o cuenta bancaria inválidos");
            return;
        }

        FormaPagoDetalle forma = new FormaPagoDetalle();
        forma.setFormaPagoCabecera(formaCab);
        forma.setCuenta(cuenta);
        forma.setMonto(monto);
        forma.setFecha(estado.ordenPago.getFechaEmision() != null
                ? estado.ordenPago.getFechaEmision() : new Date());
        // Pendiente de conciliación bancaria (el módulo de conciliación es el que lo cierra).
        forma.setEstado(ESTADO_OP_PENDIENTE);
        if (referencia != null && !referencia.trim().isEmpty()) {
            forma.setReferencia(referencia.trim());
        }
        if (tipoCambioStr != null && !tipoCambioStr.trim().isEmpty()) {
            try {
                // El tipo de cambio va por forma de pago: convierte la moneda de la cuenta
                // bancaria a la de la deuda (Gs). Se acepta coma o punto decimal.
                forma.setTipoCambio(Double.parseDouble(tipoCambioStr.trim().replace(',', '.')));
            } catch (NumberFormatException e) {
                errorEnEditorForma(request, response, session, estado, token,
                    "El tipo de cambio no es un número válido");
                return;
            }
        }

        // Línea de cheque: el Service emitirá el cheque real desde la chequera al generar.
        if (esFormaCheque(formaCab)) {
            if (idChequeraStr == null || idChequeraStr.isEmpty()) {
                estado.formaEnEditor = forma;
                errorEnEditorForma(request, response, session, estado, token,
                    "La forma de pago con cheque debe indicar la chequera");
                return;
            }
            if (idTipoChequeStr == null || idTipoChequeStr.isEmpty()) {
                estado.formaEnEditor = forma;
                errorEnEditorForma(request, response, session, estado, token,
                    "La forma de pago con cheque debe indicar el tipo de cheque");
                return;
            }

            Chequera chequera = chequeraService.getChequera(Long.parseLong(idChequeraStr));
            TipoCheque tipoCheque = tipoChequeService.getTipoCheque(Long.parseLong(idTipoChequeStr));
            if (chequera == null || tipoCheque == null) {
                estado.formaEnEditor = forma;
                errorEnEditorForma(request, response, session, estado, token,
                    "Chequera o tipo de cheque inválidos");
                return;
            }

            Cheque cheque = new Cheque();
            cheque.setChequera(chequera);
            cheque.setTipoCheque(tipoCheque);
            // El Service valida que el cheque tenga usuario emisor; lo toma de la sesión.
            cheque.setUsuario(usuario);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                if (fechaPagoStr != null && !fechaPagoStr.isEmpty()) {
                    cheque.setFechaPago(sdf.parse(fechaPagoStr));
                }
                if (fechaVenciStr != null && !fechaVenciStr.isEmpty()) {
                    cheque.setFechaVencimiento(sdf.parse(fechaVenciStr));
                }
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error al parsear fecha del cheque en accionAgregarForma", e);
            }
            forma.setCheque(cheque);
        }

        estado.listaFormasPago.add(forma);
        estado.formaEnEditor = null;

        mostrarMensaje(request, "Forma de pago agregada", "alert-success");
        volverAVista(request, response, session, estado, token);
    }

    /**
     * Camino de validación fallida del editor de formas de pago: muestra el aviso y reabre el
     * modal con lo que el usuario había cargado (atributo abrirModalForma que espera la vista).
     */
    private void errorEnEditorForma(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, OrdenPagoState estado, String token, String mensaje)
            throws ServletException, IOException {

        mostrarMensaje(request, mensaje, "alert-warning");
        request.setAttribute("abrirModalForma", true);
        volverAVista(request, response, session, estado, token);
    }

    /** El catálogo de formas de pago es cheque/transferencia; solo el cheque emite instrumento. */
    private boolean esFormaCheque(FormaPagoCabecera formaCab) {
        return formaCab != null && formaCab.getDescripcion() != null
                && formaCab.getDescripcion().toLowerCase().contains("cheque");
    }

    /**
     * Eliminar una forma de pago del carrito.
     */
    private void accionEliminarForma(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        OrdenPagoState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        leerDatosFormulario(request, estado);

        if (!estado.esNuevo) {
            mostrarMensaje(request, "No se pueden modificar las formas de pago de una orden ya generada", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        String idxStr = request.getParameter("index");
        if (idxStr != null && !idxStr.isEmpty()) {
            int index = Integer.parseInt(idxStr);
            if (index >= 0 && index < estado.listaFormasPago.size()) {
                estado.listaFormasPago.remove(index);
                mostrarMensaje(request, "Forma de pago eliminada", "alert-success");
            }
        }
        estado.formaEnEditor = null;

        volverAVista(request, response, session, estado, token);
    }

    /**
     * Genera la orden de pago: valida y delega en el Service, que en una sola transacción
     * inserta la OP, su detalle y sus formas de pago, emite los cheques, descuenta el saldo de
     * las cuentas a pagar y marca la provisión como 'Procesada'.
     */
    private void accionGenerar(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token, Usuario usuario)
            throws ServletException, IOException, SQLException {

        OrdenPagoState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        leerDatosFormulario(request, estado);

        if (!estado.esNuevo) {
            mostrarMensaje(request, "Esta orden de pago ya fue generada", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        if (estado.ordenPago.getIdProvisionCtaPagar() == null || estado.listaDetalle.isEmpty()) {
            mostrarMensaje(request, "Debe seleccionar la provisión a pagar", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        if (estado.sucursalSeleccionada == null) {
            mostrarMensaje(request, "Debe seleccionar una sucursal", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        if (estado.ordenPago.getTipoPago() == null || estado.ordenPago.getTipoPago().isEmpty()) {
            mostrarMensaje(request, "Debe seleccionar el tipo de pago", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        if (estado.listaFormasPago.isEmpty()) {
            mostrarMensaje(request, "Debe cargar al menos una forma de pago", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        long totalOrden = calcularTotalDetalle(estado.listaDetalle);
        long sumaFormas = calcularSumaFormas(estado.listaFormasPago);
        if (sumaFormas != totalOrden) {
            mostrarMensaje(request, "La suma de las formas de pago (" + sumaFormas
                    + ") debe ser igual al total a pagar (" + totalOrden + ")", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        // Completar la cabecera con lo que el formulario no envía
        estado.ordenPago.setMonto(totalOrden);
        estado.ordenPago.setEstado(ESTADO_OP_PENDIENTE);
        if (estado.ordenPago.getFechaEmision() == null) {
            estado.ordenPago.setFechaEmision(new Date());
        }
        if (estado.ordenPago.getNumero() == null) {
            estado.ordenPago.setNumero(ordenPagoService.obtenerProximoNumero());
        }
        // El recibo lo da el proveedor y no siempre existe (compras al contado): 0 = sin recibo.
        if (estado.ordenPago.getNumeroRecibo() == null) {
            estado.ordenPago.setNumeroRecibo(0);
        }
        // El emisor de cada cheque debe ser el usuario de la sesión (lo exige el Service).
        for (FormaPagoDetalle fp : estado.listaFormasPago) {
            if (fp.getCheque() != null && fp.getCheque().getUsuario() == null) {
                fp.getCheque().setUsuario(usuario);
            }
        }

        Long idGenerado = ordenPagoService.guardarOrdenPagoCompleta(
                estado.ordenPago, estado.listaDetalle, estado.listaFormasPago);

        limpiarEstado(session, token);

        mostrarMensaje(request, "Orden de pago generada correctamente. ID: " + idGenerado, "alert-success");
        accionListarModal(request, response);
    }

    /**
     * Anular la orden de pago: el Service revierte todo (devuelve el saldo de las facturas,
     * anula los cheques emitidos y reactiva la provisión a 'Pendiente').
     */
    private void accionAnular(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        OrdenPagoState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (estado.idOrdenPagoExistente == null) {
            mostrarMensaje(request, "No se puede anular una orden de pago que no ha sido generada", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        if ("Anulado".equals(estado.ordenPago.getEstado())) {
            mostrarMensaje(request, "La orden de pago ya está anulada", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        ordenPagoService.anularOrdenPagoCompleta(estado.idOrdenPagoExistente);

        limpiarEstado(session, token);

        mostrarMensaje(request, "Orden de pago anulada correctamente", "alert-success");
        accionListarModal(request, response);
    }

    /**
     * Cancelar y limpiar sesión
     */
    private void accionCancelar(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        limpiarEstado(session, token);
        accionListarModal(request, response);
    }

    // ==================== MÉTODOS HTTP ====================

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

    @Override
    public String getServletInfo() {
        return "OrdenPagoServlet - Patrón Session + Token";
    }
}
