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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import modelo.PedidoCompra;
import modelo.PedidoCompraDetalle;
import modelo.Persona;
import modelo.Presupuesto;
import modelo.PresupuestoDetalle;
import modelo.Proveedor;
import modelo.Sucursal;
import modelo.Usuario;
import service.ArticuloService;
import service.DepositoService;
import service.FacturaCompraService;
import service.OrdenCompraService;
import service.PedidoCompraDetalleService;
import service.PedidoCompraService;
import service.PersonaService;
import service.PresupuestoDetalleService;
import service.PresupuestoService;
import service.ProveedorService;
import service.SucursalService;
import service.UsuarioService;

/**
 *
 * @author Miguel
 */
@WebServlet(name = "PresupuestoServlet", urlPatterns = {"/PresupuestoServlet"})
public class PresupuestoServlet extends HttpServlet {

    private List<Presupuesto> presupuestosConDetalle;
    PedidoCompra pedidoCompra = new PedidoCompra();
    PedidoCompraService pedidoCompraService = new PedidoCompraService();
    private Usuario usuario = new Usuario();
    PedidoCompraDetalle pedidoCompraDetalle;
    PedidoCompraDetalleService pedidoCompraDetalleService = new PedidoCompraDetalleService();
    List<PedidoCompraDetalle> listaPedidoCompraDetalle;
    List<PedidoCompra> listaPedidoCompraConDetalle = new ArrayList<PedidoCompra>();
    private UsuarioService usuarioService = new UsuarioService();
    private Persona persona = new Persona();
    private PersonaService personaService = new PersonaService();
    private Sucursal sucursal;
    private Long idSucursal;
    private List<Sucursal> listaSucursales = new ArrayList<>();
    private SucursalService sucursalService = new SucursalService();
    private Deposito deposito;
    private List<Deposito> depositos = new ArrayList<>();
    private DepositoService depositoService = new DepositoService();
    private Articulo articulo = new Articulo();
    private List<Articulo> articulos = new ArrayList<>();
    private ArticuloService articuloService = new ArticuloService();
    private Long newIdPresupuesto = null;
    private PresupuestoService presupuestoService = new PresupuestoService();
    private Proveedor proveedor;
    private ProveedorService proveedorService = new ProveedorService();
    private List<Proveedor> proveedores = new ArrayList<>();
    private Presupuesto presupuesto;
    private List<Presupuesto> presupuestos= new ArrayList<Presupuesto>();
    private List<PresupuestoDetalle> listaPresupuestoDetalle;
    private PresupuestoDetalle presupuestoDetalle;
    private PresupuestoDetalleService presupuestoDetalleService = new PresupuestoDetalleService();
    private OrdenCompraService ordenCompraService = new OrdenCompraService();
    private FacturaCompraService facturaCompraService = new FacturaCompraService();
    private static final Logger LOGGER = Logger.getLogger(PresupuestoServlet.class.getName());
     
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
        HttpSession session = request.getSession(false); //obtener datos de la sesion (se puede obtener el usuario logueado)
        
        // Leer permisos del filter
        Boolean puedeInsertar = (Boolean) request.getAttribute("puedeInsertar");
        Boolean puedeEditar = (Boolean) request.getAttribute("puedeEditar");
        Boolean puedeBorrar = (Boolean) request.getAttribute("puedeBorrar");

