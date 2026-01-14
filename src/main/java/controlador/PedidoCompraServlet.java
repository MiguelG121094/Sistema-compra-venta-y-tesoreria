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
import modelo.Sucursal;
import modelo.Usuario;
import service.ArticuloService;
import service.DepositoService;
import service.PedidoCompraDetalleService;
import service.PedidoCompraService;
import service.PersonaService;
import service.SucursalService;
import service.UsuarioService;

/**
 *
 * @author Miguel
 */
@WebServlet(name = "PedidoCompraServlet", urlPatterns = {"/PedidoCompraServlet"})
public class PedidoCompraServlet extends HttpServlet {


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
    private Long newIdPedido = null;
    
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
//        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (menu.equals("PedidoCompra")) {
            try {
                switch (accion) {
                    case "ListarModal":
                        listaPedidoCompraConDetalle = pedidoCompraService.listarPedidosConDetalles();
                        request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle);
                        articulos = articuloService.listarArticulo();
                        request.setAttribute("listaAticulos", articulos);
                        listaSucursales = sucursalService.listarSucursles();
                        request.setAttribute("listaSucursales", listaSucursales);
                        
                        request.getRequestDispatcher("pedidoCompra.jsp").forward(request, response);
                        break;
                    case "Nuevo":
                        usuario = (Usuario) session.getAttribute("usuario"); //obtenemos usuario de la sesion
                        usuario = usuarioService.getUsuario(usuario.getIdUsuario());
                        
                        pedidoCompra = new PedidoCompra(); //crea nuevo pedido
                        newIdPedido = pedidoCompraService.obtenerProximoIdPedidoCompra();
                        pedidoCompra.setFecha(new Date());
                        pedidoCompra.setEstado("Pendiente");
                        pedidoCompra.setUsuario(usuario);
                        request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos
                        request.setAttribute("listaSucursales", listaSucursales); //mantener lista de sucursales
                        request.setAttribute("listaAticulos", articulos); //mantener articulos
                        request.setAttribute("pedidoCompra", pedidoCompra); //enviamos datos del pedido nuevo cargado
                        request.setAttribute("newIdPedido", newIdPedido);
                        listaPedidoCompraDetalle = null; //vacia la lista de pedidoDetalle
                        
                        //enviar mensaje al tooltip
                        request.setAttribute("mostrarTooltip", true);
                        request.setAttribute("mensajeTooltip", "Seleccione una sucursal para cargar los artículos");
                        
                        request.getRequestDispatcher("pedidoCompra.jsp").forward(request, response);
//                        request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);
                        break;
                    case "CargarDeposito":
                        //cargar el combobox de deposito en base a la sucursal seleccionada
                        idSucursal = null;
                        try {
                            idSucursal = Long.parseLong(request.getParameter("idSucursal"));
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Debe seleccionar una sucursal", "alert-warning");
                            System.out.println("Sucursal no seleccionada");
                        }
                        //obtenemos los depositos de la sucursal seleccionada
                        if (idSucursal != null) {
                            request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos
                            request.setAttribute("listaSucursales", listaSucursales); //mantener lista de sucursales
                            request.setAttribute("newIdPedido", newIdPedido); //si es pedido nuevo que mantenga el nuevo id
                            sucursal = sucursalService.getSucursal(idSucursal); //obtenemos la sucursal
                            pedidoCompra.setSucursal(sucursal); //cargamos la sucursal al pedido
                            depositos = depositoService.listarDepostioPorSucursal(idSucursal); //cargamos los depositos de la sucursal
                            request.setAttribute("listaAticulos", articulos); //mantener articulos
                            request.setAttribute("listaDepositos", depositos);
                        } else{
                            mostrarMensaje(request, "Debe seleccionar una sucursal", "alert-warning");
                            request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);
                            break;
                        }
                            // mantener datos
                            request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos
                            request.setAttribute("listaAticulos", articulos); //mantener articulos
                            request.setAttribute("pedidoCompra", pedidoCompra); //mantener datos del pedido
                            request.setAttribute("newIdPedido", newIdPedido); //si es pedido nuevo que mantenga el nuevo id
                            request.setAttribute("idSucursalSeleccionada", pedidoCompra.getSucursal().getIdSucursal()); //mantener sucursal seleccionada
                            request.setAttribute("listaSucursales", listaSucursales); // mantener las sucursales
                            request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle); //mantener detallePedidoCompra
                        
                            request.getRequestDispatcher("pedidoCompra.jsp").forward(request, response);
