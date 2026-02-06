/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import modelo.Articulo;
import modelo.OrdenCompra;
import modelo.OrdenCompraDetalle;
import modelo.Presupuesto;
import modelo.PresupuestoDetalle;
import modelo.Proveedor;
import modelo.Sucursal;
import modelo.Usuario;
import service.FacturaCompraService;
import service.OrdenCompraDetalleService;
import service.OrdenCompraService;
import service.PresupuestoDetalleService;
import service.PresupuestoService;
import service.ProveedorService;
import service.SucursalService;
import service.UsuarioService;

/**
 *
 * @author Miguel
 */
@WebServlet(name = "OrdenCompraServlet", urlPatterns = {"/OrdenCompraServlet"})
public class OrdenCompraServlet extends HttpServlet {

    private List<OrdenCompra> ordenesCompraConDetalle;
    private List<Presupuesto> presupuestosConDetalle;
    private Presupuesto presupuesto = new Presupuesto();
    private PresupuestoService presupuestoService = new PresupuestoService();
    private PresupuestoDetalleService presupuestoDetalleService = new PresupuestoDetalleService();
    private List<PresupuestoDetalle> listaPresupuestoDetalle;
    private Usuario usuario = new Usuario();
    private UsuarioService usuarioService = new UsuarioService();
    private Sucursal sucursal;
    private List<Sucursal> listaSucursales = new ArrayList<>();
    private SucursalService sucursalService = new SucursalService();
    private Long newIdOrdenCompra = null;
    private OrdenCompraService ordenCompraService = new OrdenCompraService();
    private OrdenCompraDetalleService ordenCompraDetalleService = new OrdenCompraDetalleService();
    private Proveedor proveedor;
    private ProveedorService proveedorService = new ProveedorService();
    private List<Proveedor> proveedores = new ArrayList<>();
    private OrdenCompra ordenCompra;
    private List<OrdenCompra> ordenesCompra = new ArrayList<OrdenCompra>();
    private List<OrdenCompraDetalle> listaOrdenCompraDetalle;
    private OrdenCompraDetalle ordenCompraDetalle;
    private FacturaCompraService facturaCompraService = new FacturaCompraService();
    private static final Logger LOGGER = Logger.getLogger(OrdenCompraServlet.class.getName());

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String menu = request.getParameter("menu");
        String accion = request.getParameter("accion");
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        HttpSession session = request.getSession(false);

        // Leer permisos del filter
        Boolean puedeInsertar = (Boolean) request.getAttribute("puedeInsertar");
        Boolean puedeEditar = (Boolean) request.getAttribute("puedeEditar");
        Boolean puedeBorrar = (Boolean) request.getAttribute("puedeBorrar");

