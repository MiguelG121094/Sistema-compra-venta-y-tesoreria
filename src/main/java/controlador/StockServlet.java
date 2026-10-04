/*
 * Puntos de reposicion por deposito (modulo Compras): minima y maxima de cada articulo.
 * Servlet sin estado en variables de instancia: el deposito elegido viaja por el request.
 * No usa Session+Token porque no hay documento en edicion, es una grilla que se graba entera.
 */
package controlador;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import modelo.Articulo;
import modelo.Deposito;
import modelo.Stock;
import modelo.Usuario;
import service.DepositoService;
import service.StockService;

@WebServlet(name = "StockServlet", urlPatterns = {"/StockServlet"})
public class StockServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StockServlet.class.getName());
    private static final String JSP_STOCK = "stock.jsp";

    private final StockService stockService = new StockService();
    private final DepositoService depositoService = new DepositoService();

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
        if (!"Stock".equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }
        if (accion == null) {
            accion = "Listar";
        }

        Boolean puedeEditar = (Boolean) request.getAttribute("puedeEditar");

        try {
            if ("Guardar".equals(accion)) {
                if (!Boolean.TRUE.equals(puedeEditar)) {
                    mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                } else {
                    accionGuardar(request);
                }
            }
            listar(request, response, usuario);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en StockServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            request.getRequestDispatcher(JSP_STOCK).forward(request, response);
        }
    }

    // ==================== ACCIONES ====================

    /**
     * Graba la minima y la maxima de todas las filas de la grilla, en una sola transaccion.
     *
     * <p>Las filas se leen por posicion (minima_0, maxima_0, idArticulo_0, ...) porque es una
     * grilla editable y no un formulario de un solo registro.
     */
    private void accionGuardar(HttpServletRequest request) throws SQLException {
        Long idDeposito = leerId(request.getParameter("idDeposito"));
        if (idDeposito == null) {
            mostrarMensaje(request, "Debe seleccionar el depósito", "alert-warning");
            return;
        }

        String[] idsArticulo = request.getParameterValues("idArticulo");
        if (idsArticulo == null || idsArticulo.length == 0) {
            mostrarMensaje(request, "No hay artículos para guardar", "alert-warning");
            return;
        }

        List<Stock> filas = new ArrayList<>();
        for (int i = 0; i < idsArticulo.length; i++) {
            Long idArticulo = leerId(idsArticulo[i]);
            if (idArticulo == null) {
                continue;
            }
            Long minima = leerCantidad(request, request.getParameter("minima_" + i), "mínima");
            Long maxima = leerCantidad(request, request.getParameter("maxima_" + i), "máxima");
            if (minima == null || maxima == null) {
                return; // el mensaje ya fue seteado
            }
            if (maxima > 0 && minima > maxima) {
                mostrarMensaje(request, "La cantidad mínima no puede ser mayor que la máxima",
                        "alert-warning");
                return;
            }

            Stock fila = new Stock(new Deposito(idDeposito), new Articulo(idArticulo));
            fila.setCantidadMinima(minima);
            fila.setCantidadMaxima(maxima);
            filas.add(fila);
        }

        int grabadas = stockService.guardarLimites(idDeposito, filas);
        mostrarMensaje(request, "Se guardaron los límites de " + grabadas
                + (grabadas == 1 ? " artículo" : " artículos"), "alert-success");
    }

    private void listar(HttpServletRequest request, HttpServletResponse response, Usuario usuario)
            throws ServletException, IOException, SQLException {

        /* Los depositos son los de la sucursal del usuario: cada uno administra el stock de donde
           trabaja, igual que los documentos salen con su sucursal. */
        List<Deposito> depositos = new ArrayList<>();
        if (usuario.getSucursal() != null && usuario.getSucursal().getIdSucursal() != null) {
            depositos = depositoService.listarDepostioPorSucursal(usuario.getSucursal().getIdSucursal());
        } else {
            mostrarMensaje(request, "El usuario no tiene una sucursal asignada", "alert-warning");
        }
        if (depositos == null) {
            depositos = new ArrayList<>();
        }

        Long idDeposito = leerId(request.getParameter("idDeposito"));
        if (idDeposito == null && depositos.size() == 1) {
            // Con un solo deposito no tiene sentido obligar a elegirlo.
            idDeposito = depositos.get(0).getIdDeposito();
        }

        if (idDeposito != null) {
            request.setAttribute("listaStock", stockService.listarPorDeposito(idDeposito));
        }
        request.setAttribute("listaDepositos", depositos);
        request.setAttribute("idDeposito", idDeposito);
        request.getRequestDispatcher(JSP_STOCK).forward(request, response);
    }

    // ==================== AUXILIARES ====================

    /** Cantidad entera y no negativa; vacio vale 0, que es "sin limite definido". */
    private Long leerCantidad(HttpServletRequest request, String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0L;
        }
        try {
            long cantidad = Long.parseLong(valor.trim().replace(".", ""));
            if (cantidad < 0) {
                mostrarMensaje(request, "La cantidad " + campo + " no puede ser negativa", "alert-warning");
                return null;
            }
            return cantidad;
        } catch (NumberFormatException e) {
            mostrarMensaje(request, "La cantidad " + campo + " no es un número válido", "alert-warning");
            return null;
        }
    }

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