//                        request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);
                        break;
                    case "CargarArticulo":
                        try {
                            articulo = articuloService.getArticulo(Long.parseLong(request.getParameter("idArt")));
                            if (articulo != null) {
                                request.setAttribute("artACargar", articulo);
                            } else {
                                mostrarMensaje(request, "No se pudo cargar el artículo", "alert-warning");
                            }
                        
                            // mantener los datos
                            request.setAttribute("listaAticulos", articulos); //mantener articulos
                            request.setAttribute("pedidoCompra", pedidoCompra); //mantener datos del pedido
                            request.setAttribute("newIdPedido", newIdPedido); //si es pedido nuevo que mantenga el nuevo id
                            request.setAttribute("idSucursalSeleccionada", pedidoCompra.getSucursal().getIdSucursal()); //mantener sucursal seleccionada
                            request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos
                            request.setAttribute("listaSucursales", listaSucursales); //mantener lista de sucursales
                            request.setAttribute("listaDepositos", depositos); //mantener lista de depositos
                            request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle); //mantener detallePedidoCompra
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al obtener el articulo", "alert-danger");
                        }
                        
                        request.getRequestDispatcher("pedidoCompra.jsp").forward(request, response);
//                        request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);

                        break;
                    case "AgregarArticuloADetalle":
                        //imprimir parametros recibidos desde el jsp
//                        System.out.println("Parámetros recibidos:");
//                        Enumeration<String> parametros = request.getParameterNames();
//                        while (parametros.hasMoreElements()) {
//                            String nombreParametro = parametros.nextElement();
//                            String valorParametro = request.getParameter(nombreParametro);
//                            System.out.println(nombreParametro + " = " + valorParametro);
//                        }
                        
                        Long idDeposito= null;
                        try {
                            idDeposito = Long.parseLong(request.getParameter("idDeposito"));
                            deposito = depositoService.getDeposito(idDeposito);
                        } catch (NumberFormatException e) {
                            System.out.println("Deposito no seleccionado");
                        }
                        if (listaPedidoCompraDetalle == null) {
                             listaPedidoCompraDetalle = new ArrayList<>();
                        }

                        pedidoCompraDetalle = new PedidoCompraDetalle();
                        pedidoCompraDetalle.setPedido(pedidoCompra);
                        pedidoCompraDetalle.setArticulo(articulo);
                        pedidoCompraDetalle.setDeposito(deposito);
                        pedidoCompraDetalle.setCantidad(Long.parseLong(request.getParameter("txtCantidad")));
                        
                        //si el articulo ya existe en la lista sumamos sus cantidades
                        //usamos forEach() con Iterator para evitar errores de concurrencia
                        //para evitar ConcurrentModificationException al modificar la lista
                        Iterator<PedidoCompraDetalle> iterator = listaPedidoCompraDetalle.iterator();
                        while (iterator.hasNext()) {
                            PedidoCompraDetalle pedidoCompDetalle = iterator.next();
                            if (pedidoCompDetalle.getArticulo().getIdArticulo().equals(pedidoCompraDetalle.getArticulo().getIdArticulo())) {
                                pedidoCompraDetalle.setCantidad(pedidoCompDetalle.getCantidad() + pedidoCompraDetalle.getCantidad());
                                iterator.remove();
                            }
                        }