        if (menu.equals("Presupuesto")) {
            if (accion == null) {
                accion = "ListarModal";
            }
            try {
                // Validar permisos para acciones de escritura
                switch (accion) {
                    case "Nuevo":
                    case "AgregarArticuloADetalle":
                    case "PersistirPresupuesto":
                        if (puedeInsertar == null || !puedeInsertar) {
                            mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                            request.getRequestDispatcher("PresupuestoServlet?menu=Presupuesto&accion=ListarModal").forward(request, response);
                            return;
                        }
                        break;
                    case "EditarPrecioArticuloList":
                        if (puedeEditar == null || !puedeEditar) {
                            mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                            request.getRequestDispatcher("PresupuestoServlet?menu=Presupuesto&accion=ListarModal").forward(request, response);
                            return;
                        }
                        break;
                    case "EliminarArticuloList":
                    case "Anular":
                        if (puedeBorrar == null || !puedeBorrar) {
                            mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                            request.getRequestDispatcher("PresupuestoServlet?menu=Presupuesto&accion=ListarModal").forward(request, response);
                            return;
                        }
                        break;
                }

                switch (accion) {
                    case "ListarModal":
                        // Usar método que muestra solo artículos pendientes de presupuestar
                        listaPedidoCompraConDetalle = pedidoCompraService.listarPedidosConArticulosPendientes();
                        request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle);
                        presupuestosConDetalle = presupuestoService.listarPresupuestoConDetalles();
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                        proveedores = proveedorService.listarProveedores();
                        request.setAttribute("listaProveedores", proveedores);
                        
                        request.getRequestDispatcher("presupuesto.jsp").forward(request, response);
                        break;
                    case "Nuevo":
                        usuario = (Usuario) session.getAttribute("usuario"); //obtenemos usuario de la sesion
                        usuario = usuarioService.getUsuario(usuario.getIdUsuario());
                        
                        presupuesto = new Presupuesto(); //crea nuevo pedido
                        newIdPresupuesto = presupuestoService.obtenerProximoIdPresupuesto();
                        presupuesto.setFecha(new Date());
                        presupuesto.setEstado("Pendiente");
                        presupuesto.setUsuario(usuario);

                        /* Limpiar TODO el estado del documento anterior. Sin esto, las variables
                           que no se resetean aca reaparecen en la primera accion que las reenvie
                           a la vista (p.ej. GuardarCondicionCompra publica listPedCompDetalle y
                           presupuestoDetSeleccionado): el usuario toca la condicion de compra y
                           le vuelve el pedido/articulo del presupuesto que estaba cargando antes. */
                        listaPedidoCompraDetalle = null;   //detalle del pedido que se habia cargado
                        pedidoCompraDetalle = null;
                        presupuestoDetalle = null;         //articulo abierto en el editor de precio
                        pedidoCompra = new PedidoCompra();
                        request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos Modal
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle); //mantener lista de presupuestos con detalle Modal
                        request.setAttribute("listaProveedores", proveedores); // mantener proveedores Modal
                        request.setAttribute("presupuesto", presupuesto); //enviamos datos del presupuesto nuevo cargado
                        request.setAttribute("newIdPresupuesto", newIdPresupuesto); //enviamos nro nuevo de presupuesto
                        request.setAttribute("listaPresupuestoDetalle", listaPresupuestoDetalle = null); //vacia y envia lista vacia de detalle
                        request.setAttribute("proveedorSeleccionado", proveedor = null); //mantener proveedor seleccionado
                        
                        //enviar mensaje al tooltip (ver para mostrar tooltip sobre boton pedido)
//                        request.setAttribute("mostrarTooltip", true);
//                        request.setAttribute("mensajeTooltip", "Seleccione una sucursal para cargar los artículos");
                        
                        request.getRequestDispatcher("presupuesto.jsp").forward(request, response);
                        break;
                    case "CargarPedidoCompra":
                        try {
                            listaPresupuestoDetalle = new ArrayList<PresupuestoDetalle>();
                            presupuesto.setPedidoCompra(pedidoCompraService.getPedidoCompra(Long.parseLong(request.getParameter("idPedCompraCab"))));
                            if (presupuesto.getPedidoCompra() != null) {
                                request.setAttribute("pedidoCompra", presupuesto.getPedidoCompra());
                                listaPedidoCompraDetalle = pedidoCompraDetalleService.listarDetallesPorPedido(presupuesto.getPedidoCompra().getIdPedido());

                                // Obtener cantidades ya presupuestadas para este pedido
                                Map<Long, Long> cantidadesYaPresupuestadas = presupuestoDetalleService
                                        .obtenerCantidadesPresupuestadasPorPedido(presupuesto.getPedidoCompra().getIdPedido());

                                // Calcular cantidades restantes y solo agregar artículos con cantidad > 0
                                for (PedidoCompraDetalle pedidoCompraDetalle1 : listaPedidoCompraDetalle) {
                                    Long idArticulo = pedidoCompraDetalle1.getArticulo().getIdArticulo();
                                    Long cantidadPedida = pedidoCompraDetalle1.getCantidad();
                                    Long cantidadPresupuestada = cantidadesYaPresupuestadas.getOrDefault(idArticulo, 0L);
                                    Long cantidadRestante = cantidadPedida - cantidadPresupuestada;

                                    // Solo agregar si hay cantidad restante por presupuestar
                                    if (cantidadRestante > 0) {
                                        PresupuestoDetalle presupuestoDetalleAux = new PresupuestoDetalle();
                                        presupuestoDetalleAux.setPresupuesto(presupuesto);
                                        presupuestoDetalleAux.setArticulo(pedidoCompraDetalle1.getArticulo());
                                        presupuestoDetalleAux.setCantidad(cantidadRestante); // Solo la cantidad restante
                                        listaPresupuestoDetalle.add(presupuestoDetalleAux);
                                    }
                                }

                                // Verificar si hay artículos pendientes de presupuestar
                                if (listaPresupuestoDetalle.isEmpty()) {
                                    mostrarMensaje(request, "Este pedido ya tiene todos sus artículos presupuestados", "alert-info");
                                    presupuesto.setPedidoCompra(null);
                                }

                                request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos Modal
                                request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle); //mantener lista de presupuestos con detalle Modal
                                request.setAttribute("listaProveedores", proveedores); // mantener proveedores Modal
                                request.setAttribute("presupuesto", presupuesto); //mantener datos del presupuesto nuevo cargado
                                request.setAttribute("newIdPresupuesto", newIdPresupuesto); //si es presupuesto nuevo que mantenga el nuevo id
                                request.setAttribute("proveedorSeleccionado", proveedor); //mantener proveedor seleccionado
                                request.setAttribute("listaPresupuestoDetalle", listaPresupuestoDetalle); //carga grilla principal con lista de articulos del pedido seleccionado
                            } else {
                                request.setAttribute("Message", "No se pudo cargar el Pedido de compra");
                                request.setAttribute("tipoAlert", "alert-warning");
                            }
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al cargar pedio de compra seleccionado", "alert-danger");
                        }