        if (menu.equals("OrdenCompra")) {
            try {
                // Validar permisos para acciones de escritura
                switch (accion) {
                    case "Nuevo":
                    case "PersistirOrdenCompra":
                        if (puedeInsertar == null || !puedeInsertar) {
                            mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                            request.getRequestDispatcher("OrdenCompraServlet?menu=OrdenCompra&accion=ListarModal").forward(request, response);
                            return;
                        }
                        break;
                    case "EditarArticuloList":
                    case "ModificarArticuloDetalle":
                    case "Aprobar":
                        if (puedeEditar == null || !puedeEditar) {
                            mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                            request.getRequestDispatcher("OrdenCompraServlet?menu=OrdenCompra&accion=ListarModal").forward(request, response);
                            return;
                        }
                        break;
                    case "EliminarArticuloList":
                    case "Anular":
                        if (puedeBorrar == null || !puedeBorrar) {
                            mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                            request.getRequestDispatcher("OrdenCompraServlet?menu=OrdenCompra&accion=ListarModal").forward(request, response);
                            return;
                        }
                        break;
                }

                switch (accion) {
                    case "ListarModal":
                        // Listar presupuestos aprobados para seleccionar
                        presupuestosConDetalle = presupuestoService.listarPresupuestoConDetalles();
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                        // Listar ordenes de compra existentes
                        ordenesCompraConDetalle = ordenCompraService.listarOrdenesCompraConDetalles();
                        request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                        // Listar sucursales
                        listaSucursales = sucursalService.listarSucursles();
                        request.setAttribute("listaSucursales", listaSucursales);

                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                        break;

                    case "Nuevo":
                        usuario = (Usuario) session.getAttribute("usuario");
                        usuario = usuarioService.getUsuario(usuario.getIdUsuario());

                        ordenCompra = new OrdenCompra();
                        newIdOrdenCompra = ordenCompraService.obtenerProximoIdOrdenCompra();
                        ordenCompra.setFecha(new Date());
                        ordenCompra.setEstado("Pendiente");
                        ordenCompra.setUsuario(usuario);

                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                        request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                        request.setAttribute("listaSucursales", listaSucursales);
                        request.setAttribute("ordenCompra", ordenCompra);
                        request.setAttribute("newIdOrdenCompra", newIdOrdenCompra);
                        request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle = null);
                        request.setAttribute("proveedorSeleccionado", proveedor = null);
                        request.setAttribute("sucursalSeleccionada", sucursal = null);

                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                        break;

                    case "CargarPresupuesto":
                        try {
                            listaOrdenCompraDetalle = new ArrayList<OrdenCompraDetalle>();
                            presupuesto = presupuestoService.getPresupuesto(Long.parseLong(request.getParameter("idPresupuestoCab")));

                            if (presupuesto != null) {
                                // Cargar datos del presupuesto a la orden
                                ordenCompra.setPresupuesto(presupuesto);
                                ordenCompra.setPedidoCompra(presupuesto.getPedidoCompra());
                                ordenCompra.setProveedor(presupuesto.getProveedor());
                                ordenCompra.setCondicionCompra(presupuesto.getCondicionCompra());
                                proveedor = presupuesto.getProveedor();

                                // Cargar sucursal desde el pedido de compra
                                if (presupuesto.getPedidoCompra() != null && presupuesto.getPedidoCompra().getSucursal() != null) {
                                    sucursal = presupuesto.getPedidoCompra().getSucursal();
                                    ordenCompra.setSucursal(sucursal);
                                }

                                request.setAttribute("presupuesto", presupuesto);

                                // Cargar detalles del presupuesto a la orden de compra
                                listaPresupuestoDetalle = presupuestoDetalleService.listarDetallesPorPresupuesto(presupuesto.getIdPresupuesto());
                                for (PresupuestoDetalle presupuestoDet : listaPresupuestoDetalle) {
                                    OrdenCompraDetalle ordenCompraDetalleAux = new OrdenCompraDetalle();
                                    ordenCompraDetalleAux.setOrdenCompra(ordenCompra);
                                    ordenCompraDetalleAux.setArticulo(presupuestoDet.getArticulo());
                                    ordenCompraDetalleAux.setCantidad(presupuestoDet.getCantidad());
                                    ordenCompraDetalleAux.setPrecioCompra(presupuestoDet.getPrecioCompra());
                                    listaOrdenCompraDetalle.add(ordenCompraDetalleAux);
                                }

                                if (listaOrdenCompraDetalle == null || listaOrdenCompraDetalle.isEmpty()) {
                                    mostrarMensaje(request, "No se pudo obtener lista de articulos del presupuesto seleccionado", "alert-warning");
                                    ordenCompra = null;
                                    request.setAttribute("ordenCompra", ordenCompra);
                                    request.getRequestDispatcher("OrdenCompraServlet?menu=OrdenCompra&accion=ListarModal").forward(request, response);
                                    break;
                                }

                                request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                                request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                                request.setAttribute("listaSucursales", listaSucursales);
                                request.setAttribute("ordenCompra", ordenCompra);
                                request.setAttribute("newIdOrdenCompra", newIdOrdenCompra);
                                request.setAttribute("proveedorSeleccionado", proveedor);
                                request.setAttribute("sucursalSeleccionada", sucursal);
                                request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle);
                            } else {
                                mostrarMensaje(request, "No se pudo cargar el Presupuesto", "alert-warning");
                            }
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al cargar presupuesto seleccionado", "alert-danger");
                        }

                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                        break;

                    case "CargarSucursal":
                        try {
                            sucursal = sucursalService.getSucursal(Long.parseLong(request.getParameter("idSucursal")));
                            if (sucursal != null) {
                                ordenCompra.setSucursal(sucursal);
                                request.setAttribute("sucursalSeleccionada", sucursal);
                            } else {
                                mostrarMensaje(request, "No se pudo cargar la sucursal seleccionada", "alert-warning");
                            }

                            // Mantener los datos
                            request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                            request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                            request.setAttribute("listaSucursales", listaSucursales);
                            request.setAttribute("ordenCompra", ordenCompra);
                            request.setAttribute("newIdOrdenCompra", newIdOrdenCompra);
                            request.setAttribute("proveedorSeleccionado", proveedor);
                            request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle);
                            request.setAttribute("presupuesto", presupuesto);
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al obtener la sucursal", "alert-danger");
                        }

                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                        break;