//                        for (int i = 0; i < listaPedidoCompraDetalle.size(); i++) {
//                            PedidoCompraDetalle pedidoCompDetalle = listaPedidoCompraDetalle.get(i);
//                            if (pedidoCompDetalle.getArticulo().getIdArticulo().equals(pedidoCompraDetalle.getArticulo().getIdArticulo())) {
//                                pedidoCompraDetalle.setCantidad(pedidoCompDetalle.getCantidad() + pedidoCompraDetalle.getCantidad());
//                                listaPedidoCompraDetalle.remove(i);
//                            }
//                        }
                        
                        listaPedidoCompraDetalle.add(pedidoCompraDetalle);
                        
                        request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle);
                        // mantener los datos
                        request.setAttribute("listaAticulos", articulos); //mantener articulos
                        request.setAttribute("pedidoCompra", pedidoCompra);//mantener datos del pedido
                        request.setAttribute("newIdPedido", newIdPedido); //si es pedido nuevo que mantenga el nuevo id
                        request.setAttribute("idSucursalSeleccionada", pedidoCompra.getSucursal().getIdSucursal()); //mantener sucursal seleccionada
                        request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos
                        request.setAttribute("listaSucursales", listaSucursales); //mantener lista de sucursales
                        request.setAttribute("listaDepositos", depositos); //mantener lista de depositos
                        
                        request.getRequestDispatcher("pedidoCompra.jsp").forward(request, response);
//                        request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);

                        break;
                    case "EliminarArticuloList":;
                        for (int i = 0; i < listaPedidoCompraDetalle.size(); i++) {
                            if (listaPedidoCompraDetalle.get(i).getArticulo().getIdArticulo() == (Integer.parseInt(request.getParameter("idArt")))) {
                                listaPedidoCompraDetalle.remove(i);
                            }
                        }
                        // mantener los datos
                        request.setAttribute("listaAticulos", articulos); //mantener articulos
                        request.setAttribute("pedidoCompra", pedidoCompra); //mantener datos del pedido
                        request.setAttribute("newIdPedido", newIdPedido); //si es pedido nuevo que mantenga el nuevo id
                        request.setAttribute("idSucursalSeleccionada", pedidoCompra.getSucursal().getIdSucursal()); //mantener sucursal seleccionada
                        request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos
                        request.setAttribute("listaSucursales", listaSucursales); //mantener lista de sucursales
                        request.setAttribute("listaDepositos", depositos); //mantener lista de depositos
                        request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle); //mantener lista
                        
                        request.getRequestDispatcher("pedidoCompra.jsp").forward(request, response);
//                        request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);
                    
                    break;
                    case "CargarPedidoCompra":
                        try {
                            listaPedidoCompraDetalle = null;
                            pedidoCompra = pedidoCompraService.getPedidoCompra(Long.parseLong(request.getParameter("idPedCompraCab")));
                            if (pedidoCompra != null) {
                                listaPedidoCompraDetalle = pedidoCompraDetalleService.listarDetallesPorPedido(pedidoCompra.getIdPedido());
                                if (listaPedidoCompraDetalle == null) {
                                    mostrarMensaje(request, "No se pudo obtener lista de articulos del pedido seleccionado", "alert-warning");
                                    pedidoCompra = null;
                                    request.setAttribute("pedidoCompra", pedidoCompra); //envia pedido de compra vacio
                                    request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);
                                    break;
                                }
                                request.setAttribute("pedidoCompra", pedidoCompra);
                                request.setAttribute("listaAticulos", articulos); //mantener articulos
                                request.setAttribute("idSucursalSeleccionada", pedidoCompra.getSucursal().getIdSucursal()); //carga la sucursal seleccioanda del pedido
                                request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos
                                request.setAttribute("listaSucursales", listaSucursales); //mantener lista de sucursales
                                depositos = depositoService.listarDepostioPorSucursal(pedidoCompra.getSucursal().getIdSucursal()); //cargamos los depositos de la sucursal
                                request.setAttribute("listaDepositos", depositos); //mantener lista de depositos
                                request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle); //cargar lista de pedidodetalle del pedido seleccionado
                            } else {
                                request.setAttribute("Message", "No se pudo cargar el Pedido de compra");
                                request.setAttribute("tipoAlert", "alert-warning");
                            }
                        } catch (NumberFormatException e) {
                            mostrarMensaje(request, "Error al cargar pedio de compra", "alert-danger");
                        }
                        
                        request.getRequestDispatcher("pedidoCompra.jsp").forward(request, response);