                        request.getRequestDispatcher("presupuesto.jsp").forward(request, response);

                        break;
                    case "CargarProveedor":
                        try {
                            proveedor = proveedorService.getProveedor(Long.parseLong(request.getParameter("idProvee")));
                            if (proveedor != null) {
                                presupuesto.setProveedor(proveedor);
                                request.setAttribute("proveedorSeleccionado", proveedor);
                            } else {
                                mostrarMensaje(request, "No se pudo cargar el proveedor seleccionado", "alert-warning");
                            }
                        
                            // mantener los datos
                            request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos Modal
                            request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle); //mantener lista de presupuestos con detalle Modal
                            request.setAttribute("listaProveedores", proveedores); // mantener proveedores Modal
                            request.setAttribute("presupuesto", presupuesto); //mantener datos del presupuesto nuevo cargado
                            request.setAttribute("newIdPresupuesto", newIdPresupuesto); //si es presupuesto nuevo que mantenga el nuevo id
                            request.setAttribute("listaPresupuestoDetalle", listaPresupuestoDetalle); //carga grilla principal con lista de articulos del pedido seleccionado
                            request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle); //mantener detallePedidoCompra
                            request.setAttribute("presupuestoDetSeleccionado", presupuestoDetalle); //mantener articulo seleccionado para agregar precio de compra
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al obtener el proveedor", "alert-danger");
                        }
                        
                        request.getRequestDispatcher("presupuesto.jsp").forward(request, response);

                        break;
                    case "GuardarCondicionCompra":
                        String condicionCompraParam = request.getParameter("condicionCompra");
                        if (presupuesto != null && condicionCompraParam != null) {
                            presupuesto.setCondicionCompra(condicionCompraParam);
                        }

                        // mantener los datos
                        request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle);
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                        request.setAttribute("listaProveedores", proveedores);
                        request.setAttribute("proveedorSeleccionado", proveedor);
                        request.setAttribute("presupuesto", presupuesto);
                        request.setAttribute("newIdPresupuesto", newIdPresupuesto);
                        request.setAttribute("listaPresupuestoDetalle", listaPresupuestoDetalle);
                        request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle);
                        request.setAttribute("presupuestoDetSeleccionado", presupuestoDetalle);

                        request.getRequestDispatcher("presupuesto.jsp").forward(request, response);

                        break;
                    case "EditarPrecioArticuloList":
                        Long idArtSeleccionado= null;
                        presupuestoDetalle = new PresupuestoDetalle();
                        try {
                            idArtSeleccionado = Long.parseLong(request.getParameter("idArt"));
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al obtener el articulo", "alert-danger");
                            System.out.println("Error al obtener articulo selecionado del detalle");
                            break;
                        }
                        for (PresupuestoDetalle presupuestoDet : listaPresupuestoDetalle) {
                            if (presupuestoDet.getArticulo().getIdArticulo().equals(idArtSeleccionado)) {
                                presupuestoDetalle.setArticulo(presupuestoDet.getArticulo());
                                presupuestoDetalle.setCantidad(presupuestoDet.getCantidad());
                                presupuestoDetalle.setPrecioCompra(presupuestoDet.getPrecioCompra());
                                presupuestoDetalle.setPresupuesto(presupuestoDet.getPresupuesto());
                                
                            }
                        }
                        request.setAttribute("presupuestoDetSeleccionado", presupuestoDetalle); //cargar articulo seleccionado para agregar precio de compra
                        
                        // mantener los datos
                        request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos Modal
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle); //mantener lista de presupuestos con detalle Modal
                        request.setAttribute("listaProveedores", proveedores); // mantener proveedores Modal
                        request.setAttribute("proveedorSeleccionado", proveedor); //mantener proveedor seleccionado
                        request.setAttribute("presupuesto", presupuesto); //mantener datos del presupuesto nuevo cargado
                        request.setAttribute("newIdPresupuesto", newIdPresupuesto); //si es presupuesto nuevo que mantenga el nuevo id
                        request.setAttribute("listaPresupuestoDetalle", listaPresupuestoDetalle); //carga grilla principal con lista de articulos del pedido seleccionado
                        request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle); //mantener detallePedidoCompra
                        
                        request.getRequestDispatcher("presupuesto.jsp").forward(request, response);

                        break;
                    case "EliminarArticuloList":
                        for (int i = 0; i < listaPresupuestoDetalle.size(); i++) {
                            if (listaPresupuestoDetalle.get(i).getArticulo().getIdArticulo() == (Integer.parseInt(request.getParameter("idArt")))) {
                                listaPresupuestoDetalle.remove(i);
                            }
                        }
                        // mantener los datos
                        request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos Modal
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle); //mantener lista de presupuestos con detalle Modal
                        request.setAttribute("listaProveedores", proveedores); // mantener proveedores Modal
                        request.setAttribute("proveedorSeleccionado", proveedor); //mantener proveedor seleccionado
                        request.setAttribute("presupuesto", presupuesto); //mantener datos del presupuesto nuevo cargado
                        request.setAttribute("newIdPresupuesto", newIdPresupuesto); //si es presupuesto nuevo que mantenga el nuevo id
                        request.setAttribute("listaPresupuestoDetalle", listaPresupuestoDetalle); //carga grilla principal con lista de articulos del pedido seleccionado
                        request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle); //mantener detallePedidoCompra

                        request.getRequestDispatcher("presupuesto.jsp").forward(request, response);
