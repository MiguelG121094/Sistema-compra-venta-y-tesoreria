/*
 * Provisión de cuenta a pagar (Tesorería). Agrupa por proveedor las cuentas a pagar
 * a provisionar (importe a pagar por factura), reservándolas (estado 'En provision').
 * El saldo se descuenta recién en la Orden de Pago.
 * Patrón Session + Token (calcado de FacturaCompraServlet). Ver MODULO_TESORERIA_PLAN.md §B.
 */
package controlador;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
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
import modelo.CuentaPagar;
import modelo.Proveedor;
import modelo.ProvisionCuentaPagar;
import modelo.ProvisionCuentaPagarDetalle;
import modelo.Usuario;
import service.CuentaPagarService;
import service.ProveedorService;
import service.ProvisionCuentaPagarService;

@WebServlet(name = "ProvisionCuentaPagarServlet", urlPatterns = {"/ProvisionCuentaPagarServlet"})
public class ProvisionCuentaPagarServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProvisionCuentaPagarServlet.class.getName());
    private static final String SESSION_PREFIX = "provision_";
    private static final String JSP_PROVISION = "provision.jsp";

    private final ProvisionCuentaPagarService provisionService = new ProvisionCuentaPagarService();
    private final CuentaPagarService cuentaPagarService = new CuentaPagarService();
    private final ProveedorService proveedorService = new ProveedorService();

    // ==================== ESTADO DEL DOCUMENTO ====================

    private static class ProvisionState implements Serializable {
        private static final long serialVersionUID = 1L;

        ProvisionCuentaPagar provision = new ProvisionCuentaPagar();
        List<ProvisionCuentaPagarDetalle> listaDetalle = new ArrayList<>();
        Proveedor proveedorSeleccionado;

        // Editor de línea
        CuentaPagar cuentaEnEditor;    // cuenta seleccionada del modal para agregar/editar
        Integer indexSeleccionado;     // índice de la línea en edición (null = alta)
        Long importeEditor;            // importe a pagar mostrado en el editor

        boolean esNuevo = false;
        Long idProvisionExistente;     // seteado al cargar una provisión (para anular/ver)

        // Listas para modales
        List<Proveedor> listaProveedores;
        List<CuentaPagar> listaCuentasPagar;
        List<ProvisionCuentaPagar> listaProvisiones;
    }

    // ==================== HELPERS DE SESIÓN ====================

    private String generarToken() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private ProvisionState obtenerEstado(HttpSession session, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return (ProvisionState) session.getAttribute(SESSION_PREFIX + token);
    }

    private void guardarEstado(HttpSession session, String token, ProvisionState estado) {
        session.setAttribute(SESSION_PREFIX + token, estado);
    }

    private void limpiarEstado(HttpSession session, String token) {
        if (token != null) {
            session.removeAttribute(SESSION_PREFIX + token);
        }
    }

    private ProvisionState obtenerEstadoORedireccionar(HttpServletRequest request,
            HttpServletResponse response, HttpSession session, String token) throws IOException {
        ProvisionState estado = obtenerEstado(session, token);
        if (estado == null) {
            mostrarMensaje(request, "Sesión expirada, inicie de nuevo", "alert-warning");
            response.sendRedirect("ProvisionCuentaPagarServlet?menu=ProvisionCuentaPagar&accion=ListarModal");
        }
        return estado;
    }

    private void cargarDatosParaVista(HttpServletRequest request, ProvisionState estado, String token) {
        request.setAttribute("token", token);
        request.setAttribute("provision", estado.provision);
        request.setAttribute("listaDetalle", estado.listaDetalle);
        request.setAttribute("proveedorSeleccionado", estado.proveedorSeleccionado);
        request.setAttribute("cuentaEnEditor", estado.cuentaEnEditor);
        request.setAttribute("indexSeleccionado", estado.indexSeleccionado);
        request.setAttribute("importeEditor", estado.importeEditor);
        request.setAttribute("esNuevo", estado.esNuevo);
        request.setAttribute("idProvisionExistente", estado.idProvisionExistente);
        request.setAttribute("listaProveedores", estado.listaProveedores);
        request.setAttribute("listaCuentasPagar", estado.listaCuentasPagar);
        request.setAttribute("listaProvisiones", estado.listaProvisiones);
        request.setAttribute("totalProvision", calcularNeto(estado.listaDetalle));
    }

    private void mostrarMensaje(HttpServletRequest request, String mensaje, String tipoAlert) {
        request.setAttribute("Message", mensaje);
        request.setAttribute("tipoAlert", tipoAlert);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        request.getRequestDispatcher(vista).forward(request, response);
    }

    private long calcularNeto(List<ProvisionCuentaPagarDetalle> detalles) {
        long neto = 0;
        for (ProvisionCuentaPagarDetalle d : detalles) {
            if (d.getMonto() != null) {
                neto += d.getMonto();
            }
        }
        return neto;
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
        if (!"ProvisionCuentaPagar".equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }
        if (accion == null) {
            accion = "ListarModal";
        }

        Boolean puedeInsertar = (Boolean) request.getAttribute("puedeInsertar");
        Boolean puedeBorrar = (Boolean) request.getAttribute("puedeBorrar");
        switch (accion) {
            case "Nuevo":
            case "CargarProveedor":
            case "SeleccionarCuenta":
            case "AgregarLinea":
            case "EditarLinea":
            case "EliminarLinea":
            case "Generar":
                if (puedeInsertar == null || !puedeInsertar) {
                    mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                    try { accionListarModal(request, response); } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
                    return;
                }
                break;
            case "Anular":
                if (puedeBorrar == null || !puedeBorrar) {
                    mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                    try { accionListarModal(request, response); } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error", e); }
                    return;
                }
                break;
            default:
                break;
        }

        try {
            switch (accion) {
                case "Nuevo":            accionNuevo(request, response, session); break;
                case "CargarProveedor":  accionCargarProveedor(request, response, session, token); break;
                case "SeleccionarCuenta":accionSeleccionarCuenta(request, response, session, token); break;
                case "AgregarLinea":     accionAgregarLinea(request, response, session, token); break;
                case "EditarLinea":      accionEditarLinea(request, response, session, token); break;
                case "EliminarLinea":    accionEliminarLinea(request, response, session, token); break;
                case "Generar":          accionGenerar(request, response, session, token); break;
                case "CargarProvision":  accionCargarProvision(request, response, session); break;
                case "Anular":           accionAnular(request, response, session, token); break;
                case "Cancelar":         accionCancelar(request, response, session, token); break;
                case "ListarModal":
                default:                 accionListarModal(request, response); break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en ProvisionCuentaPagarServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            forward(request, response, JSP_PROVISION);
        }
    }

    // ==================== ACCIONES ====================

    private void accionListarModal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        request.setAttribute("token", "");
        request.setAttribute("listaProvisiones", provisionService.listarProvisiones());
        forward(request, response, JSP_PROVISION);
    }

    private void accionNuevo(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException, SQLException {
        String nuevoToken = generarToken();
        ProvisionState estado = new ProvisionState();
        estado.esNuevo = true;
        estado.provision.setFecha(new Date());
        estado.provision.setEstado("Pendiente");
        estado.listaProveedores = proveedorService.listarProveedores();
        estado.listaProvisiones = provisionService.listarProvisiones();

        guardarEstado(session, nuevoToken, estado);
        cargarDatosParaVista(request, estado, nuevoToken);

        // Tooltip de ayuda sobre el botón "Buscar Proveedor": el proveedor es el primer paso
        // (define qué cuentas a pagar se pueden provisionar). Mismo mecanismo que el tooltip de
        // la sucursal en PedidoCompraServlet/pedidoCompra.jsp.
        request.setAttribute("mostrarTooltip", true);
        request.setAttribute("mensajeTooltip", "Seleccione un proveedor para cargar su lista de cuentas a pagar");

        forward(request, response, JSP_PROVISION);
    }

    /**
     * Selecciona el proveedor: carga sus cuentas a pagar y reinicia el detalle
     * (una provisión es por proveedor).
     */
    private void accionCargarProveedor(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {
        ProvisionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String idProvStr = request.getParameter("idProveedor");
        if (idProvStr != null && !idProvStr.isEmpty()) {
            Proveedor prov = proveedorService.getProveedor(Long.parseLong(idProvStr));
            estado.proveedorSeleccionado = prov;
            estado.provision.setProveedor(prov);
            estado.listaCuentasPagar = cuentaPagarService.listarCuentasPagarPorProveedor(prov.getIdProveedor());
            estado.listaDetalle.clear();
            estado.cuentaEnEditor = null;
            estado.indexSeleccionado = null;
            estado.importeEditor = null;
            if (estado.listaCuentasPagar == null || estado.listaCuentasPagar.isEmpty()) {
                mostrarMensaje(request, "El proveedor no tiene cuentas a pagar para provisionar", "alert-warning");
            }
        }
        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_PROVISION);
    }

    /** Selecciona una cuenta a pagar del modal y la carga en el editor (importe = saldo). */
    private void accionSeleccionarCuenta(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {
        ProvisionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String idCtaStr = request.getParameter("idCtaPagar");
        String idFactStr = request.getParameter("idFactura");
        if (idCtaStr != null && idFactStr != null && estado.listaCuentasPagar != null) {
            long idCta = Long.parseLong(idCtaStr);
            long idFact = Long.parseLong(idFactStr);
            for (CuentaPagar cp : estado.listaCuentasPagar) {
                if (cp.getIdCuentaPagar() == idCta && cp.getFacturaCompra().getIdFacturaCompra() == idFact) {
                    estado.cuentaEnEditor = cp;
                    estado.indexSeleccionado = null;
                    estado.importeEditor = cp.getSaldo();
                    break;
                }
            }
        }
        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_PROVISION);
    }

    /** Agrega (o actualiza) la línea del detalle con el importe a pagar del editor. */
    private void accionAgregarLinea(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {
        ProvisionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String importeStr = request.getParameter("importe");
        if (estado.cuentaEnEditor == null) {
            mostrarMensaje(request, "Seleccione una cuenta a pagar primero", "alert-warning");
            guardarEstado(session, token, estado);
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_PROVISION);
            return;
        }
        if (importeStr == null || importeStr.trim().isEmpty() || !importeStr.trim().matches("-?\\d+")) {
            mostrarMensaje(request, "Ingrese un importe a pagar válido", "alert-warning");
            guardarEstado(session, token, estado);
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_PROVISION);
            return;
        }
        long importe = Long.parseLong(importeStr.trim());
        long saldo = estado.cuentaEnEditor.getSaldo() != null ? estado.cuentaEnEditor.getSaldo() : 0L;

        // El importe debe tener el mismo signo que el saldo y no superarlo en valor absoluto.
        if (saldo > 0 && (importe <= 0 || importe > saldo)) {
            mostrarMensaje(request, "El importe a pagar debe estar entre 1 y el saldo (" + saldo + ")", "alert-warning");
            guardarEstado(session, token, estado);
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_PROVISION);
            return;
        }
        if (saldo < 0 && (importe >= 0 || importe < saldo)) {
            mostrarMensaje(request, "El saldo a favor a aplicar debe estar entre " + saldo + " y -1", "alert-warning");
            guardarEstado(session, token, estado);
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_PROVISION);
            return;
        }

        if (estado.indexSeleccionado != null
                && estado.indexSeleccionado >= 0
                && estado.indexSeleccionado < estado.listaDetalle.size()) {
            // Actualizar línea existente
            estado.listaDetalle.get(estado.indexSeleccionado).setMonto(importe);
        } else {
            // Alta: evitar duplicar la misma cuenta
            for (ProvisionCuentaPagarDetalle d : estado.listaDetalle) {
                if (d.getCuentaPagar().getIdCuentaPagar().equals(estado.cuentaEnEditor.getIdCuentaPagar())
                        && d.getCuentaPagar().getFacturaCompra().getIdFacturaCompra()
                            .equals(estado.cuentaEnEditor.getFacturaCompra().getIdFacturaCompra())) {
                    mostrarMensaje(request, "Esa cuenta ya está en el detalle", "alert-warning");
                    guardarEstado(session, token, estado);
                    cargarDatosParaVista(request, estado, token);
                    forward(request, response, JSP_PROVISION);
                    return;
                }
            }
            ProvisionCuentaPagarDetalle det = new ProvisionCuentaPagarDetalle();
            det.setCuentaPagar(estado.cuentaEnEditor);
            det.setMonto(importe);
            estado.listaDetalle.add(det);
        }

        estado.cuentaEnEditor = null;
        estado.indexSeleccionado = null;
        estado.importeEditor = null;

        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_PROVISION);
    }

    private void accionEditarLinea(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {
        ProvisionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String idxStr = request.getParameter("index");
        if (idxStr != null && !idxStr.isEmpty()) {
            int idx = Integer.parseInt(idxStr);
            if (idx >= 0 && idx < estado.listaDetalle.size()) {
                ProvisionCuentaPagarDetalle det = estado.listaDetalle.get(idx);
                estado.cuentaEnEditor = det.getCuentaPagar();
                estado.indexSeleccionado = idx;
                estado.importeEditor = det.getMonto();
            }
        }
        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_PROVISION);
    }

    private void accionEliminarLinea(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {
        ProvisionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String idxStr = request.getParameter("index");
        if (idxStr != null && !idxStr.isEmpty()) {
            int idx = Integer.parseInt(idxStr);
            if (idx >= 0 && idx < estado.listaDetalle.size()) {
                estado.listaDetalle.remove(idx);
            }
        }
        estado.cuentaEnEditor = null;
        estado.indexSeleccionado = null;
        estado.importeEditor = null;

        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_PROVISION);
    }

    private void accionGenerar(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {
        ProvisionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (estado.proveedorSeleccionado == null) {
            mostrarMensaje(request, "Debe seleccionar un proveedor", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_PROVISION);
            return;
        }
        if (estado.listaDetalle.isEmpty()) {
            mostrarMensaje(request, "Debe agregar al menos una cuenta a pagar", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_PROVISION);
            return;
        }
        long neto = calcularNeto(estado.listaDetalle);
        if (neto < 0) {
            mostrarMensaje(request, "El neto de la provisión no puede ser negativo (saldo a favor sin deuda suficiente)", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_PROVISION);
            return;
        }

        estado.provision.setProveedor(estado.proveedorSeleccionado);
        if (estado.provision.getFecha() == null) {
            estado.provision.setFecha(new Date());
        }
        estado.provision.setEstado("Pendiente");

        Long id = provisionService.guardarProvisionCompleta(estado.provision, estado.listaDetalle);
        limpiarEstado(session, token);
        mostrarMensaje(request, "Provisión generada correctamente. ID: " + id, "alert-success");
        accionListarModal(request, response);
    }

    private void accionCargarProvision(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws ServletException, IOException, SQLException {
        String idStr = request.getParameter("idProvision");
        if (idStr == null || idStr.isEmpty()) {
            accionListarModal(request, response);
            return;
        }
        Long id = Long.parseLong(idStr);
        ProvisionCuentaPagar prov = provisionService.getProvision(id);
        if (prov == null) {
            accionListarModal(request, response);
            return;
        }
        String nuevoToken = generarToken();
        ProvisionState estado = new ProvisionState();
        estado.esNuevo = false;
        estado.idProvisionExistente = id;
        estado.provision = prov;
        estado.proveedorSeleccionado = prov.getProveedor();
        estado.listaDetalle = provisionService.listarDetallesPorProvision(id);
        if (estado.listaDetalle == null) {
            estado.listaDetalle = new ArrayList<>();
        }
        estado.listaProvisiones = provisionService.listarProvisiones();

        guardarEstado(session, nuevoToken, estado);
        cargarDatosParaVista(request, estado, nuevoToken);
        forward(request, response, JSP_PROVISION);
    }

    private void accionAnular(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {
        ProvisionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (estado.idProvisionExistente == null) {
            mostrarMensaje(request, "No hay una provisión cargada para anular", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_PROVISION);
            return;
        }
        if ("Anulado".equals(estado.provision.getEstado())) {
            mostrarMensaje(request, "La provisión ya está anulada", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_PROVISION);
            return;
        }
        // TODO: bloquear si la provisión ya tiene una orden de pago (cuando exista ese módulo).
        provisionService.anularProvisionCompleta(estado.idProvisionExistente);
        limpiarEstado(session, token);
        mostrarMensaje(request, "Provisión anulada correctamente", "alert-success");
        accionListarModal(request, response);
    }

    private void accionCancelar(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {
        limpiarEstado(session, token);
        accionListarModal(request, response);
    }

    // ==================== doGet / doPost ====================

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
