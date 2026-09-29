/*
 * Gestión de cheques emitidos (módulo Tesorería, §G3 del plan).
 * Pantalla de consulta con dos acciones sobre un cheque: anularlo y registrar su entrega.
 * Sin estado en variables de instancia ni Session+Token: no hay documento en edición, el id
 * del cheque viaja por el request.
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
import modelo.Usuario;
import service.ChequeService;

@WebServlet(name = "ChequeServlet", urlPatterns = {"/ChequeServlet"})
public class ChequeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ChequeServlet.class.getName());
    private static final String JSP_CHEQUE = "cheque.jsp";

    private final ChequeService chequeService = new ChequeService();

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
        if (!"Cheque".equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }
        if (accion == null) {
            accion = "Listar";
        }

        Boolean puedeEditar = (Boolean) request.getAttribute("puedeEditar");
        Boolean puedeBorrar = (Boolean) request.getAttribute("puedeBorrar");

        try {
            switch (accion) {
                case "Anular":
                    if (!Boolean.TRUE.equals(puedeBorrar)) {
                        sinPermiso(request);
                    } else {
                        accionAnular(request);
                    }
                    break;
                case "RegistrarEntrega":
                    if (!Boolean.TRUE.equals(puedeEditar)) {
                        sinPermiso(request);
                    } else {
                        accionRegistrarEntrega(request);
                    }
                    break;
                case "Listar":
                default:
                    break;
            }
            listar(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en ChequeServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            request.getRequestDispatcher(JSP_CHEQUE).forward(request, response);
        }
    }

    // ==================== ACCIONES ====================

    /**
     * Anula el cheque. El Service rechaza el cobrado, el ya anulado y el de una orden con más de
     * una forma de pago; esos mensajes se muestran como aviso, no como error de base.
     */
    private void accionAnular(HttpServletRequest request) {
        Long idCheque = leerId(request.getParameter("idCheque"));
        if (idCheque == null) {
            mostrarMensaje(request, "Seleccione un cheque para anular", "alert-warning");
            return;
        }
        try {
            chequeService.anularChequeIndividual(idCheque);
            mostrarMensaje(request, "Cheque anulado correctamente. Se devolvió el saldo de las "
                    + "facturas y la orden de pago quedó anulada.", "alert-success");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se pudo anular el cheque " + idCheque, e);
            mostrarMensaje(request, e.getMessage(), "alert-warning");
        }
    }

    private void accionRegistrarEntrega(HttpServletRequest request) {
        Long idCheque = leerId(request.getParameter("idCheque"));
        if (idCheque == null) {
            mostrarMensaje(request, "Seleccione un cheque para registrar la entrega", "alert-warning");
            return;
        }
        Date fechaEntrega = leerFecha(request.getParameter("fechaEntrega"));
        if (fechaEntrega == null) {
            mostrarMensaje(request, "Indique una fecha de entrega válida", "alert-warning");
            return;
        }
        try {
            chequeService.registrarEntrega(idCheque, fechaEntrega, request.getParameter("entregadoA"));
            mostrarMensaje(request, "Entrega registrada correctamente", "alert-success");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se pudo registrar la entrega del cheque " + idCheque, e);
            mostrarMensaje(request, e.getMessage(), "alert-warning");
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        request.setAttribute("listaCheques", chequeService.listarParaGestion());
        request.getRequestDispatcher(JSP_CHEQUE).forward(request, response);
    }

    // ==================== AUXILIARES ====================

    private Long leerId(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date leerFecha(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(valor.trim());
        } catch (ParseException e) {
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
}