//                        request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);
                    
                    break;
                    case "AgregarArticuloADetalle":
                        Long cantidad = null;
                        Long precioCompra = null;
                        Long idArticulo = null;
                        try {
                            cantidad = Long.parseLong(request.getParameter("txtCantidad"));
                            precioCompra = Long.parseLong(request.getParameter("txtPrecioCompra"));
                            idArticulo = Long.parseLong(request.getParameter("idArt"));
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al obtener el articulo", "alert-danger");
                            System.out.println("Error al obtener articulo selecionado del detalle");
                            break;
                        }
                        for (PresupuestoDetalle presupuestoDet : listaPresupuestoDetalle) {
                            if (idArticulo == presupuestoDet.getArticulo().getIdArticulo()) {
                                if (cantidad > presupuestoDet.getCantidad()) {
                                    mostrarMensaje(request, "El articulo no puede tener una cantidad mayor al pedido", "alert-warning");
                                    System.out.println("Error el articulo tiene una cantidad mayor al pedido");
                                    break;
                                }
                            }
                            
                            if (presupuestoDet.getArticulo().getIdArticulo().equals(presupuestoDetalle.getArticulo().getIdArticulo())) {
                                presupuestoDet.setCantidad(cantidad);
                                presupuestoDet.setPrecioCompra(precioCompra);
                            }
                        }
                        presupuestoDetalle = null;
                        
                        // mantener los datos
                            request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos Modal
                            request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle); //mantener lista de presupuestos con detalle Modal
                            request.setAttribute("listaProveedores", proveedores); // mantener proveedores Modal
                            request.setAttribute("proveedorSeleccionado", proveedor); //mantener proveedor seleccionado
                            request.setAttribute("presupuesto", presupuesto); //mantener datos del presupuesto nuevo cargado
                            request.setAttribute("newIdPresupuesto", newIdPresupuesto); //si es presupuesto nuevo que mantenga el nuevo id
                            request.setAttribute("listaPresupuestoDetalle", listaPresupuestoDetalle); //carga grilla principal con lista de articulos del pedido seleccionado
                            request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle); //mantener detallePedidoCompra
                        
                            request.getRequestDispatcher("presupuesto.jsp").forward(request, response);

                        break;
                    case "PersistirPresupuesto":
                        Presupuesto presupustoToPersist = presupuestoService.getPresupuesto(presupuesto.getIdPresupuesto());
                        if (presupustoToPersist == null) {
                            try {
                                if (presupuesto == null || presupuesto.getProveedor() == null || 
                                    listaPresupuestoDetalle == null || listaPresupuestoDetalle.isEmpty()) {

                                    mostrarMensaje(request, "Datos del presupuesto incompletos", "alert-warning");

                                    // mantener los datos en la vista
                                    request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle);
                                    request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                                    request.setAttribute("listaProveedores", proveedores);
                                    request.setAttribute("proveedorSeleccionado", proveedor);
                                    request.setAttribute("presupuesto", presupuesto);
                                    request.setAttribute("newIdPresupuesto", newIdPresupuesto);
                                    request.setAttribute("listaPresupuestoDetalle", listaPresupuestoDetalle);
                                    request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle);
                                    request.getRequestDispatcher("presupuesto.jsp").forward(request, response);
                                    break;
                                }

                                boolean detalleIncompleto = false;
                                for (PresupuestoDetalle presupuestoDetalle1 : listaPresupuestoDetalle) {
                                    if (presupuestoDetalle1.getCantidad() == null || presupuestoDetalle1.getPrecioCompra() == null) {
                                        detalleIncompleto = true;
                                        break;
                                    }
                                }
                                if (detalleIncompleto) {
                                    mostrarMensaje(request, "Detalle del presupuesto incompleto", "alert-warning");

                                    // mantener datos en la vista
                                    request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle);
                                    request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                                    request.setAttribute("listaProveedores", proveedores);
                                    request.setAttribute("proveedorSeleccionado", proveedor);
                                    request.setAttribute("presupuesto", presupuesto);
                                    request.setAttribute("newIdPresupuesto", newIdPresupuesto);
                                    request.setAttribute("listaPresupuestoDetalle", listaPresupuestoDetalle);
                                    request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle);
                                    request.getRequestDispatcher("presupuesto.jsp").forward(request, response);
                                    break;
                                }

                                // Obtener condición de compra del formulario
                                String condicionCompra = request.getParameter("condicionCompra");

                                // Crear presupuesto con estado Pendiente
                                Presupuesto presupuestoToInsert = new Presupuesto(null, presupuesto.getPedidoCompra(),
                                        presupuesto.getProveedor(), new Date(), "Pendiente", usuario);
                                presupuestoToInsert.setFechaVencimiento(presupuesto.getFechaVencimiento());
                                presupuestoToInsert.setObservacion(presupuesto.getObservacion());
                                presupuestoToInsert.setCondicionCompra(condicionCompra);

                                // Guardar cabecera y detalles en una sola transacción
                                presupuestoService.guardarPresupuestoCompleto(presupuestoToInsert, listaPresupuestoDetalle);

                                mostrarMensaje(request, "Presupuesto guardado correctamente", "alert-success");
                                LOGGER.log(Level.INFO, "Presupuesto de compra insertado correctamente");
                            } catch (Exception e) {
                                request.setAttribute("Message", "Error al guardar el presupuesto: " + e.getMessage());
                                request.setAttribute("tipoAlert", "alert-danger");
                            }
                        } else {
                            try {
                                if (presupuesto == null || presupuesto.getProveedor() == null || listaPresupuestoDetalle == null || listaPresupuestoDetalle.isEmpty()) {
                                    mostrarMensaje(request, "Datos del presupuesto incompletos", "alert-warning");
                                } else {
                                    // Actualizar cabecera y detalles en una sola transacción
                                    presupuestoService.actualizarPresupuestoCompleto(presupuesto, listaPresupuestoDetalle);

                                    mostrarMensaje(request, "Presupuesto actualizado correctamente", "alert-success");
                                }
                            } catch (Exception e) {
                                request.setAttribute("Message", "Error al actualizar el presupuesto: " + e.getMessage());
                                request.setAttribute("tipoAlert", "alert-danger");
                            }
                        }
                        pedidoCompra = null;
                        listaPedidoCompraDetalle = null;
                        