                    case "EditarArticuloList":
                        Long idArtSeleccionado = null;
                        ordenCompraDetalle = new OrdenCompraDetalle();
                        try {
                            idArtSeleccionado = Long.parseLong(request.getParameter("idArt"));
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al obtener el articulo", "alert-danger");
                            System.out.println("Error al obtener articulo seleccionado del detalle");
                            break;
                        }
                        for (OrdenCompraDetalle ordenCompraDet : listaOrdenCompraDetalle) {
                            if (ordenCompraDet.getArticulo().getIdArticulo().equals(idArtSeleccionado)) {
                                ordenCompraDetalle.setArticulo(ordenCompraDet.getArticulo());
                                ordenCompraDetalle.setCantidad(ordenCompraDet.getCantidad());
                                ordenCompraDetalle.setPrecioCompra(ordenCompraDet.getPrecioCompra());
                                ordenCompraDetalle.setOrdenCompra(ordenCompraDet.getOrdenCompra());
                            }
                        }
                        request.setAttribute("ordenCompraDetSeleccionado", ordenCompraDetalle);

                        // Mantener los datos
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                        request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                        request.setAttribute("listaSucursales", listaSucursales);
                        request.setAttribute("ordenCompra", ordenCompra);
                        request.setAttribute("newIdOrdenCompra", newIdOrdenCompra);
                        request.setAttribute("proveedorSeleccionado", proveedor);
                        request.setAttribute("sucursalSeleccionada", sucursal);
                        request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle);
                        request.setAttribute("presupuesto", presupuesto);

                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                        break;

                    case "EliminarArticuloList":
                        for (int i = 0; i < listaOrdenCompraDetalle.size(); i++) {
                            if (listaOrdenCompraDetalle.get(i).getArticulo().getIdArticulo().equals(Long.parseLong(request.getParameter("idArt")))) {
                                listaOrdenCompraDetalle.remove(i);
                                break;
                            }
                        }
                        // Mantener los datos
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                        request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                        request.setAttribute("listaSucursales", listaSucursales);
                        request.setAttribute("ordenCompra", ordenCompra);
                        request.setAttribute("newIdOrdenCompra", newIdOrdenCompra);
                        request.setAttribute("proveedorSeleccionado", proveedor);
                        request.setAttribute("sucursalSeleccionada", sucursal);
                        request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle);
                        request.setAttribute("presupuesto", presupuesto);

                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                        break;

                    case "ModificarArticuloDetalle":
                        Long cantidad = null;
                        Long precioCompra = null;
                        Long idArticulo = null;
                        try {
                            cantidad = Long.parseLong(request.getParameter("txtCantidad"));
                            precioCompra = Long.parseLong(request.getParameter("txtPrecioCompra"));
                            idArticulo = Long.parseLong(request.getParameter("idArt"));
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al obtener datos del articulo", "alert-danger");
                            System.out.println("Error al obtener articulo seleccionado del detalle");
                            break;
                        }
                        for (OrdenCompraDetalle ordenCompraDet : listaOrdenCompraDetalle) {
                            if (ordenCompraDet.getArticulo().getIdArticulo().equals(idArticulo)) {
                                ordenCompraDet.setCantidad(cantidad);
                                ordenCompraDet.setPrecioCompra(precioCompra);
                            }
                        }
                        ordenCompraDetalle = null;

                        // Mantener los datos
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                        request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                        request.setAttribute("listaSucursales", listaSucursales);
                        request.setAttribute("ordenCompra", ordenCompra);
                        request.setAttribute("newIdOrdenCompra", newIdOrdenCompra);
                        request.setAttribute("proveedorSeleccionado", proveedor);
                        request.setAttribute("sucursalSeleccionada", sucursal);
                        request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle);
                        request.setAttribute("presupuesto", presupuesto);

                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                        break;

                    case "GuardarCondicionCompra":
                        String condicionCompra = request.getParameter("condicionCompra");
                        if (ordenCompra != null && condicionCompra != null) {
                            ordenCompra.setCondicionCompra(condicionCompra);
                        }

                        // Mantener los datos
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                        request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                        request.setAttribute("listaSucursales", listaSucursales);
                        request.setAttribute("ordenCompra", ordenCompra);
                        request.setAttribute("newIdOrdenCompra", newIdOrdenCompra);
                        request.setAttribute("proveedorSeleccionado", proveedor);
                        request.setAttribute("sucursalSeleccionada", sucursal);
                        request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle);
                        request.setAttribute("presupuesto", presupuesto);

                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                        break;

                    case "GuardarObservacion":
                        String observacion = request.getParameter("txtObservacion");
                        if (ordenCompra != null) {
                            ordenCompra.setObservacion(observacion);
                        }

                        // Mantener los datos
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                        request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                        request.setAttribute("listaSucursales", listaSucursales);
                        request.setAttribute("ordenCompra", ordenCompra);
                        request.setAttribute("newIdOrdenCompra", newIdOrdenCompra);
                        request.setAttribute("proveedorSeleccionado", proveedor);
                        request.setAttribute("sucursalSeleccionada", sucursal);
                        request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle);
                        request.setAttribute("presupuesto", presupuesto);

                        mostrarMensaje(request, "Observacion actualizada", "alert-success");

                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                        break;

                    case "PersistirOrdenCompra":
                        OrdenCompra ordenCompraToPersist = ordenCompraService.getOrdenCompra(ordenCompra.getIdOrdenCompra());
                        if (ordenCompraToPersist == null) {
                            try {
                                // Validaciones
                                if (ordenCompra == null || ordenCompra.getProveedor() == null ||
                                    ordenCompra.getSucursal() == null ||
                                    listaOrdenCompraDetalle == null || listaOrdenCompraDetalle.isEmpty()) {

                                    mostrarMensaje(request, "Datos de la orden de compra incompletos. Verifique proveedor, sucursal y articulos.", "alert-warning");

                                    request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                                    request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                                    request.setAttribute("listaSucursales", listaSucursales);
                                    request.setAttribute("ordenCompra", ordenCompra);
                                    request.setAttribute("newIdOrdenCompra", newIdOrdenCompra);
                                    request.setAttribute("proveedorSeleccionado", proveedor);
                                    request.setAttribute("sucursalSeleccionada", sucursal);
                                    request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle);
                                    request.setAttribute("presupuesto", presupuesto);
                                    request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                                    break;
                                }

                                // Validar detalles
                                for (OrdenCompraDetalle detalle : listaOrdenCompraDetalle) {
                                    if (detalle.getCantidad() == null || detalle.getPrecioCompra() == null) {
                                        mostrarMensaje(request, "Detalle de la orden incompleto. Verifique cantidad y precio.", "alert-warning");
                                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                                        request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                                        request.setAttribute("listaSucursales", listaSucursales);
                                        request.setAttribute("ordenCompra", ordenCompra);
                                        request.setAttribute("newIdOrdenCompra", newIdOrdenCompra);
                                        request.setAttribute("proveedorSeleccionado", proveedor);
                                        request.setAttribute("sucursalSeleccionada", sucursal);
                                        request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle);
                                        request.setAttribute("presupuesto", presupuesto);
                                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                                        break;
                                    }
                                }

                                // Insertar orden de compra cabecera
                                Long idOrdenInserted = ordenCompraService.insertarOrdenCompra(
                                        new OrdenCompra(null, ordenCompra.getPresupuesto(),
                                        ordenCompra.getPedidoCompra(), ordenCompra.getProveedor(),
                                        ordenCompra.getSucursal(), usuario, new Date(), "Pendiente",
                                        ordenCompra.getCondicionCompra(), ordenCompra.getObservacion()));

                                if (idOrdenInserted == null) {
                                    mostrarMensaje(request, "Error al guardar la orden de compra cabecera", "alert-danger");
                                    LOGGER.log(Level.SEVERE, "Orden de compra no fue insertada correctamente");
                                    request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                                    break;
                                } else {
                                    ordenCompra.setIdOrdenCompra(idOrdenInserted);
                                }

                                // Insertar detalles
                                for (OrdenCompraDetalle detalle : listaOrdenCompraDetalle) {
                                    detalle.setOrdenCompra(ordenCompra);
                                    ordenCompraDetalleService.insertarDetalle(detalle);
                                }

                                // Actualizar estado del presupuesto a "Orden Generada"