//                        request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);

                        break;
                    case "PersistirPedido":
                        PedidoCompra pedidoComp = pedidoCompraService.getPedidoCompra(pedidoCompra.getIdPedido());
                        if (pedidoComp == null) {
                            try {
                                if (pedidoCompra == null || listaPedidoCompraDetalle == null || listaPedidoCompraDetalle.isEmpty()) {
                                    for (PedidoCompraDetalle pedidoCompraDetalle1 : listaPedidoCompraDetalle) {
//                                        pedidoCompraDetalle1.getCantidad() == null || pedidoCompraDetalle1.getCantidad()
                                    }
                                    request.setAttribute("newIdPedido", newIdPedido);
                                    request.setAttribute("pedidoCompra", pedidoCompra);
                                    request.setAttribute("listaAticulos", articulos); //mantener articulos
                                    if (pedidoCompra.getSucursal() != null) {
                                        request.setAttribute("idSucursalSeleccionada", pedidoCompra.getSucursal().getIdSucursal()); //carga la sucursal seleccioanda del pedido
                                        depositos = depositoService.listarDepostioPorSucursal(pedidoCompra.getSucursal().getIdSucursal()); //cargamos los depositos de la sucursal
                                        request.setAttribute("listaDepositos", depositos); //mantener lista de depositos
                                    }
                                    request.setAttribute("listPedCompraConDetalle", listaPedidoCompraConDetalle); //mantener lista de pedidos
                                    request.setAttribute("listaSucursales", listaSucursales); //mantener lista de sucursales
                                    request.setAttribute("listPedCompDetalle", listaPedidoCompraDetalle); //cargar lista de pedidodetalle del pedido seleccionado
                                    mostrarMensaje(request, "Datos del pedido incompletos", "alert-warning");
                                } else {
                                    Long idPedCabInserted = pedidoCompraService.insertarPedido(new PedidoCompra(null, usuario, sucursal, new Date(), "Pendiente de compra"));
                                    if (idPedCabInserted == null) {
                                        request.setAttribute("Message", "Error al guardar el pedido de compra cabecera");
                                        request.setAttribute("tipoAlert", "alert-danger");
                                        break;
                                    }else{
                                        pedidoCompra.setIdPedido(idPedCabInserted);
                                    }
                                    for (PedidoCompraDetalle pedidoCompraDetalle1 : listaPedidoCompraDetalle) {
                                        pedidoCompraDetalleService.insertarDetalle(pedidoCompraDetalle1);
                                    }
                                    
                                    //insertar pedidoCompra y pedidoCompraDetalle en una misma transaccion
//                                    pedidoCompraService.insertarPedidoCabeceraYDetalle(new PedidoCompra
//                                        (null, usuario, sucursal, new Date(), "Pendiente de compra"), listaPedidoCompraDetalle);
                                    
                                    pedidoCompra = null;
                                    listaPedidoCompraDetalle = null;
                                    
                                    mostrarMensaje(request, "Pedido de compra guardado correctamente", "alert-success");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                request.setAttribute("Message", "Error al guardar el pedido de compra");
                                request.setAttribute("tipoAlert", "alert-danger");
                            }
                        } else {
                            try {
                                if (pedidoCompra == null || listaPedidoCompraDetalle == null || listaPedidoCompraDetalle.isEmpty()) {
                                    mostrarMensaje(request, "Datos del pedido incompletos", "alert-warning");
                                } else {
                                    pedidoCompraService.actualizarPedidoCabecera(pedidoCompra);
                                    pedidoCompraDetalleService.actualizarPedidoDetalles(pedidoCompra.getIdPedido(), listaPedidoCompraDetalle);

                                    mostrarMensaje(request, "Pedido actualizado correctamente", "alert-success");
                                }
                                
                                pedidoCompra = null;
                                listaPedidoCompraDetalle = null;
                            } catch (SQLException e) {
                                request.setAttribute("Message", "Error al actualizar el pedido de compra: " + e.getMessage());
                                request.setAttribute("tipoAlert", "alert-danger");
                            }
                        }
                        
//                        request.getRequestDispatcher("pedidoCompra.jsp").forward(request, response);
                        request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);

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
                        request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);
                        
                        break;
                    case "Cancelar":
                        request.getRequestDispatcher("PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal").forward(request, response);
                        break;
                    default:
                        request.getRequestDispatcher("error.jsp").forward(request, response);
                }
            } catch (Exception e) {
                mostrarMensaje(request, "Ocurrió un error inesperado", "alert-danger");
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