//                        request.getRequestDispatcher("pedidoCompra.jsp").forward(request, response);
                        request.getRequestDispatcher("PresupuestoServlet?menu=Presupuesto&accion=ListarModal").forward(request, response);

                        break;
                    case "Anular":
                        try {
                            if (presupuesto == null || presupuesto.getIdPresupuesto() == null) {
                                mostrarMensaje(request, "Debe seleccionar un presupuesto para anular", "alert-warning");
                            } else {
                                // Validar si el presupuesto tiene documentos asociados (no se puede anular)
                                boolean tieneOrden = ordenCompraService.existeOrdenCompraPorPresupuesto(presupuesto.getIdPresupuesto());
                                boolean tieneFactura = facturaCompraService.existeFacturaCompraPorPresupuesto(presupuesto.getIdPresupuesto());

                                if (tieneOrden || tieneFactura) {
                                    String msg = "Este presupuesto no puede ser anulado porque tiene: ";
                                    if (tieneOrden) msg += "Orden de Compra, ";
                                    if (tieneFactura) msg += "Factura, ";
                                    msg = msg.substring(0, msg.length() - 2) + " asociado(s)";

                                    mostrarMensaje(request, msg, "alert-warning");
                                } else {
                                    presupuesto.setEstado("Anulado");
                                    presupuestoService.actualizarPresupuestoCabecera(presupuesto);
                                    mostrarMensaje(request, "Presupuesto anulado correctamente", "alert-success");
                                }
                            }
                        } catch (SQLException e) {
                            request.setAttribute("Message", "Error al anular el presupuesto: " + e.getMessage());
                            request.setAttribute("tipoAlert", "alert-danger");
                        }
                        presupuesto = null;
                        listaPresupuestoDetalle = null;

                        request.getRequestDispatcher("PresupuestoServlet?menu=Presupuesto&accion=ListarModal").forward(request, response);

                        break;
                    case "Cancelar":
                        request.getRequestDispatcher("PresupuestoServlet?menu=Presupuesto&accion=ListarModal").forward(request, response);
                        break;
                    case "CargarPresupuesto":
                        try {
                            Long idPresupuestoCargar = Long.parseLong(request.getParameter("idPresupuesto"));
                            presupuesto = presupuestoService.getPresupuesto(idPresupuestoCargar);

                            if (presupuesto != null) {
                                // Cargar detalles del presupuesto
                                listaPresupuestoDetalle = presupuestoDetalleService.listarDetallesPorPresupuesto(idPresupuestoCargar);

                                // Cargar el pedido asociado
                                pedidoCompra = presupuesto.getPedidoCompra();

                                // Cargar el proveedor
                                proveedor = presupuesto.getProveedor();

                                // Validar si tiene documentos asociados (solo lectura)
                                boolean tieneOrden = ordenCompraService.existeOrdenCompraPorPresupuesto(idPresupuestoCargar);
                                boolean tieneFactura = facturaCompraService.existeFacturaCompraPorPresupuesto(idPresupuestoCargar);

                                if (tieneOrden || tieneFactura) {
                                    String msg = "Este presupuesto tiene documentos asociados: ";
                                    if (tieneOrden) msg += "Orden de Compra, ";
                                    if (tieneFactura) msg += "Factura, ";
                                    msg = msg.substring(0, msg.length() - 2) + ". No puede ser modificado.";
                                    mostrarMensaje(request, msg, "alert-warning");
                                    request.setAttribute("esReadOnly", true);
                                }

                                request.setAttribute("presupuesto", presupuesto);
                                request.setAttribute("pedidoCompra", pedidoCompra);
                                request.setAttribute("proveedorSeleccionado", proveedor);
                                request.setAttribute("listaPresupuestoDetalle", listaPresupuestoDetalle);
                                request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle);
                                request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                                request.setAttribute("listaProveedores", proveedores);
                            } else {
                                mostrarMensaje(request, "No se encontró el presupuesto seleccionado", "alert-warning");
                            }
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al cargar el presupuesto", "alert-danger");
                            LOGGER.log(Level.SEVERE, "Error al parsear idPresupuesto: " + e.getMessage());
                        }

                        request.getRequestDispatcher("presupuesto.jsp").forward(request, response);

                        break;
                    default:
                        request.getRequestDispatcher("error.jsp").forward(request, response);
                }
            } catch (Exception e) {
                mostrarMensaje(request, "Ocurrió un error inesperado: " + e.getMessage(), "alert-danger");
                request.getRequestDispatcher("MenuPrincipal.jsp").forward(request, response);
                e.printStackTrace();
            }
        }
        
    }
    
    /**
     * Metodo para mostrar mensaje en el jsp
     * @param request servlet request
     * @param mensaje Parametro que contiene el mensaje a imprimirse
     * @param tipoAler Parametro que contiene el tipi de alert al imprimirse el mensaje
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
        return "Short description";
    }// </editor-fold>

}