//                                if (presupuesto != null) {
//                                    presupuesto.setEstado("Orden Generada");
//                                    presupuestoService.actualizarPresupuestoCabecera(presupuesto);
//                                }

                                mostrarMensaje(request, "Orden de compra guardada correctamente", "alert-success");
                                LOGGER.log(Level.INFO, "Orden de compra insertada correctamente con ID: " + idOrdenInserted);

                                // Limpiar datos
                                ordenCompra = null;
                                listaOrdenCompraDetalle = null;
                                presupuesto = null;
                                proveedor = null;
                                sucursal = null;
                                newIdOrdenCompra = null;

                            } catch (Exception e) {
                                mostrarMensaje(request, "Error al guardar la orden de compra: " + e.getMessage(), "alert-danger");
                                LOGGER.log(Level.SEVERE, "Error al insertar orden de compra", e);
                            }
                        } else {
                            // Actualizar orden existente
                            try {
                                if (ordenCompra == null || ordenCompra.getProveedor() == null ||
                                    listaOrdenCompraDetalle == null || listaOrdenCompraDetalle.isEmpty()) {
                                    mostrarMensaje(request, "Datos de la orden de compra incompletos", "alert-warning");
                                } else {
                                    ordenCompraService.actualizarOrdenCompra(ordenCompra);
                                    ordenCompraDetalleService.actualizarDetalles(ordenCompra.getIdOrdenCompra(), listaOrdenCompraDetalle);
                                    mostrarMensaje(request, "Orden de compra actualizada correctamente", "alert-success");
                                }
                            } catch (Exception e) {
                                mostrarMensaje(request, "Error al actualizar la orden de compra: " + e.getMessage(), "alert-danger");
                            }
                        }

                        request.getRequestDispatcher("OrdenCompraServlet?menu=OrdenCompra&accion=ListarModal").forward(request, response);
                        break;

                    case "CargarOrdenCompra":
                        try {
                            Long idOrdenCompra = Long.parseLong(request.getParameter("idOrdenCompra"));
                            ordenCompra = ordenCompraService.getOrdenCompra(idOrdenCompra);

                            if (ordenCompra != null) {
                                proveedor = ordenCompra.getProveedor();
                                sucursal = ordenCompra.getSucursal();
                                presupuesto = ordenCompra.getPresupuesto();
                                listaOrdenCompraDetalle = ordenCompraDetalleService.listarDetallesPorOrdenCompra(idOrdenCompra);

                                // Verificar si tiene factura asociada
                                boolean tieneFactura = facturaCompraService.existeFacturaCompraPorOrden(idOrdenCompra);
                                if (tieneFactura) {
                                    mostrarMensaje(request, "Esta orden tiene Factura asociada. No puede ser modificada.", "alert-warning");
                                    request.setAttribute("esReadOnly", true);
                                }

                                request.setAttribute("ordenCompra", ordenCompra);
                                request.setAttribute("proveedorSeleccionado", proveedor);
                                request.setAttribute("sucursalSeleccionada", sucursal);
                                request.setAttribute("presupuesto", presupuesto);
                                request.setAttribute("listaOrdenCompraDetalle", listaOrdenCompraDetalle);
                            } else {
                                mostrarMensaje(request, "No se pudo cargar la orden de compra", "alert-warning");
                            }

                            request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                            request.setAttribute("listaOrdenesCompraConDetalle", ordenesCompraConDetalle);
                            request.setAttribute("listaSucursales", listaSucursales);

                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al cargar la orden de compra", "alert-danger");
                        }

                        request.getRequestDispatcher("ordenCompra.jsp").forward(request, response);
                        break;

                    case "Aprobar":
                        try {
                            if (ordenCompra != null && ordenCompra.getIdOrdenCompra() != null) {
                                ordenCompra.setEstado("Aprobado");
                                ordenCompraService.actualizarOrdenCompra(ordenCompra);
                                mostrarMensaje(request, "Orden de compra aprobada correctamente", "alert-success");
                            } else {
                                mostrarMensaje(request, "No hay orden de compra para aprobar", "alert-warning");
                            }
                        } catch (SQLException e) {
                            mostrarMensaje(request, "Error al aprobar la orden de compra: " + e.getMessage(), "alert-danger");
                        }

                        ordenCompra = null;
                        listaOrdenCompraDetalle = null;

                        request.getRequestDispatcher("OrdenCompraServlet?menu=OrdenCompra&accion=ListarModal").forward(request, response);
                        break;

                    case "Anular":
                        try {
                            if (ordenCompra != null && ordenCompra.getIdOrdenCompra() != null) {
                                ordenCompra.setEstado("Anulado");
                                ordenCompraService.actualizarOrdenCompra(ordenCompra);
                                mostrarMensaje(request, "Orden de compra anulada correctamente", "alert-success");
                            } else {
                                mostrarMensaje(request, "No hay orden de compra para anular", "alert-warning");
                            }
                        } catch (SQLException e) {
                            mostrarMensaje(request, "Error al anular la orden de compra: " + e.getMessage(), "alert-danger");
                        }

                        ordenCompra = null;
                        listaOrdenCompraDetalle = null;

                        request.getRequestDispatcher("OrdenCompraServlet?menu=OrdenCompra&accion=ListarModal").forward(request, response);
                        break;

                    case "Cancelar":
                        ordenCompra = null;
                        listaOrdenCompraDetalle = null;
                        presupuesto = null;
                        proveedor = null;
                        sucursal = null;
                        newIdOrdenCompra = null;

                        request.getRequestDispatcher("OrdenCompraServlet?menu=OrdenCompra&accion=ListarModal").forward(request, response);
                        break;

                    default:
                        request.getRequestDispatcher("error.jsp").forward(request, response);
                }
            } catch (Exception e) {
                mostrarMensaje(request, "Ocurrio un error inesperado: " + e.getMessage(), "alert-danger");
                request.getRequestDispatcher("MenuPrincipal.jsp").forward(request, response);
                e.printStackTrace();
            }
        }
    }

    /**
     * Metodo para mostrar mensaje en el jsp
     * @param request servlet request
     * @param mensaje Parametro que contiene el mensaje a imprimirse
     * @param tipoAlert Parametro que contiene el tipo de alert al imprimirse el mensaje
     */
    private void mostrarMensaje(HttpServletRequest request, String mensaje, String tipoAlert) {
        request.setAttribute("Message", mensaje);
        request.setAttribute("tipoAlert", tipoAlert);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet para gestionar Ordenes de Compra";
    }// </editor-fold>

}
