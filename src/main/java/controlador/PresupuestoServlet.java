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
        
        if (menu.equals("Presupuesto")) {
            try {
                switch (accion) {
                    case "ListarModal":
                        listaPedidoCompraConDetalle = pedidoCompraService.listarPedidosConDetalles();
                        request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle);
                        presupuestosConDetalle = presupuestoService.listarPresupuestoConDetalles();
                        request.setAttribute("listaPresupuestosConDetalle", presupuestosConDetalle);
                        proveedores = proveedorService.listarProveedors();
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
                                for (PedidoCompraDetalle pedidoCompraDetalle1 : listaPedidoCompraDetalle) {
                                    PresupuestoDetalle presupuestoDetalleAux = new PresupuestoDetalle();
                                    presupuestoDetalleAux.setPresupuesto(presupuesto);
                                    presupuestoDetalleAux.setArticulo(pedidoCompraDetalle1.getArticulo());
                                    presupuestoDetalleAux.setCantidad(pedidoCompraDetalle1.getCantidad());
                                    listaPresupuestoDetalle.add(presupuestoDetalleAux);
                                }
                            if (listaPresupuestoDetalle == null) {
                                mostrarMensaje(request, "No se pudo obtener lista de articulos del pedido seleccionado", "alert-warning");
                                presupuesto = null;
                                presupuestoDetalle = null;
                                request.setAttribute("presupuesto", presupuesto); //envia presupuesto vacio
                                request.getRequestDispatcher("PresupuestoServlet?menu=Presupuesto&accion=ListarModal").forward(request, response);
                                break;
                            }
//                            presupuestoDetalle = null; //vacia presupuseto detalle
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

                                for (PresupuestoDetalle presupuestoDetalle1 : listaPresupuestoDetalle) {
                                    if (presupuestoDetalle1.getCantidad() == null || presupuestoDetalle1.getPrecioCompra() == null) {
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
                                }
                                Long idPresuInserted = null;
//                                Long idPresuInserted = presupuestoService.insertarPresupuesto(
//                                        new Presupuesto(null, presupuesto.getPedidoCompra(),
//                                        presupuesto.getProveedor(), new Date(), "Pendiente", usuario));

                                if (idPresuInserted == null) {
                                    request.setAttribute("Message", "Error al guardar el presupuesto de compra cabecera");
                                    request.setAttribute("tipoAlert", "alert-danger");
                                    LOGGER.log(Level.SEVERE, "Presupuesto de compra no fue insertado correctamente");
                                    request.getRequestDispatcher("presupuesto.jsp").forward(request, response);
                                    break;
                                } else {
                                    presupuesto.setIdPresupuesto(idPresuInserted);
                                }

                                for (PresupuestoDetalle presupuestoDetalle : listaPresupuestoDetalle) {
                                    if (presupuestoDetalle.getPresupuesto() == null) {
                                        mostrarMensaje(request, "Error al guardar presupuesto", "alert-danger");
                                        System.out.println("Error: no se encontro presupuesto asociado al presupuesto detalle");
                                        request.getRequestDispatcher("presupuesto.jsp").forward(request, response);
                                        break;
                                    }
                                    presupuestoDetalleService.insertarDetalle(presupuestoDetalle);
                                }

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
//                                    presupuestoService.actualizarPresupuestoCabecera(presupuesto);
//                                    presupuestoDetalleService.actualizarPresupuestoDetalles(presupuesto.getIdPresupuesto(), listaPresupuestoDetalle);

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
                            if (pedidoCompra == null || listaPedidoCompraDetalle == null || listaPedidoCompraDetalle.isEmpty()) {
                                mostrarMensaje(request, "Datos del pedido incompletos", "alert-warning");
                            } else {
                                pedidoCompra.setEstado("Anulado");
                                pedidoCompraService.actualizarPedidoCabecera(pedidoCompra);
                                mostrarMensaje(request, "Pedido anulado correctamente", "alert-success");
                            }
                        } catch (SQLException e) {
                            request.setAttribute("Message", "Error al actualizar el pedido de compra: " + e.getMessage());
                            request.setAttribute("tipoAlert", "alert-danger");
                        }
                        pedidoCompra = null;
                        listaPedidoCompraDetalle = null;
                        
//                        request.getRequestDispatcher("pedidoCompra.jsp").forward(request, response);
                        request.getRequestDispatcher("PresupuestoServlet?menu=Presupuesto&accion=ListarModal").forward(request, response);
                        
                        break;
                    case "Cancelar":
                        request.getRequestDispatcher("PresupuestoServlet?menu=Presupuesto&accion=ListarModal").forward(request, response);
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
