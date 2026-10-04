/*
 * ABM de Artículos (referencial del módulo Compras).
 * Servlet sin estado en variables de instancia (thread-safe): el id a editar/actualizar viaja
 * por el request, no por un campo compartido. Calcado de CuentaServlet.
 */
package controlador;

import java.io.IOException;
import java.sql.SQLException;
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
import modelo.ArticuloDAO;
import modelo.Marca;
import modelo.Presentacion;
import modelo.TipoArticulo;
import modelo.TipoImpuesto;
import modelo.Usuario;
import service.ArticuloService;
import service.MarcaService;
import service.PresentacionService;
import service.TipoArticuloService;
import service.TipoImpuestoService;

@WebServlet(name = "ArticuloServlet", urlPatterns = {"/ArticuloServlet"})
public class ArticuloServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ArticuloServlet.class.getName());
    private static final String JSP_ARTICULO = "articulo.jsp";

    private final ArticuloService articuloService = new ArticuloService();
    private final TipoArticuloService tipoArticuloService = new TipoArticuloService();
    private final MarcaService marcaService = new MarcaService();
    private final PresentacionService presentacionService = new PresentacionService();
    private final TipoImpuestoService tipoImpuestoService = new TipoImpuestoService();

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
        if (!"Articulo".equals(menu)) {
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
            LOGGER.log(Level.SEVERE, "Error en ArticuloServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            request.getRequestDispatcher(JSP_ARTICULO).forward(request, response);
        }
    }

    // ==================== ACCIONES ====================

    private void accionInsertar(HttpServletRequest request) throws SQLException {
        Articulo articulo = leerArticuloFormulario(request);
        if (articulo == null) {
            return; // el mensaje de validación ya fue seteado
        }
        articuloService.insertarArticulo(articulo);
        mostrarMensaje(request, "Artículo agregado correctamente", "alert-success");
    }

    private void accionActualizar(HttpServletRequest request) throws SQLException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            mostrarMensaje(request, "Debe seleccionar un artículo para actualizar", "alert-warning");
            return;
        }
        Articulo articulo = leerArticuloFormulario(request);
        if (articulo == null) {
            return;
        }
        articulo.setIdArticulo(Long.parseLong(idStr));
        articuloService.actualizarArticulo(articulo);
        mostrarMensaje(request, "Artículo actualizado correctamente", "alert-success");
    }

    private void accionEditar(HttpServletRequest request) throws SQLException {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            Articulo articulo = articuloService.getArticulo(Long.parseLong(idStr));
            if (articulo != null) {
                request.setAttribute("articuloEdit", articulo);
            } else {
                mostrarMensaje(request, "No se pudo cargar el artículo", "alert-warning");
            }
        }
    }

    /**
     * Borra el artículo de la base. Si tiene movimientos la FK lo impide y el Service devuelve el
     * mensaje explicando que corresponde darlo de baja cambiándole el estado.
     */
    private void accionEliminar(HttpServletRequest request) {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            mostrarMensaje(request, "Id inválido para eliminar", "alert-danger");
            return;
        }
        try {
            articuloService.eliminarArticulo(Long.parseLong(idStr));
            mostrarMensaje(request, "Artículo eliminado correctamente", "alert-success");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se pudo eliminar el artículo " + idStr, e);
            mostrarMensaje(request, e.getMessage(), "alert-warning");
        }
    }

    // ==================== HELPERS ====================

    /**
     * Lee y valida los campos del formulario. Devuelve null si falta algo (y setea el mensaje).
     *
     * <p>Solo son obligatorios los que la base exige: descripción, precio de venta, impuesto y
     * estado. Tipo de artículo, marca, presentación, precio de compra y código de barras quedan
     * nulos si no se cargan.
     */
    private Articulo leerArticuloFormulario(HttpServletRequest request) {
        String descripcion = request.getParameter("descripcion");
        String precioVentaStr = request.getParameter("precioVenta");
        String idImpuestoStr = request.getParameter("idImpuesto");
        String estado = request.getParameter("estado");

        if (descripcion == null || descripcion.trim().isEmpty()
                || precioVentaStr == null || precioVentaStr.trim().isEmpty()
                || idImpuestoStr == null || idImpuestoStr.isEmpty()
                || estado == null || estado.trim().isEmpty()) {
            mostrarMensaje(request, "Complete la descripción, el precio de venta, el impuesto y el estado",
                    "alert-warning");
            return null;
        }
        if (descripcion.trim().length() > 100) {
            mostrarMensaje(request, "La descripción no puede superar los 100 caracteres", "alert-warning");
            return null;
        }

        Articulo articulo = new Articulo();
        articulo.setDescripcion(descripcion.trim());
        articulo.setEstado(estado.trim());
        articulo.setTipoImpuesto(new TipoImpuesto(Long.parseLong(idImpuestoStr)));

        Long precioVenta = leerMonto(request, precioVentaStr, "precio de venta");
        if (precioVenta == null) {
            return null;
        }
        articulo.setPrecioVenta(precioVenta);

        String precioCompraStr = request.getParameter("precioCompra");
        if (precioCompraStr != null && !precioCompraStr.trim().isEmpty()) {
            Long precioCompra = leerMonto(request, precioCompraStr, "precio de compra");
            if (precioCompra == null) {
                return null;
            }
            articulo.setPrecioCompra(precioCompra);
        }

        // El código de barras lo emite el fabricante: va como texto y puede tener ceros a la
        // izquierda. Es el que usa el escaneo desde el celular (SCANNER_MOVIL.md).
        String codigo = request.getParameter("codigo");
        if (codigo != null && !codigo.trim().isEmpty()) {
            articulo.setCodigo(codigo.trim());
        }

        Long idTipo = leerId(request.getParameter("idTipoArticulo"));
        if (idTipo != null) {
            articulo.setTipoArticulo(new TipoArticulo(idTipo));
        }
        Long idMarca = leerId(request.getParameter("idMarca"));
        if (idMarca != null) {
            articulo.setMarca(new Marca(idMarca));
        }
        Long idPresentacion = leerId(request.getParameter("idPresentacion"));
        if (idPresentacion != null) {
            articulo.setPresentacion(new Presentacion(idPresentacion));
        }
        return articulo;
    }

    /** Monto en Guaraníes: entero y no negativo. La máscara de miles manda los puntos. */
    private Long leerMonto(HttpServletRequest request, String valor, String campo) {
        try {
            long monto = Long.parseLong(valor.trim().replace(".", "").replace(",", ""));
            if (monto < 0) {
                mostrarMensaje(request, "El " + campo + " no puede ser negativo", "alert-warning");
                return null;
            }
            return monto;
        } catch (NumberFormatException e) {
            mostrarMensaje(request, "El " + campo + " no es un número válido", "alert-warning");
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

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        List<Articulo> articulos = articuloService.listarArticulo();
        request.setAttribute("listaArticulos", articulos);
        request.setAttribute("listaTiposArticulo", tipoArticuloService.listarTipoArticulo());
        request.setAttribute("listaMarcas", marcaService.listarMarca());
        request.setAttribute("listaPresentaciones", presentacionService.listarPresentacion());
        request.setAttribute("listaImpuestos", tipoImpuestoService.listarTipoImpuesto());
        request.setAttribute("estadoActivo", ArticuloDAO.ESTADO_ACTIVO);
        request.setAttribute("estadoInactivo", ArticuloDAO.ESTADO_INACTIVO);
        request.getRequestDispatcher(JSP_ARTICULO).forward(request, response);
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
