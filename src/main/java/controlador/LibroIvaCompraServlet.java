/*
 * Informe de Libro de Compras (Ley 125/91), módulo Compras.
 * Servlet de sólo lectura: no hay documento en edición, así que no usa Session+Token.
 * Toma el período y el tipo de comprobante del formulario y arma la grilla.
 */
package controlador;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import modelo.LibroIvaCompra;
import modelo.Usuario;
import service.LibroIvaCompraService;

@WebServlet(name = "LibroIvaCompraServlet", urlPatterns = {"/LibroIvaCompraServlet"})
public class LibroIvaCompraServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LibroIvaCompraServlet.class.getName());
    private static final String JSP_LIBRO = "libroIvaCompra.jsp";
    private static final String FORMATO_FECHA = "yyyy-MM-dd";

    private final LibroIvaCompraService libroIvaService = new LibroIvaCompraService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String menu = request.getParameter("menu");

        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        if (!"LibroIvaCompra".equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }

        try {
            listar(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en LibroIvaCompraServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            request.getRequestDispatcher(JSP_LIBRO).forward(request, response);
        }
    }

    /**
     * Arma el informe del período pedido. Sin fechas en el request arranca con el mes en curso,
     * para que la pantalla no abra vacía.
     */
    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Date desde = leerFecha(request.getParameter("fechaDesde"));
        Date hasta = leerFecha(request.getParameter("fechaHasta"));
        if (desde == null || hasta == null) {
            desde = primerDiaDelMes();
            hasta = new Date();
        }
        if (desde.after(hasta)) {
            mostrarMensaje(request, "La fecha desde no puede ser posterior a la fecha hasta", "alert-warning");
            request.setAttribute("fechaDesde", formatear(desde));
            request.setAttribute("fechaHasta", formatear(hasta));
            request.getRequestDispatcher(JSP_LIBRO).forward(request, response);
            return;
        }

        String origen = request.getParameter("origen");
        if (origen != null && origen.trim().isEmpty()) {
            origen = null;
        }

        List<LibroIvaCompra> filas = libroIvaService.listarParaInforme(desde, hasta, origen);

        request.setAttribute("listaLibroIva", filas);
        request.setAttribute("totales", LibroIvaCompraService.calcularTotales(filas));
        request.setAttribute("fechaDesde", formatear(desde));
        request.setAttribute("fechaHasta", formatear(hasta));
        // Los input date necesitan el texto yyyy-MM-dd; el encabezado del informe, la fecha.
        request.setAttribute("desdeFecha", desde);
        request.setAttribute("hastaFecha", hasta);
        request.setAttribute("origen", origen);
        request.getRequestDispatcher(JSP_LIBRO).forward(request, response);
    }

    // ==================== AUXILIARES ====================

    private Date leerFecha(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(FORMATO_FECHA).parse(valor.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private String formatear(Date fecha) {
        return new SimpleDateFormat(FORMATO_FECHA).format(fecha);
    }

    private Date primerDiaDelMes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
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
