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
    private final TipoImpuestoService tipoImpuestoService = new TipoImpuestoService();
    private final CuentaPagarService cuentaPagarService = new CuentaPagarService();
    private final PedidoCompraDetalleService pedidoCompraDetalleService = new PedidoCompraDetalleService();
    private final NotaCreditoCompraService notaCreditoCompraService = new NotaCreditoCompraService();
    private final NotaDebitoCompraService notaDebitoCompraService = new NotaDebitoCompraService();

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
        Integer indexSeleccionado;
        boolean esNuevo = false;

        // Datos para modales (se cargan una vez)
        List<OrdenCompra> listaOrdenesCompra;
        List<FacturaCompra> listaFacturasCompra;
        List<Proveedor> listaProveedores;
        List<Sucursal> listaSucursales;
        List<Articulo> listaArticulos;
        List<TipoImpuesto> listaTipoImpuesto;
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
        request.setAttribute("indexSeleccionado", estado.indexSeleccionado);
        request.setAttribute("esNuevo", estado.esNuevo);

        // Flags calculados para la vista
        boolean esCredito = "Credito".equals(estado.facturaCompra.getCondicion());
        request.setAttribute("esCredito", esCredito);

        String tipoFactura = estado.facturaCompra.getTipoFactura();
        boolean mostrarAcciones = "gasto".equals(tipoFactura) || "fondoFijo".equals(tipoFactura);
        request.setAttribute("mostrarAcciones", mostrarAcciones);

        // Calcular impuestos y totales para la vista
        calcularImpuestos(request, estado.listaDetalle);

        // Listas para modales
        request.setAttribute("listaOrdenesCompra", estado.listaOrdenesCompra);
        request.setAttribute("listaFacturasCompra", estado.listaFacturasCompra);
        request.setAttribute("listaProveedores", estado.listaProveedores);
        request.setAttribute("listaSucursales", estado.listaSucursales);
        request.setAttribute("listaArticulos", estado.listaArticulos);
        request.setAttribute("listaTipoImpuesto", estado.listaTipoImpuesto);
    }

    /**
     * DTO con los totales de IVA calculados sobre una lista de detalles.
     */
    private static class TotalesIva {
        long total;
        long iva10;
        long iva5;
        long gravada10;
        long gravada5;
        long exenta;
    }

    /**
     * Calcula subtotal/gravada/iva/exenta por cada detalle (mutando los items),
     * y devuelve los totales agregados.
     * Único lugar donde vive la lógica de cálculo de IVA — usado tanto por la
     * vista como por la construcción del libro IVA.
     */
    private TotalesIva calcularTotalesIva(List<FacturaCompraDetalle> listaDetalle) {
        TotalesIva totales = new TotalesIva();

        for (FacturaCompraDetalle detalle : listaDetalle) {
            long subtotal = detalle.getCantidad() * detalle.getPrecioCompra();
            detalle.setSubtotal(subtotal);
            totales.total += subtotal;

            // Reiniciar campos calculados
            detalle.setGravada10(0L);
            detalle.setIva10(0L);
            detalle.setGravada5(0L);
            detalle.setIva5(0L);
            detalle.setExenta(0L);

            // Obtener descripción del impuesto (del artículo o del detalle directo)
            String descImpuesto = "";
            if (detalle.getArticulo() != null && detalle.getArticulo().getTipoImpuesto() != null) {
                descImpuesto = detalle.getArticulo().getTipoImpuesto().getDescripcion();
            } else if (detalle.getTipoImpuesto() != null) {
                descImpuesto = detalle.getTipoImpuesto().getDescripcion();
            }

            if (descImpuesto.contains("10")) {
                long iva = subtotal / 11;
                detalle.setIva10(iva);
                detalle.setGravada10(subtotal - iva);
                totales.iva10 += iva;
                totales.gravada10 += subtotal - iva;
            } else if (descImpuesto.contains("5")) {
                long iva = subtotal / 21;
                detalle.setIva5(iva);
                detalle.setGravada5(subtotal - iva);
                totales.iva5 += iva;
                totales.gravada5 += subtotal - iva;
            } else {
                detalle.setExenta(subtotal);
                totales.exenta += subtotal;
            }
        }

        return totales;
    }

    /**
     * Calcula impuestos y setea los totales como atributos del request para la vista.
     */
    private void calcularImpuestos(HttpServletRequest request, List<FacturaCompraDetalle> listaDetalle) {
        TotalesIva totales = calcularTotalesIva(listaDetalle);
        request.setAttribute("totalGeneral", totales.total);
        request.setAttribute("totalIva10", totales.iva10);
        request.setAttribute("totalIva5", totales.iva5);
        request.setAttribute("totalExenta", totales.exenta);
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

    /**
     * Lee los datos del formulario y los guarda en el estado.
     * Se usa para mantener los datos cuando se hacen cambios parciales (cambiar condición, tipo, etc.)
     */
    private void leerDatosFormulario(HttpServletRequest request, FacturaCompraState estado) {
        String numeroComprobanteStr = request.getParameter("numeroComprobante");
        String timbradoStr = request.getParameter("timbrado");
        String fechaEmisionStr = request.getParameter("fechaEmision");
        String fechaVencTimbradoStr = request.getParameter("fechaVencTimbrado");
        String condicion = request.getParameter("condicion");
        String plazoStr = request.getParameter("plazo");
        String tipoFactura = request.getParameter("tipoFactura");
        String observacion = request.getParameter("observacion");

        // Numero de comprobante (ahora es String, se guarda con formato de máscara)
        if (numeroComprobanteStr != null && !numeroComprobanteStr.isEmpty()) {
            estado.facturaCompra.setNumero(numeroComprobanteStr);
        }

        // Timbrado
        if (timbradoStr != null && !timbradoStr.isEmpty()) {
            try {
                estado.facturaCompra.setTimbrado(Integer.parseInt(timbradoStr));
            } catch (NumberFormatException e) {
                // Ignorar si no se puede parsear
            }
        }

        // Fechas
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (fechaEmisionStr != null && !fechaEmisionStr.isEmpty()) {
                estado.facturaCompra.setFechaEmision(sdf.parse(fechaEmisionStr));
            }
            if (fechaVencTimbradoStr != null && !fechaVencTimbradoStr.isEmpty()) {
                estado.facturaCompra.setFechaVenciTimbrado(sdf.parse(fechaVencTimbradoStr));
            }
        } catch (ParseException e) {
            LOGGER.log(Level.WARNING, "Error al parsear fecha en leerDatosFormulario", e);
        }

        // Condicion
        if (condicion != null && !condicion.isEmpty()) {
            estado.facturaCompra.setCondicion(condicion);
        }

        // Plazo
        if (plazoStr != null && !plazoStr.isEmpty()) {
            try {
                estado.facturaCompra.setPlazo(Integer.parseInt(plazoStr));
            } catch (NumberFormatException e) {
                // Ignorar si no se puede parsear
            }
        }

        // Tipo de factura
        if (tipoFactura != null && !tipoFactura.isEmpty()) {
            estado.facturaCompra.setTipoFactura(tipoFactura);
        }

        // Observacion
        if (observacion != null) {
            estado.facturaCompra.setObservacion(observacion);
        }
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

        // Leer permisos del filter
        Boolean puedeInsertar = (Boolean) request.getAttribute("puedeInsertar");
        Boolean puedeEditar = (Boolean) request.getAttribute("puedeEditar");
        Boolean puedeBorrar = (Boolean) request.getAttribute("puedeBorrar");

        try {
            // Validar permisos para acciones de escritura
            switch (accion) {
                case "Nuevo":
                case "AgregarArticulo":
                case "Guardar":
                    if (puedeInsertar == null || !puedeInsertar) {
                        mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                        accionListarModal(request, response);
                        return;
                    }
                    break;
                case "EditarArticulo":
                case "ActualizarArticulo":
                    if (puedeEditar == null || !puedeEditar) {
                        mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                        accionListarModal(request, response);
                        return;
                    }
                    break;
                case "EliminarArticulo":
                case "Anular":
                    if (puedeBorrar == null || !puedeBorrar) {
                        mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                        accionListarModal(request, response);
                        return;
                    }
                    break;
            }

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
                case "CancelarEdicion":
                    accionCancelarEdicion(request, response, session, token);
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
        estado.listaFacturasCompra = facturaCompraService.listarFacturasCompra();
        estado.listaOrdenesCompra = ordenCompraService.listarOrdenesCompraConDetalles();
        estado.listaProveedores = proveedorService.listarProveedores();
        estado.listaSucursales = sucursalService.listarSucursles();
        estado.listaArticulos = articuloService.listarArticulo();
        estado.listaTipoImpuesto = tipoImpuestoService.listarTipoImpuesto();

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
        estado.listaFacturasCompra = facturaCompraService.listarFacturasCompra();
        estado.listaOrdenesCompra = ordenCompraService.listarOrdenesCompraConDetalles();
        estado.listaSucursales = sucursalService.listarSucursles();
        estado.listaArticulos = articuloService.listarArticulo();
        estado.listaTipoImpuesto = tipoImpuestoService.listarTipoImpuesto();

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

            // Construir mapa articulo -> deposito a partir del Pedido relacionado.
            // El depósito vive en pedido_compra_detalle; presupuesto y orden no lo conservan,
            // así que lo recuperamos del pedido para poblarlo en la factura.
            java.util.Map<Long, Deposito> articuloDeposito = new java.util.HashMap<>();
            if (ordenCompra.getPedidoCompra() != null) {
                List<PedidoCompraDetalle> pedDetalles =
                    pedidoCompraDetalleService.listarDetallesPorPedido(ordenCompra.getPedidoCompra().getIdPedido());
                if (pedDetalles != null) {
                    for (PedidoCompraDetalle pd : pedDetalles) {
                        if (pd.getArticulo() != null && pd.getDeposito() != null) {
                            articuloDeposito.put(pd.getArticulo().getIdArticulo(), pd.getDeposito());
                        }
                    }
                }
            }

            // Cargar detalles de la orden como detalles de factura
            List<OrdenCompraDetalle> ordenDetalles = ordenCompraDetalleService.listarDetallesPorOrdenCompra(idOrden);
            estado.listaDetalle.clear();
            for (OrdenCompraDetalle od : ordenDetalles) {
                FacturaCompraDetalle fd = new FacturaCompraDetalle();
                fd.setArticulo(od.getArticulo());
                fd.setCantidad(od.getCantidad());
                fd.setPrecioCompra(od.getPrecioCompra());
                if (od.getArticulo() != null) {
                    fd.setDeposito(articuloDeposito.get(od.getArticulo().getIdArticulo()));
                }
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

        // Leer todos los datos del formulario para mantenerlos
        leerDatosFormulario(request, estado);

        String idSucursalStr = request.getParameter("idSucursal");
        if (idSucursalStr != null && !idSucursalStr.isEmpty()) {
            Long idSucursal = Long.parseLong(idSucursalStr);
            Sucursal sucursal = sucursalService.getSucursal(idSucursal);
            if (sucursal != null) {
                estado.sucursalSeleccionada = sucursal;
                estado.facturaCompra.setSucursal(sucursal);
            }
        }

        guardarEstado(session, token, estado);
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

        // Leer todos los datos del formulario para mantenerlos
        leerDatosFormulario(request, estado);

        // Si es contado, limpiar plazo
        if ("Contado".equals(estado.facturaCompra.getCondicion())) {
            estado.facturaCompra.setPlazo(0);
        }

        guardarEstado(session, token, estado);
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

        // Leer todos los datos del formulario para mantenerlos
        leerDatosFormulario(request, estado);

        guardarEstado(session, token, estado);
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

        // Leer datos del formulario para mantenerlos (timbrado, fechas, plazo, etc.)
        leerDatosFormulario(request, estado);

        String idArticuloStr = request.getParameter("idArticulo");
        String cantidadStr = request.getParameter("cantidad");
        String precioStr = request.getParameter("precioCompra");
        String descripcion = request.getParameter("descripcion");
        String idTipoImpuestoStr = request.getParameter("idTipoImpuesto");

        // Validaciones
        if (idArticuloStr == null || idArticuloStr.isEmpty()) {
            // Si no hay id de articulo, es factura de gasto/fondo fijo (solo descripcion)
            if (descripcion == null || descripcion.isEmpty()) {
                mostrarMensaje(request, "Debe ingresar una descripción", "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_FACTURA);
                return;
            }
            if (cantidadStr == null || cantidadStr.isEmpty() || precioStr == null || precioStr.isEmpty()) {
                mostrarMensaje(request, "Debe ingresar cantidad y precio", "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_FACTURA);
                return;
            }
            if (idTipoImpuestoStr == null || idTipoImpuestoStr.isEmpty()) {
                mostrarMensaje(request, "Debe seleccionar un tipo de impuesto", "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_FACTURA);
                return;
            }

            FacturaCompraDetalle detalle = new FacturaCompraDetalle();
            detalle.setDescripcion(descripcion);
            detalle.setCantidad(Long.parseLong(cantidadStr));
            detalle.setPrecioCompra(Long.parseLong(precioStr));

            TipoImpuesto tipoImpuesto = tipoImpuestoService.getTipoImpuesto(Long.parseLong(idTipoImpuestoStr));
            detalle.setTipoImpuesto(tipoImpuesto);

            estado.listaDetalle.add(detalle);
            mostrarMensaje(request, "Artículo agregado", "alert-success");
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
        estado.indexSeleccionado = null;

        guardarEstado(session, token, estado);
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

        int index = Integer.parseInt(request.getParameter("index"));

        if (index >= 0 && index < estado.listaDetalle.size()) {
            estado.detalleSeleccionado = estado.listaDetalle.get(index);
            estado.indexSeleccionado = index;
        }

        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Cancelar edición de artículo
     */
    private void accionCancelarEdicion(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        estado.detalleSeleccionado = null;
        estado.indexSeleccionado = null;

        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Actualizar artículo en el detalle
     */
    private void accionActualizarArticulo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        int index = Integer.parseInt(request.getParameter("index"));
        String cantidadStr = request.getParameter("cantidad");
        String precioStr = request.getParameter("precioCompra");
        String descripcion = request.getParameter("descripcion");
        String idTipoImpuestoStr = request.getParameter("idTipoImpuesto");

        if (index >= 0 && index < estado.listaDetalle.size()) {
            FacturaCompraDetalle detalle = estado.listaDetalle.get(index);
            if (cantidadStr != null && !cantidadStr.isEmpty()) {
                detalle.setCantidad(Long.parseLong(cantidadStr));
            }
            if (precioStr != null && !precioStr.isEmpty()) {
                detalle.setPrecioCompra(Long.parseLong(precioStr));
            }
            if (descripcion != null && !descripcion.isEmpty()) {
                detalle.setDescripcion(descripcion);
            }
            if (idTipoImpuestoStr != null && !idTipoImpuestoStr.isEmpty()) {
                TipoImpuesto tipoImpuesto = tipoImpuestoService.getTipoImpuesto(Long.parseLong(idTipoImpuestoStr));
                detalle.setTipoImpuesto(tipoImpuesto);
            }
            mostrarMensaje(request, "Artículo actualizado", "alert-success");
        }

        estado.detalleSeleccionado = null;
        estado.indexSeleccionado = null;

        guardarEstado(session, token, estado);
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
        estado.indexSeleccionado = null;

        guardarEstado(session, token, estado);
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

        // Leer todos los datos del formulario
        leerDatosFormulario(request, estado);

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

        // Cargar entidades seleccionadas al objeto factura
        estado.facturaCompra.setProveedor(estado.proveedorSeleccionado);
        estado.facturaCompra.setSucursal(estado.sucursalSeleccionada);
        if (estado.ordenCompraSeleccionada != null) {
            estado.facturaCompra.setOrdenCompra(estado.ordenCompraSeleccionada);
        }

        // Validar campos obligatorios antes de guardar
        if (estado.facturaCompra.getNumero() == null || estado.facturaCompra.getNumero().isEmpty()) {
            mostrarMensaje(request, "Debe ingresar el número de comprobante", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_FACTURA);
            return;
        }

        if (estado.facturaCompra.getFechaEmision() == null) {
            mostrarMensaje(request, "Debe ingresar la fecha de emisión", "alert-warning");
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

        // Validar que el timbrado esté cargado
        if (estado.facturaCompra.getTimbrado() == null || estado.facturaCompra.getTimbrado() <= 0) {
            mostrarMensaje(request, "Debe ingresar el timbrado", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_FACTURA);
            return;
        }

        // Validar timbrado máximo 8 dígitos
        if (estado.facturaCompra.getTimbrado() > 99999999) {
            mostrarMensaje(request, "El timbrado debe tener un máximo de 8 dígitos", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_FACTURA);
            return;
        }

        // Validar plazo no negativo
        if (estado.facturaCompra.getPlazo() != null && estado.facturaCompra.getPlazo() < 0) {
            mostrarMensaje(request, "El plazo no puede ser negativo", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_FACTURA);
            return;
        }

        // Si es Crédito, el plazo es obligatorio y debe ser mayor a cero
        if ("Credito".equals(estado.facturaCompra.getCondicion())
                && (estado.facturaCompra.getPlazo() == null || estado.facturaCompra.getPlazo() <= 0)) {
            mostrarMensaje(request, "No puede guardar una factura a crédito sin plazo", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_FACTURA);
            return;
        }

        // Validar fecha de vencimiento del timbrado sea mayor o igual a la fecha actual
        if (estado.facturaCompra.getFechaVenciTimbrado() != null) {
            Date fechaActual = new Date();
            // Comparar solo las fechas (sin hora)
            java.util.Calendar calVenc = java.util.Calendar.getInstance();
            calVenc.setTime(estado.facturaCompra.getFechaVenciTimbrado());
            calVenc.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calVenc.set(java.util.Calendar.MINUTE, 0);
            calVenc.set(java.util.Calendar.SECOND, 0);
            calVenc.set(java.util.Calendar.MILLISECOND, 0);

            java.util.Calendar calActual = java.util.Calendar.getInstance();
            calActual.setTime(fechaActual);
            calActual.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calActual.set(java.util.Calendar.MINUTE, 0);
            calActual.set(java.util.Calendar.SECOND, 0);
            calActual.set(java.util.Calendar.MILLISECOND, 0);

            if (calVenc.before(calActual)) {
                mostrarMensaje(request, "La fecha de vencimiento del timbrado debe ser mayor o igual a la fecha actual", "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_FACTURA);
                return;
            }
        }

        // Guardar en BD
        if (estado.esNuevo) {
            // Construir objetos auxiliares para la transacción
            CuentaPagar cuentaPagar = construirCuentaPagar(estado);
            LibroIvaCompra libroIva = construirLibroIvaCompra(estado);

            // Guardar todo en una sola transacción
            Long idInsertado = facturaCompraService.guardarFacturaCompleta(
                estado.facturaCompra, estado.listaDetalle,
                cuentaPagar, libroIva, estado.ordenCompraSeleccionada);

            limpiarEstado(session, token);

            mostrarMensaje(request, "Factura guardada correctamente. ID: " + idInsertado, "alert-success");
            accionListarModal(request, response);
            return;
        } else {
            // Bloquear edición de facturas de compra de artículos: el stock ya fue
            // sumado por trigger al insertar; editar requeriría revertir+reaplicar y
            // genera complejidad innecesaria. Para corregir, anular y crear de nuevo.
            if ("compraArt".equals(estado.facturaCompra.getTipoFactura())) {
                mostrarMensaje(request,
                    "No se puede editar una factura de compra de artículos porque ya afectó al stock. "
                    + "Si necesita corregirla, anúlela y cree una nueva.",
                    "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_FACTURA);
                return;
            }

            // Validar que la cuenta a pagar no tenga pagos aplicados antes de permitir editar.
            // Si los tiene, bloquear la edición: primero se deben reversar los pagos.
            CuentaPagar cuentaPagarActual = cuentaPagarService.getByFactura(estado.facturaCompra.getIdFacturaCompra());
            if (cuentaPagarActual != null && "Anulado".equals(cuentaPagarActual.getEstado())) {
                mostrarMensaje(request,
                    "No se puede editar: la cuenta a pagar asociada está anulada.",
                    "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_FACTURA);
                return;
            }
            if (cuentaPagarService.tienePagosAplicados(estado.facturaCompra.getIdFacturaCompra())) {
                mostrarMensaje(request,
                    "No se puede editar: la factura tiene pagos aplicados. "
                    + "Reverse los pagos desde Orden de Pago antes de editar.",
                    "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_FACTURA);
                return;
            }
            // Con NC/ND, editar la factura desincronizaria/borraria el ajuste de la nota:
            // exigir que primero se anulen las notas activas. Ver NOTA_CREDITO_DEBITO_PLAN.md §8.4.
            if (notaCreditoCompraService.tieneNotaActivaPorFactura(estado.facturaCompra.getIdFacturaCompra())
                    || notaDebitoCompraService.tieneNotaActivaPorFactura(estado.facturaCompra.getIdFacturaCompra())) {
                mostrarMensaje(request,
                    "No se puede editar: la factura tiene notas de crédito/débito activas. "
                    + "Anule primero las notas.",
                    "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_FACTURA);
                return;
            }

            // Reconstruir cuenta a pagar con los nuevos montos/fecha y preservar su id existente
            CuentaPagar cuentaPagarNueva = null;
            if (cuentaPagarActual != null) {
                cuentaPagarNueva = construirCuentaPagar(estado);
                cuentaPagarNueva.setIdCuentaPagar(cuentaPagarActual.getIdCuentaPagar());
            }

            // Actualizar cabecera, detalles y cuenta a pagar en una sola transacción
            facturaCompraService.actualizarFacturaCompleta(
                estado.facturaCompra, estado.listaDetalle, cuentaPagarNueva);

            limpiarEstado(session, token);

            mostrarMensaje(request, "Factura actualizada correctamente", "alert-success");
            accionListarModal(request, response);
            return;
        }
    }

    /**
     * Anular factura
     */
    private void accionAnular(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (estado.facturaCompra.getIdFacturaCompra() != null) {
            // Bloquear la anulación si la factura tiene pagos aplicados (provision/orden de pago).
            // Reemplaza la heuristica saldo < monto, que con NC ya no implica pago (§8.4).
            if (cuentaPagarService.tienePagosAplicados(estado.facturaCompra.getIdFacturaCompra())) {
                mostrarMensaje(request,
                    "No se puede anular: la factura tiene pagos aplicados. "
                    + "Reverse los pagos desde Orden de Pago antes de anular.",
                    "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_FACTURA);
                return;
            }
            // Anular la factura dejaria huerfanas sus notas: exigir anularlas primero.
            if (notaCreditoCompraService.tieneNotaActivaPorFactura(estado.facturaCompra.getIdFacturaCompra())
                    || notaDebitoCompraService.tieneNotaActivaPorFactura(estado.facturaCompra.getIdFacturaCompra())) {
                mostrarMensaje(request,
                    "No se puede anular: la factura tiene notas de crédito/débito activas. "
                    + "Anule primero las notas.",
                    "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_FACTURA);
                return;
            }

            // Anular factura y revertir documentos en una sola transacción
            facturaCompraService.anularFacturaCompleta(
                estado.facturaCompra, estado.ordenCompraSeleccionada);

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

    /**
     * Construye el objeto CuentaPagar con los datos del estado.
     * Solo crea el objeto en memoria, no lo persiste.
     */
    private CuentaPagar construirCuentaPagar(FacturaCompraState estado) {
        Long montoTotal = 0L;
        for (FacturaCompraDetalle detalle : estado.listaDetalle) {
            if (detalle.getCantidad() != null && detalle.getPrecioCompra() != null) {
                montoTotal += detalle.getCantidad() * detalle.getPrecioCompra();
            }
        }

        Date fechaVencimiento;
        if ("Credito".equals(estado.facturaCompra.getCondicion()) && estado.facturaCompra.getPlazo() != null) {
            Date base = estado.facturaCompra.getFechaEmision() != null
                ? estado.facturaCompra.getFechaEmision() : new Date();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(base);
            cal.add(java.util.Calendar.DAY_OF_MONTH, estado.facturaCompra.getPlazo());
            fechaVencimiento = cal.getTime();
        } else {
            // Contado: vencimiento es el momento de carga (tesorería debe procesar el pago ya)
            fechaVencimiento = new Date();
        }

        CuentaPagar cuentaPagar = new CuentaPagar();
        cuentaPagar.setMonto(montoTotal);
        cuentaPagar.setEstado("Pendiente");
        cuentaPagar.setFechaVencimiento(fechaVencimiento);
        cuentaPagar.setSaldo(montoTotal);
        return cuentaPagar;
    }

    /**
     * Construye el objeto LibroIvaCompra con los totales de IVA calculados del detalle.
     * Solo crea el objeto en memoria, no lo persiste.
     */
    private LibroIvaCompra construirLibroIvaCompra(FacturaCompraState estado) {
        TotalesIva totales = calcularTotalesIva(estado.listaDetalle);

        LibroIvaCompra libroIva = new LibroIvaCompra();
        libroIva.setFecha(estado.facturaCompra.getFechaEmision() != null
            ? estado.facturaCompra.getFechaEmision() : new Date());
        libroIva.setIva5(totales.iva5);
        libroIva.setIva10(totales.iva10);
        libroIva.setGravada5(totales.gravada5);
        libroIva.setGravada10(totales.gravada10);
        libroIva.setExenta(totales.exenta);
        libroIva.setTotal(totales.total);
        libroIva.setEstado("Activo");
        return libroIva;
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
