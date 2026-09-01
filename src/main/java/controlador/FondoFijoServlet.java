/*
 * ABM de Fondo Fijo (modulo Tesoreria, requerimiento 3.5).
 * Servlet sin estado en variables de instancia (thread-safe): el id a editar/actualizar
 * viaja por el request, no por un campo compartido. Services stateless (final).
 * Calcado de CuentaServlet, que es el ABM de referencia del modulo.
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
import modelo.FondoFijo;
import modelo.Proveedor;
import modelo.Usuario;
import service.FondoFijoService;
import service.ProveedorService;

@WebServlet(name = "FondoFijoServlet", urlPatterns = {"/FondoFijoServlet"})
public class FondoFijoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FondoFijoServlet.class.getName());
    private static final String JSP_FONDO_FIJO = "fondoFijo.jsp";

    private final FondoFijoService fondoFijoService = new FondoFijoService();
    private final ProveedorService proveedorService = new ProveedorService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String menu = request.getParameter("menu");
        String accion = request.getParameter("accion");

        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        if (!"FondoFijo".equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }
        if (accion == null) {
            accion = "Listar";
        }

        Boolean puedeInsertar = (Boolean) request.getAttribute("puedeInsertar");
        Boolean puedeEditar = (Boolean) request.getAttribute("puedeEditar");
        Boolean puedeBorrar = (Boolean) request.getAttribute("puedeBorrar");

        try {
            switch (accion) {
                case "Insertar":
                    if (!Boolean.TRUE.equals(puedeInsertar)) {
                        sinPermiso(request);
                    } else {
                        accionInsertar(request);
                    }
                    listar(request, response);
                    break;
                case "Editar":
                    accionEditar(request);
                    listar(request, response);
                    break;
                case "Actualizar":
                    if (!Boolean.TRUE.equals(puedeEditar)) {
                        sinPermiso(request);
                    } else {
                        accionActualizar(request);
                    }
                    listar(request, response);
                    break;
                case "Eliminar":
                    if (!Boolean.TRUE.equals(puedeBorrar)) {
                        sinPermiso(request);
                    } else {
                        accionEliminar(request);
                    }
                    listar(request, response);
                    break;
                case "Cancelar":
                case "Listar":
                default:
                    listar(request, response);
                    break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en FondoFijoServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            request.getRequestDispatcher(JSP_FONDO_FIJO).forward(request, response);
        }
    }

    // ==================== ACCIONES ====================

    private void accionInsertar(HttpServletRequest request) {
        FondoFijo fondoFijo = leerFondoFijoFormulario(request);
        if (fondoFijo == null) {
            return; // el mensaje de validacion ya fue seteado
        }
        try {
            fondoFijoService.insertarFondoFijo(fondoFijo);
            mostrarMensaje(request, "Fondo fijo agregado correctamente", "alert-success");
        } catch (SQLException e) {
            mostrarRechazo(request, e);
        }
    }

    private void accionActualizar(HttpServletRequest request) {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            mostrarMensaje(request, "Debe seleccionar un fondo fijo para actualizar", "alert-warning");
            return;
        }
        FondoFijo fondoFijo = leerFondoFijoFormulario(request);
        if (fondoFijo == null) {
            return;
        }
        fondoFijo.setIdFondoFijo(Long.parseLong(idStr));
        try {
            fondoFijoService.actualizarFondoFijo(fondoFijo);
            mostrarMensaje(request, "Fondo fijo actualizado correctamente", "alert-success");
        } catch (SQLException e) {
            mostrarRechazo(request, e);
        }
    }

    private void accionEditar(HttpServletRequest request) throws SQLException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            FondoFijo fondoFijo = fondoFijoService.getFondoFijo(Long.parseLong(idStr));
            if (fondoFijo != null) {
                request.setAttribute("fondoFijoEdit", fondoFijo);
            } else {
                mostrarMensaje(request, "No se pudo cargar el fondo fijo", "alert-warning");
            }
        }
    }

    private void accionEliminar(HttpServletRequest request) {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            mostrarMensaje(request, "Id inválido para eliminar", "alert-danger");
            return;
        }
        try {
            fondoFijoService.eliminarFondoFijo(Long.parseLong(idStr));
            mostrarMensaje(request, "Fondo fijo eliminado correctamente", "alert-success");
        } catch (SQLException e) {
            mostrarRechazo(request, e);
        }
    }

    // ==================== HELPERS ====================

    /** Lee y valida los campos del formulario. Devuelve null si falta algo (y setea el mensaje). */
    private FondoFijo leerFondoFijoFormulario(HttpServletRequest request) {
        String responsable = request.getParameter("responsable");
        String montoStr = request.getParameter("montoAsignado");
        String fechaStr = request.getParameter("fechaAsignacion");
        String idProveedorStr = request.getParameter("idProveedor");

        if (responsable == null || responsable.trim().isEmpty()
                || montoStr == null || montoStr.trim().isEmpty()
                || fechaStr == null || fechaStr.trim().isEmpty()
                || idProveedorStr == null || idProveedorStr.isEmpty()) {
            mostrarMensaje(request, "Complete todos los campos del fondo fijo", "alert-warning");
            return null;
        }
        if (responsable.trim().length() > 50) {
            mostrarMensaje(request, "El responsable no puede superar los 50 caracteres", "alert-danger");
            return null;
        }
        if (!montoStr.trim().matches("\\d+")) {
            mostrarMensaje(request, "El monto asignado debe ser numérico", "alert-danger");
            return null;
        }
        long monto = Long.parseLong(montoStr.trim());
        if (monto <= 0) {
            mostrarMensaje(request, "El monto asignado debe ser mayor a cero", "alert-danger");
            return null;
        }
        Date fecha;
        try {
            fecha = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr.trim());
        } catch (ParseException e) {
            mostrarMensaje(request, "La fecha de asignación no es válida", "alert-danger");
            return null;
        }

        FondoFijo fondoFijo = new FondoFijo();
        fondoFijo.setResponsable(responsable.trim());
        fondoFijo.setMontoAsignado(monto);
        fondoFijo.setFechaAsignacion(fecha);
        fondoFijo.setProveedor(new Proveedor(Long.parseLong(idProveedorStr)));
        return fondoFijo;
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        request.setAttribute("listaFondosFijos", fondoFijoService.listarFondosFijos());
        request.setAttribute("listaProveedores", proveedorService.listarProveedores());
        request.getRequestDispatcher(JSP_FONDO_FIJO).forward(request, response);
    }

    /** Las validaciones del Service viajan como SQLException: se muestra el mensaje tal cual. */
    private void mostrarRechazo(HttpServletRequest request, SQLException e) {
        LOGGER.log(Level.WARNING, "Operación rechazada en FondoFijoServlet: {0}", e.getMessage());
        mostrarMensaje(request, e.getMessage(), "alert-warning");
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
