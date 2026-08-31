/*
 * ABM de Chequeras (modulo Tesoreria).
 * Servlet sin estado en variables de instancia (thread-safe): el id a editar/actualizar
 * viaja por el request, no por un campo compartido. Services stateless (final).
 * Calcado de CuentaServlet, que es el ABM de referencia del modulo.
 */
package controlador;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import modelo.Chequera;
import modelo.Cuenta;
import modelo.Usuario;
import service.ChequeraService;
import service.CuentaService;

@WebServlet(name = "ChequeraServlet", urlPatterns = {"/ChequeraServlet"})
public class ChequeraServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ChequeraServlet.class.getName());
    private static final String JSP_CHEQUERA = "chequera.jsp";

    private final ChequeraService chequeraService = new ChequeraService();
    private final CuentaService cuentaService = new CuentaService();

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
        if (!"Chequera".equals(menu)) {
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
            LOGGER.log(Level.SEVERE, "Error en ChequeraServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            request.getRequestDispatcher(JSP_CHEQUERA).forward(request, response);
        }
    }

    // ==================== ACCIONES ====================

    private void accionInsertar(HttpServletRequest request) {
        Chequera chequera = leerChequeraFormulario(request);
        if (chequera == null) {
            return; // el mensaje de validacion ya fue seteado
        }
        try {
            chequeraService.insertarChequera(chequera);
            mostrarMensaje(request, "Chequera agregada correctamente", "alert-success");
        } catch (SQLException e) {
            mostrarRechazo(request, e);
        }
    }

    private void accionActualizar(HttpServletRequest request) {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            mostrarMensaje(request, "Debe seleccionar una chequera para actualizar", "alert-warning");
            return;
        }
        Chequera chequera = leerChequeraFormulario(request);
        if (chequera == null) {
            return;
        }
        chequera.setIdChequera(Long.parseLong(idStr));
        try {
            chequeraService.actualizarChequera(chequera);
            mostrarMensaje(request, "Chequera actualizada correctamente", "alert-success");
        } catch (SQLException e) {
            mostrarRechazo(request, e);
        }
    }

    private void accionEditar(HttpServletRequest request) throws SQLException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            Chequera chequera = chequeraService.getChequera(Long.parseLong(idStr));
            if (chequera != null) {
                request.setAttribute("chequeraEdit", chequera);
            } else {
                mostrarMensaje(request, "No se pudo cargar la chequera", "alert-warning");
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
            chequeraService.eliminarChequera(Long.parseLong(idStr));
            mostrarMensaje(request, "Chequera eliminada correctamente", "alert-success");
        } catch (SQLException e) {
            mostrarRechazo(request, e);
        }
    }

    // ==================== HELPERS ====================

    /**
     * Lee y valida los campos del formulario. Devuelve null si falta algo (y setea el mensaje).
     * Las validaciones que necesitan mirar la base —solapamiento de rangos y cheques ya emitidos—
     * viven en el Service, adentro de la transaccion.
     */
    private Chequera leerChequeraFormulario(HttpServletRequest request) {
        String idCuentaStr = request.getParameter("idCuenta");
        String serieStr = request.getParameter("serie");
        String desdeStr = request.getParameter("desdeNumero");
        String hastaStr = request.getParameter("hastaNumero");

        if (idCuentaStr == null || idCuentaStr.isEmpty()
                || serieStr == null || serieStr.trim().isEmpty()
                || desdeStr == null || desdeStr.trim().isEmpty()
                || hastaStr == null || hastaStr.trim().isEmpty()) {
            mostrarMensaje(request, "Complete todos los campos de la chequera", "alert-warning");
            return null;
        }
        if (!serieStr.trim().matches("\\d+") || !desdeStr.trim().matches("\\d+") || !hastaStr.trim().matches("\\d+")) {
            mostrarMensaje(request, "La serie y los números de cheque deben ser numéricos", "alert-danger");
            return null;
        }

        long serie = Long.parseLong(serieStr.trim());
        long desde = Long.parseLong(desdeStr.trim());
        long hasta = Long.parseLong(hastaStr.trim());

        if (serie <= 0 || desde <= 0) {
            mostrarMensaje(request, "La serie y el número inicial deben ser mayores a cero", "alert-danger");
            return null;
        }
        if (desde > hasta) {
            mostrarMensaje(request, "El número inicial no puede ser mayor al final", "alert-danger");
            return null;
        }

        Chequera chequera = new Chequera();
        chequera.setCuenta(new Cuenta(Long.parseLong(idCuentaStr)));
        chequera.setSerie(serie);
        chequera.setDesdeNumero(desde);
        chequera.setHastaNumero(hasta);
        return chequera;
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        request.setAttribute("listaChequeras", chequeraService.listarChequeras());
        request.setAttribute("listaCuentas", cuentaService.listarCuenta());
        request.getRequestDispatcher(JSP_CHEQUERA).forward(request, response);
    }

    /** Las validaciones del Service viajan como SQLException: se muestra el mensaje tal cual. */
    private void mostrarRechazo(HttpServletRequest request, SQLException e) {
        LOGGER.log(Level.WARNING, "Operación rechazada en ChequeraServlet: {0}", e.getMessage());
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
