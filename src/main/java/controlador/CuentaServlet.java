/*
 * ABM de Cuentas bancarias (módulo Tesorería).
 * Servlet sin estado en variables de instancia (thread-safe): el id a editar/actualizar
 * viaja por el request, no por un campo compartido. Services stateless (final).
 * Sigue el estándar de calidad de FacturaCompraServlet (acciones delegadas, permisos, Toastr).
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
import modelo.Cuenta;
import modelo.EntidadFinanciera;
import modelo.Moneda;
import modelo.TipoCuenta;
import modelo.Usuario;
import service.CuentaService;
import service.EntidadFinancieraService;
import service.MonedaService;
import service.TipoCuentaService;

@WebServlet(name = "CuentaServlet", urlPatterns = {"/CuentaServlet"})
public class CuentaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CuentaServlet.class.getName());
    private static final String JSP_CUENTA = "cuenta.jsp";

    private final CuentaService cuentaService = new CuentaService();
    private final EntidadFinancieraService entidadFinancieraService = new EntidadFinancieraService();
    private final TipoCuentaService tipoCuentaService = new TipoCuentaService();
    private final MonedaService monedaService = new MonedaService();

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
        if (!"Cuenta".equals(menu)) {
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
            LOGGER.log(Level.SEVERE, "Error en CuentaServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            listar(request, response);
        }
    }

    // ==================== ACCIONES ====================

    private void accionInsertar(HttpServletRequest request) throws SQLException {
        Cuenta cuenta = leerCuentaFormulario(request);
        if (cuenta == null) {
            return; // el mensaje de validación ya fue seteado
        }
        cuentaService.insertarCuenta(cuenta);
        mostrarMensaje(request, "Cuenta agregada correctamente", "alert-success");
    }

    private void accionActualizar(HttpServletRequest request) throws SQLException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            mostrarMensaje(request, "Debe seleccionar una cuenta para actualizar", "alert-warning");
            return;
        }
        Cuenta cuenta = leerCuentaFormulario(request);
        if (cuenta == null) {
            return;
        }
        cuenta.setIdCuenta(Long.parseLong(idStr));
        cuentaService.actualizarCuenta(cuenta);
        mostrarMensaje(request, "Cuenta actualizada correctamente", "alert-success");
    }

    private void accionEditar(HttpServletRequest request) throws SQLException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            Cuenta cuenta = cuentaService.getCuenta(Long.parseLong(idStr));
            if (cuenta != null) {
                request.setAttribute("cuentaEdit", cuenta);
            } else {
                mostrarMensaje(request, "No se pudo cargar la cuenta", "alert-warning");
            }
        }
    }

    private void accionEliminar(HttpServletRequest request) throws SQLException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            mostrarMensaje(request, "Id inválido para eliminar", "alert-danger");
            return;
        }
        cuentaService.eliminarCuenta(Long.parseLong(idStr));
        mostrarMensaje(request, "Cuenta eliminada correctamente", "alert-success");
    }

    // ==================== HELPERS ====================

    /**
     * Lee y valida los campos del formulario. Devuelve null si falta algo (y setea el mensaje).
     */
    private Cuenta leerCuentaFormulario(HttpServletRequest request) {
        String idEntStr = request.getParameter("idEntidadFinanciera");
        String idTipoStr = request.getParameter("idTipoCuenta");
        String idMonStr = request.getParameter("idMoneda");
        String numeroStr = request.getParameter("numero");

        if (idEntStr == null || idEntStr.isEmpty()
                || idTipoStr == null || idTipoStr.isEmpty()
                || idMonStr == null || idMonStr.isEmpty()
                || numeroStr == null || numeroStr.trim().isEmpty()) {
            mostrarMensaje(request, "Complete todos los campos de la cuenta", "alert-warning");
            return null;
        }
        if (!numeroStr.trim().matches("\\d+")) {
            mostrarMensaje(request, "El número de cuenta debe ser numérico", "alert-danger");
            return null;
        }

        Cuenta cuenta = new Cuenta();
        cuenta.setEntidadFinanciera(new EntidadFinanciera(Long.parseLong(idEntStr)));
        cuenta.setTipoCuenta(new TipoCuenta(Long.parseLong(idTipoStr)));
        cuenta.setMoneda(new Moneda(Long.parseLong(idMonStr)));
        cuenta.setNumero(Long.parseLong(numeroStr.trim()));
        return cuenta;
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        request.setAttribute("listaCuentas", cuentaService.listarCuenta());
        request.setAttribute("listaEntidades", entidadFinancieraService.listarEntidadFinanciera());
        request.setAttribute("listaTiposCuenta", tipoCuentaService.listarTipoCuenta());
        request.setAttribute("listaMonedas", monedaService.listarMoneda());
        request.getRequestDispatcher(JSP_CUENTA).forward(request, response);
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
