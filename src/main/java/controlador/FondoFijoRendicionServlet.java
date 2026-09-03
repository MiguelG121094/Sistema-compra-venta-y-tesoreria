/*
 * Rendicion de fondo fijo (modulo Tesoreria, requerimiento 3.6, §E del plan).
 * Patron Session+Token calcado de ProvisionCuentaPagarServlet: el documento en edicion vive en la
 * sesion bajo una clave con token, porque hay un carrito de facturas que sostener entre pedidos.
 * Se elige primero el responsable y despues las facturas, como se acordo con el prototipo.
 */
package controlador;

import java.io.IOException;
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
import modelo.FondoFijo;
import modelo.FondoFijoRendicion;
import modelo.FondoFijoRendicionDAO;
import modelo.FondoFijoRendicionDetalle;
import modelo.Usuario;
import service.FondoFijoRendicionService;
import service.FondoFijoService;

@WebServlet(name = "FondoFijoRendicionServlet", urlPatterns = {"/FondoFijoRendicionServlet"})
public class FondoFijoRendicionServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FondoFijoRendicionServlet.class.getName());
    private static final String JSP_RENDICION = "fondoFijoRendicion.jsp";
    private static final String SESSION_PREFIX = "rendicionFF_";

    private final FondoFijoRendicionService rendicionService = new FondoFijoRendicionService();
    private final FondoFijoService fondoFijoService = new FondoFijoService();

    /** Documento en edicion. Serializable porque vive en la HttpSession. */
    private static class RendicionState implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        FondoFijoRendicion rendicion = new FondoFijoRendicion();
        FondoFijo fondoFijoSeleccionado;
        List<FondoFijoRendicionDetalle> listaDetalle = new ArrayList<>();

        boolean esNuevo = false;
        Long idRendicionExistente;

        List<FondoFijo> listaFondosFijos;
        List<CuentaPagar> listaCuentasPagar;
        List<FondoFijoRendicion> listaRendiciones;
    }

    // ==================== HELPERS DE SESIÓN ====================

    private String generarToken() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private RendicionState obtenerEstado(HttpSession session, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return (RendicionState) session.getAttribute(SESSION_PREFIX + token);
    }

    private void guardarEstado(HttpSession session, String token, RendicionState estado) {
        session.setAttribute(SESSION_PREFIX + token, estado);
    }

    private void limpiarEstado(HttpSession session, String token) {
        if (token != null) {
            session.removeAttribute(SESSION_PREFIX + token);
        }
    }

    private RendicionState obtenerEstadoORedireccionar(HttpServletRequest request,
            HttpServletResponse response, HttpSession session, String token) throws IOException {
        RendicionState estado = obtenerEstado(session, token);
        if (estado == null) {
            mostrarMensaje(request, "Sesión expirada, inicie de nuevo", "alert-warning");
            response.sendRedirect("FondoFijoRendicionServlet?menu=RendicionFondoFijo&accion=ListarModal");
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
        if (!"RendicionFondoFijo".equals(menu)) {
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
                case "CargarResponsable":
                case "AgregarLinea":
                case "EliminarLinea":
                case "Generar":
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
                case "Nuevo":              accionNuevo(request, response, session); break;
                case "CargarResponsable":  accionCargarResponsable(request, response, session, token); break;
                case "AgregarLinea":       accionAgregarLinea(request, response, session, token); break;
                case "EliminarLinea":      accionEliminarLinea(request, response, session, token); break;
                case "Generar":            accionGenerar(request, response, session, token); break;
                case "CargarRendicion":    accionCargarRendicion(request, response, session); break;
                case "Anular":             accionAnular(request, response, session, token); break;
                case "Cancelar":           accionCancelar(request, response, session, token); break;
                case "ListarModal":
                default:                   accionListarModal(request, response); break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en FondoFijoRendicionServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            accionListarModal(request, response);
        }
    }

    // ==================== ACCIONES ====================

    /** Pantalla inerte: sin documento abierto solo se puede usar Nuevo y Buscar Rendición. */
    private void accionListarModal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("listaRendiciones", rendicionService.listarRendiciones());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar rendiciones", e);
        }
        forward(request, response, JSP_RENDICION);
    }

    private void accionNuevo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws ServletException, IOException, SQLException {

        String token = generarToken();
        RendicionState estado = new RendicionState();
        estado.esNuevo = true;
        // La fecha y el numero los pone el sistema al abrir la rendicion, como pide el caso de uso.
        estado.rendicion.setFechaEmisionRendicion(new Date());
        estado.rendicion.setNumeroRendicion(rendicionService.obtenerProximoNumero());
        estado.rendicion.setEstado(FondoFijoRendicionDAO.ESTADO_GENERADA);
        cargarListas(estado);

        volverAVista(request, response, session, estado, token);
    }

    /** Carga el fondo fijo elegido en el modal de responsables. */
    private void accionCargarResponsable(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        RendicionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        Long idFondoFijo = leerId(request.getParameter("idFondoFijo"));
        if (idFondoFijo == null) {
            mostrarMensaje(request, "Debe seleccionar un responsable", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        FondoFijo fondoFijo = fondoFijoService.getFondoFijo(idFondoFijo);
        if (fondoFijo == null) {
            mostrarMensaje(request, "No se pudo cargar el responsable", "alert-warning");
        } else {
            estado.fondoFijoSeleccionado = fondoFijo;
            estado.rendicion.setFondoFijo(fondoFijo);
        }
        volverAVista(request, response, session, estado, token);
    }

    /**
     * Agrega al detalle la factura elegida en el modal de cuentas a pagar. El monto rendido es
     * siempre el saldo completo: son montos chicos y se rinden enteros.
     */
    private void accionAgregarLinea(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        RendicionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (estado.fondoFijoSeleccionado == null) {
            mostrarMensaje(request, "Primero seleccione el responsable", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        Long idCta = leerId(request.getParameter("idCtaPagar"));
        Long idFact = leerId(request.getParameter("idFacturaCompra"));
        if (idCta == null || idFact == null) {
            mostrarMensaje(request, "Debe seleccionar una factura", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        for (FondoFijoRendicionDetalle det : estado.listaDetalle) {
            if (det.getCuentaPagar().getIdCuentaPagar().equals(idCta)
                    && det.getCuentaPagar().getFacturaCompra().getIdFacturaCompra().equals(idFact)) {
                mostrarMensaje(request, "Esa factura ya está en el detalle", "alert-warning");
                volverAVista(request, response, session, estado, token);
                return;
            }
        }

        CuentaPagar elegida = null;
        if (estado.listaCuentasPagar != null) {
            for (CuentaPagar cp : estado.listaCuentasPagar) {
                if (cp.getIdCuentaPagar().equals(idCta)
                        && cp.getFacturaCompra().getIdFacturaCompra().equals(idFact)) {
                    elegida = cp;
                    break;
                }
            }
        }
        if (elegida == null) {
            mostrarMensaje(request, "La factura ya no está disponible para rendir", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        FondoFijoRendicionDetalle detalle = new FondoFijoRendicionDetalle();
        detalle.setCuentaPagar(elegida);
        detalle.setMontoRendido(elegida.getSaldo());
        estado.listaDetalle.add(detalle);

        mostrarMensaje(request, "Factura agregada a la rendición", "alert-success");
        volverAVista(request, response, session, estado, token);
    }

    private void accionEliminarLinea(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        RendicionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String idxStr = request.getParameter("index");
        if (idxStr != null && !idxStr.isEmpty()) {
            int index = Integer.parseInt(idxStr);
            if (index >= 0 && index < estado.listaDetalle.size()) {
                estado.listaDetalle.remove(index);
                mostrarMensaje(request, "Línea eliminada", "alert-success");
            }
        }
        volverAVista(request, response, session, estado, token);
    }

    private void accionGenerar(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        RendicionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (!estado.esNuevo) {
            mostrarMensaje(request, "La rendición ya fue generada", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        if (estado.fondoFijoSeleccionado == null) {
            mostrarMensaje(request, "Debe seleccionar el responsable", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        if (estado.listaDetalle.isEmpty()) {
            mostrarMensaje(request, "Debe cargar al menos una factura", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }

        try {
            Long id = rendicionService.guardarRendicionCompleta(estado.rendicion, estado.listaDetalle);
            limpiarEstado(session, token);
            mostrarMensaje(request, "Rendición N° " + estado.rendicion.getNumeroRendicion()
                    + " generada correctamente (id " + id + ")", "alert-success");
            // El caso de uso pide limpiar y deshabilitar todo despues de guardar.
            accionListarModal(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Rendición rechazada: {0}", e.getMessage());
            mostrarMensaje(request, e.getMessage(), "alert-warning");
            volverAVista(request, response, session, estado, token);
        }
    }

    /** Trae una rendición ya generada para verla o anularla. */
    private void accionCargarRendicion(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws ServletException, IOException, SQLException {

        Long id = leerId(request.getParameter("id"));
        if (id == null) {
            mostrarMensaje(request, "Debe seleccionar una rendición", "alert-warning");
            accionListarModal(request, response);
            return;
        }
        FondoFijoRendicion rendicion = rendicionService.getRendicion(id);
        if (rendicion == null) {
            mostrarMensaje(request, "No se pudo cargar la rendición", "alert-warning");
            accionListarModal(request, response);
            return;
        }

        String token = generarToken();
        RendicionState estado = new RendicionState();
        estado.esNuevo = false;
        estado.rendicion = rendicion;
        estado.fondoFijoSeleccionado = rendicion.getFondoFijo();
        estado.idRendicionExistente = id;
        estado.listaDetalle = rendicionService.listarDetalles(id);
        cargarListas(estado);

        volverAVista(request, response, session, estado, token);
    }

    private void accionAnular(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        RendicionState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (estado.idRendicionExistente == null) {
            mostrarMensaje(request, "Cargue una rendición para anularla", "alert-warning");
            volverAVista(request, response, session, estado, token);
            return;
        }
        try {
            rendicionService.anularRendicionCompleta(estado.idRendicionExistente);
            limpiarEstado(session, token);
            mostrarMensaje(request, "Rendición anulada correctamente", "alert-success");
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

    // ==================== HELPERS ====================

    private void cargarListas(RendicionState estado) throws SQLException {
        estado.listaFondosFijos = fondoFijoService.listarFondosFijos();
        estado.listaCuentasPagar = rendicionService.listarCuentasPagarFondoFijo();
        estado.listaRendiciones = rendicionService.listarRendiciones();
    }

    private void volverAVista(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, RendicionState estado, String token)
            throws ServletException, IOException {

        guardarEstado(session, token, estado);
        request.setAttribute("token", token);
        request.setAttribute("rendicion", estado.rendicion);
        request.setAttribute("fondoFijoSeleccionado", estado.fondoFijoSeleccionado);
        request.setAttribute("listaDetalle", estado.listaDetalle);
        request.setAttribute("esNuevo", estado.esNuevo);
        request.setAttribute("idRendicionExistente", estado.idRendicionExistente);
        request.setAttribute("listaFondosFijos", estado.listaFondosFijos);
        request.setAttribute("listaCuentasPagar", estado.listaCuentasPagar);
        request.setAttribute("listaRendiciones", estado.listaRendiciones);
        request.setAttribute("totalRendicion", calcularTotal(estado.listaDetalle));
        forward(request, response, JSP_RENDICION);
    }

    private long calcularTotal(List<FondoFijoRendicionDetalle> detalles) {
        long total = 0;
        for (FondoFijoRendicionDetalle d : detalles) {
            if (d.getMontoRendido() != null) {
                total += d.getMontoRendido();
            }
        }
        return total;
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
