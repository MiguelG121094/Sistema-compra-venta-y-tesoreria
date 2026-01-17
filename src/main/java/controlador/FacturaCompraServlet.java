/*
 * FacturaCompraServlet - Implementación con patrón Session + Token
 *
 * Este servlet utiliza una arquitectura donde:
 * - El estado del documento se guarda en sesión con un token único
 * - Cada pestaña/documento tiene su propio token
 * - Thread-safe y sin conflictos entre usuarios
 *
 * Estructura: Switch-Case con Métodos Delegados
 */
package controlador;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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

@WebServlet(name = "FacturaCompraServlet", urlPatterns = {"/FacturaCompraServlet"})
public class FacturaCompraServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FacturaCompraServlet.class.getName());
    private static final String SESSION_PREFIX = "facturaCompra_";
    private static final String JSP_FACTURA = "facturaCompra.jsp";

    // Services (stateless, pueden ser de instancia)
    private final FacturaCompraService facturaCompraService = new FacturaCompraService();
    private final FacturaCompraDetalleService facturaCompraDetalleService = new FacturaCompraDetalleService();
    private final OrdenCompraService ordenCompraService = new OrdenCompraService();
    private final OrdenCompraDetalleService ordenCompraDetalleService = new OrdenCompraDetalleService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final SucursalService sucursalService = new SucursalService();
    private final ArticuloService articuloService = new ArticuloService();

    // ==================== CLASE DE ESTADO ====================

    /**
     * Clase que encapsula todo el estado de trabajo de una factura.
     * Se guarda en sesión con un token único.
     * Implementa Serializable para permitir persistencia de sesión.
     */
    private static class FacturaCompraState implements Serializable {
        private static final long serialVersionUID = 1L;

        FacturaCompra facturaCompra = new FacturaCompra();
        List<FacturaCompraDetalle> listaDetalle = new ArrayList<>();
        Proveedor proveedorSeleccionado;
        Sucursal sucursalSeleccionada;
        OrdenCompra ordenCompraSeleccionada;
        FacturaCompraDetalle detalleSeleccionado;
        boolean esNuevo = false;

        // Datos para modales (se cargan una vez)
        List<OrdenCompra> listaOrdenesCompra;
        List<FacturaCompra> listaFacturasCompra;
        List<Proveedor> listaProveedores;
        List<Sucursal> listaSucursales;
        List<Articulo> listaArticulos;
    }

    // ==================== MÉTODOS HELPER PARA SESIÓN ====================

    private String generarToken() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private FacturaCompraState obtenerEstado(HttpSession session, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return (FacturaCompraState) session.getAttribute(SESSION_PREFIX + token);
    }

    private void guardarEstado(HttpSession session, String token, FacturaCompraState estado) {
        session.setAttribute(SESSION_PREFIX + token, estado);
    }

    private void limpiarEstado(HttpSession session, String token) {
        if (token != null) {
            session.removeAttribute(SESSION_PREFIX + token);
        }
    }

    private void cargarDatosParaVista(HttpServletRequest request, FacturaCompraState estado, String token) {
        request.setAttribute("token", token);
        request.setAttribute("facturaCompra", estado.facturaCompra);
        request.setAttribute("listaFacturaCompraDetalle", estado.listaDetalle);
        request.setAttribute("proveedorSeleccionado", estado.proveedorSeleccionado);
        request.setAttribute("sucursalSeleccionada", estado.sucursalSeleccionada);
        request.setAttribute("ordenCompraSeleccionada", estado.ordenCompraSeleccionada);
        request.setAttribute("detalleSeleccionado", estado.detalleSeleccionado);
        request.setAttribute("esNuevo", estado.esNuevo);

        // Listas para modales
        request.setAttribute("listaOrdenesCompra", estado.listaOrdenesCompra);
        request.setAttribute("listaFacturasCompra", estado.listaFacturasCompra);
        request.setAttribute("listaProveedores", estado.listaProveedores);
        request.setAttribute("listaSucursales", estado.listaSucursales);
        request.setAttribute("listaArticulos", estado.listaArticulos);
    }

    /**
     * Obtiene el estado de la sesión o redirige si expiró
     * @return null si la sesión expiró (ya se hizo redirect)
     */
    private FacturaCompraState obtenerEstadoORedireccionar(HttpServletRequest request,
            HttpServletResponse response, HttpSession session, String token) throws IOException {

        FacturaCompraState estado = obtenerEstado(session, token);
        if (estado == null) {
            mostrarMensaje(request, "Sesión expirada, inicie de nuevo", "alert-warning");
            response.sendRedirect("FacturaCompraServlet?menu=FacturaCompra&accion=ListarModal");
        }
        return estado;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        request.getRequestDispatcher(vista).forward(request, response);
    }

    private void mostrarMensaje(HttpServletRequest request, String mensaje, String tipoAlert) {
        request.setAttribute("Message", mensaje);
        request.setAttribute("tipoAlert", tipoAlert);
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

        if (!"FacturaCompra".equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }

        if (accion == null) {
            accion = "ListarModal";
        }

        try {
            switch (accion) {
                case "Nuevo":
                    accionNuevo(request, response, session, usuario);
                    break;
                case "ListarModal":
                    accionListarModal(request, response);
                    break;
                case "CargarFactura":
                    accionCargarFactura(request, response, session);
                    break;
                case "CargarOrdenCompra":
                    accionCargarOrdenCompra(request, response, session, token);
                    break;
                case "CargarProveedor":
                    accionCargarProveedor(request, response, session, token);
                    break;
                case "CambiarSucursal":
                    accionCambiarSucursal(request, response, session, token);
                    break;
                case "CambiarCondicion":
                    accionCambiarCondicion(request, response, session, token);
                    break;
                case "CambiarTipoFactura":
                    accionCambiarTipoFactura(request, response, session, token);
                    break;
                case "AgregarArticulo":
                    accionAgregarArticulo(request, response, session, token);
                    break;
                case "EditarArticulo":
                    accionEditarArticulo(request, response, session, token);
                    break;
                case "ActualizarArticulo":
                    accionActualizarArticulo(request, response, session, token);
                    break;
                case "EliminarArticulo":
                    accionEliminarArticulo(request, response, session, token);
                    break;
                case "Guardar":
                    accionGuardar(request, response, session, token);
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
            LOGGER.log(Level.SEVERE, "Error en FacturaCompraServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            forward(request, response, JSP_FACTURA);
        }
    }

    // ==================== ACCIONES ====================

    /**
     * Crear nueva factura de compra
     */
    private void accionNuevo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, Usuario usuario) throws ServletException, IOException, SQLException {

        String nuevoToken = generarToken();
        FacturaCompraState estado = new FacturaCompraState();

        estado.esNuevo = true;
        estado.facturaCompra.setUsuario(usuario);
        estado.facturaCompra.setFechaCarga(new Date());
        estado.facturaCompra.setEstado("Pendiente");

        // Cargar datos para modales
        estado.listaOrdenesCompra = ordenCompraService.listarOrdenesCompraConDetalles();
        estado.listaProveedores = proveedorService.listarProveedores();
        estado.listaSucursales = sucursalService.listarSucursles();
        estado.listaArticulos = articuloService.listarArticulo();

        guardarEstado(session, nuevoToken, estado);
        cargarDatosParaVista(request, estado, nuevoToken);

        forward(request, response, JSP_FACTURA);
    }

    /**
     * Listar facturas y ordenes para modales de búsqueda
     */
    private void accionListarModal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        List<FacturaCompra> listaFacturas = facturaCompraService.listarFacturasCompra();
        List<OrdenCompra> listaOrdenes = ordenCompraService.listarOrdenesCompraConDetalles();

        request.setAttribute("listaFacturasCompra", listaFacturas);
        request.setAttribute("listaOrdenesCompra", listaOrdenes);

        forward(request, response, JSP_FACTURA);
    }

    /**
     * Cargar factura existente para visualización/edición
     */
    private void accionCargarFactura(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws ServletException, IOException, SQLException {

        Long idFactura = Long.parseLong(request.getParameter("idFactura"));
        String nuevoToken = generarToken();
        FacturaCompraState estado = new FacturaCompraState();

        estado.facturaCompra = facturaCompraService.getFacturaCompra(idFactura);

        if (estado.facturaCompra == null) {
            mostrarMensaje(request, "Factura no encontrada", "alert-danger");
            accionListarModal(request, response);
            return;
        }

        estado.listaDetalle = facturaCompraDetalleService.listarDetallesPorFactura(idFactura);
        estado.proveedorSeleccionado = estado.facturaCompra.getProveedor();
        estado.sucursalSeleccionada = estado.facturaCompra.getSucursal();
        estado.ordenCompraSeleccionada = estado.facturaCompra.getOrdenCompra();
        estado.esNuevo = false;

        // Cargar listas para modales
        estado.listaSucursales = sucursalService.listarSucursles();
        estado.listaArticulos = articuloService.listarArticulo();

        guardarEstado(session, nuevoToken, estado);
        cargarDatosParaVista(request, estado, nuevoToken);

        forward(request, response, JSP_FACTURA);
    }

    /**
     * Cargar datos de orden de compra seleccionada
     */
    private void accionCargarOrdenCompra(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        Long idOrden = Long.parseLong(request.getParameter("idOrden"));
        OrdenCompra ordenCompra = ordenCompraService.getOrdenCompra(idOrden);

        if (ordenCompra != null) {
            estado.ordenCompraSeleccionada = ordenCompra;
            estado.facturaCompra.setOrdenCompra(ordenCompra);
            estado.facturaCompra.setProveedor(ordenCompra.getProveedor());
            estado.facturaCompra.setSucursal(ordenCompra.getSucursal());
            estado.facturaCompra.setCondicion(ordenCompra.getCondicionCompra());
            estado.proveedorSeleccionado = ordenCompra.getProveedor();
            estado.sucursalSeleccionada = ordenCompra.getSucursal();

            // Cargar detalles de la orden como detalles de factura
            List<OrdenCompraDetalle> ordenDetalles = ordenCompraDetalleService.listarDetallesPorOrdenCompra(idOrden);
            estado.listaDetalle.clear();
            for (OrdenCompraDetalle od : ordenDetalles) {
                FacturaCompraDetalle fd = new FacturaCompraDetalle();
                fd.setArticulo(od.getArticulo());
                fd.setCantidad(od.getCantidad());
                fd.setPrecioCompra(od.getPrecioCompra());
                estado.listaDetalle.add(fd);
            }

            mostrarMensaje(request, "Orden de compra cargada correctamente", "alert-success");
        } else {
            mostrarMensaje(request, "No se encontró la orden de compra", "alert-warning");
        }

        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Cargar proveedor seleccionado
     */
    private void accionCargarProveedor(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        Long idProveedor = Long.parseLong(request.getParameter("idProveedor"));
        Proveedor proveedor = proveedorService.getProveedor(idProveedor);

        if (proveedor != null) {
            estado.proveedorSeleccionado = proveedor;
            estado.facturaCompra.setProveedor(proveedor);
            mostrarMensaje(request, "Proveedor seleccionado", "alert-success");
        } else {
            mostrarMensaje(request, "No se encontró el proveedor", "alert-warning");
        }

        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Cambiar sucursal
     */
    private void accionCambiarSucursal(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String idSucursalStr = request.getParameter("idSucursal");
        if (idSucursalStr != null && !idSucursalStr.isEmpty()) {
            Long idSucursal = Long.parseLong(idSucursalStr);
            Sucursal sucursal = sucursalService.getSucursal(idSucursal);
            if (sucursal != null) {
                estado.sucursalSeleccionada = sucursal;
                estado.facturaCompra.setSucursal(sucursal);
            }
        }

        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Cambiar condición de compra
     */
    private void accionCambiarCondicion(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String condicion = request.getParameter("condicion");
        estado.facturaCompra.setCondicion(condicion);

        // Si es contado, limpiar plazo
        if ("Contado".equals(condicion)) {
            estado.facturaCompra.setPlazo(0);
        }

        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Cambiar tipo de factura
     */
    private void accionCambiarTipoFactura(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String tipoFactura = request.getParameter("tipoFactura");
        estado.facturaCompra.setTipoFactura(tipoFactura);

        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Agregar artículo al detalle
     */
    private void accionAgregarArticulo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String idArticuloStr = request.getParameter("idArticulo");
        String cantidadStr = request.getParameter("cantidad");
        String precioStr = request.getParameter("precioCompra");
        String descripcion = request.getParameter("descripcion");

        // Validaciones
        if (idArticuloStr == null || idArticuloStr.isEmpty()) {
            // Si no hay id de articulo, puede ser factura de gasto (solo descripcion)
            if (descripcion != null && !descripcion.isEmpty() && cantidadStr != null && precioStr != null) {
                FacturaCompraDetalle detalle = new FacturaCompraDetalle();
                detalle.setDescripcion(descripcion);
                detalle.setCantidad(Long.parseLong(cantidadStr));
                detalle.setPrecioCompra(Long.parseLong(precioStr));
                estado.listaDetalle.add(detalle);
                mostrarMensaje(request, "Artículo agregado", "alert-success");
            } else {
                mostrarMensaje(request, "Debe completar los datos del artículo", "alert-warning");
            }
        } else {
            Long idArticulo = Long.parseLong(idArticuloStr);

            // Verificar si ya existe el artículo en el detalle
            for (FacturaCompraDetalle det : estado.listaDetalle) {
                if (det.getArticulo() != null && det.getArticulo().getIdArticulo().equals(idArticulo)) {
                    mostrarMensaje(request, "El artículo ya está en la lista", "alert-warning");
                    cargarDatosParaVista(request, estado, token);
                    forward(request, response, JSP_FACTURA);
                    return;
                }
            }

            Articulo articulo = articuloService.getArticulo(idArticulo);
            if (articulo != null && cantidadStr != null && precioStr != null) {
                FacturaCompraDetalle detalle = new FacturaCompraDetalle();
                detalle.setArticulo(articulo);
                detalle.setCantidad(Long.parseLong(cantidadStr));
                detalle.setPrecioCompra(Long.parseLong(precioStr));
                estado.listaDetalle.add(detalle);
                mostrarMensaje(request, "Artículo agregado correctamente", "alert-success");
            } else {
                mostrarMensaje(request, "Datos del artículo inválidos", "alert-warning");
            }
        }

        // Limpiar detalle seleccionado
        estado.detalleSeleccionado = null;

        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Seleccionar artículo para editar
     */
    private void accionEditarArticulo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        String idArticuloStr = request.getParameter("idArticulo");
        int index = Integer.parseInt(request.getParameter("index"));

        if (index >= 0 && index < estado.listaDetalle.size()) {
            estado.detalleSeleccionado = estado.listaDetalle.get(index);
            request.setAttribute("indexSeleccionado", index);
        }

        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Actualizar artículo en el detalle
     */
    private void accionActualizarArticulo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        int index = Integer.parseInt(request.getParameter("index"));
        String cantidadStr = request.getParameter("cantidad");
        String precioStr = request.getParameter("precioCompra");

        if (index >= 0 && index < estado.listaDetalle.size()) {
            FacturaCompraDetalle detalle = estado.listaDetalle.get(index);
            if (cantidadStr != null && !cantidadStr.isEmpty()) {
                detalle.setCantidad(Long.parseLong(cantidadStr));
            }
            if (precioStr != null && !precioStr.isEmpty()) {
                detalle.setPrecioCompra(Long.parseLong(precioStr));
            }
            mostrarMensaje(request, "Artículo actualizado", "alert-success");
        }

        estado.detalleSeleccionado = null;

        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Eliminar artículo del detalle
     */
    private void accionEliminarArticulo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        int index = Integer.parseInt(request.getParameter("index"));

        if (index >= 0 && index < estado.listaDetalle.size()) {
            estado.listaDetalle.remove(index);
            mostrarMensaje(request, "Artículo eliminado", "alert-success");
        }

        estado.detalleSeleccionado = null;

        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Guardar factura en base de datos
     */
    private void accionGuardar(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        // Leer datos del formulario
        String numeroComprobanteStr = request.getParameter("numeroComprobante");
        String timbradoStr = request.getParameter("timbrado");
        String fechaEmisionStr = request.getParameter("fechaEmision");
        String fechaVencTimbradoStr = request.getParameter("fechaVencTimbrado");
        String plazoStr = request.getParameter("plazo");
        String observacion = request.getParameter("observacion");

        // Validaciones
        if (estado.proveedorSeleccionado == null) {
            mostrarMensaje(request, "Debe seleccionar un proveedor", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_FACTURA);
            return;
        }

        if (estado.listaDetalle.isEmpty()) {
            mostrarMensaje(request, "Debe agregar al menos un artículo", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_FACTURA);
            return;
        }

        // Setear datos en el objeto factura
        if (numeroComprobanteStr != null && !numeroComprobanteStr.isEmpty()) {
            try {
                estado.facturaCompra.setNumero(Integer.parseInt(numeroComprobanteStr.replace("-", "")));
            } catch (NumberFormatException e) {
                // Mantener el valor anterior si no se puede parsear
            }
        }

        if (timbradoStr != null && !timbradoStr.isEmpty()) {
            try {
                estado.facturaCompra.setTimbrado(Integer.parseInt(timbradoStr));
            } catch (NumberFormatException e) {
                // Mantener el valor anterior
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (fechaEmisionStr != null && !fechaEmisionStr.isEmpty()) {
                estado.facturaCompra.setFechaEmision(sdf.parse(fechaEmisionStr));
            }
            if (fechaVencTimbradoStr != null && !fechaVencTimbradoStr.isEmpty()) {
                estado.facturaCompra.setFechaVenciTimbrado(sdf.parse(fechaVencTimbradoStr));
            }
        } catch (ParseException e) {
            LOGGER.log(Level.WARNING, "Error al parsear fecha", e);
        }

        if (plazoStr != null && !plazoStr.isEmpty()) {
            try {
                estado.facturaCompra.setPlazo(Integer.parseInt(plazoStr));
            } catch (NumberFormatException e) {
                // Mantener el valor anterior
            }
        }

        if (observacion != null) {
            estado.facturaCompra.setObservacion(observacion);
        }

        // Copiar entidades seleccionadas al objeto factura
        estado.facturaCompra.setProveedor(estado.proveedorSeleccionado);
        estado.facturaCompra.setSucursal(estado.sucursalSeleccionada);
        if (estado.ordenCompraSeleccionada != null) {
            estado.facturaCompra.setOrdenCompra(estado.ordenCompraSeleccionada);
        }

        // Validar campos obligatorios antes de guardar
        if (estado.facturaCompra.getNumero() == null) {
            mostrarMensaje(request, "Debe ingresar el número de comprobante", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_FACTURA);
            return;
        }

        if (estado.sucursalSeleccionada == null) {
            mostrarMensaje(request, "Debe seleccionar una sucursal", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_FACTURA);
            return;
        }

        // Guardar en BD
        if (estado.esNuevo) {
            Long idInsertado = facturaCompraService.insertarFacturaCompra(estado.facturaCompra);

            if (idInsertado != null) {
                // Insertar detalles
                for (FacturaCompraDetalle detalle : estado.listaDetalle) {
                    detalle.setFacturaCompra(new FacturaCompra(idInsertado));
                    facturaCompraDetalleService.insertarDetalle(detalle);
                }

                // Limpiar sesión después de guardar
                limpiarEstado(session, token);

                mostrarMensaje(request, "Factura guardada correctamente. ID: " + idInsertado, "alert-success");
                accionListarModal(request, response);
                return;
            } else {
                mostrarMensaje(request, "Error al guardar la factura", "alert-danger");
            }
        } else {
            // Actualizar factura existente
            facturaCompraService.actualizarFacturaCompra(estado.facturaCompra);
            facturaCompraDetalleService.actualizarDetalles(
                estado.facturaCompra.getIdFacturaCompra(), estado.listaDetalle);

            limpiarEstado(session, token);

            mostrarMensaje(request, "Factura actualizada correctamente", "alert-success");
            accionListarModal(request, response);
            return;
        }

        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Anular factura
     */
    private void accionAnular(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (estado.facturaCompra.getIdFacturaCompra() != null) {
            estado.facturaCompra.setEstado("Anulado");
            facturaCompraService.actualizarFacturaCompra(estado.facturaCompra);

            limpiarEstado(session, token);

            mostrarMensaje(request, "Factura anulada correctamente", "alert-success");
            accionListarModal(request, response);
            return;
        }

        mostrarMensaje(request, "No se puede anular una factura que no ha sido guardada", "alert-warning");
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
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
        return "FacturaCompraServlet - Patrón Session + Token";
    }
}
