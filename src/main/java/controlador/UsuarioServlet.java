/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.Usuario;
import modelo.UsuarioDAO;
import service.UsuarioService;

/**
 *
 * @author Miguel
 */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/UsuarioServlet"})
public class UsuarioServlet extends HttpServlet {

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
        
        Usuario usuario = new Usuario();
//        UsuarioService usuarioService = new UsuarioService();
        
        
        if (menu.equals("Usuario")) {
            switch (accion) {
                case "Listar":
//                    List lista = cdao.listar();
//                    request.setAttribute("usuario", lista);
                    break;
                case "Registrar":
//                    usuario.setIdPersona((long)6);
//                    usuario.setUser((String)request.getParameter("user"));
//                    usuario.setPass((String)request.getParameter("pass"));
//                    usuario.setEstado("inactivo");
//                    usuario.setIdGrupo((long) 2);
//                    usuarioDAO.insertarUsuario(usuario);
//
//                    request.setAttribute("Message", "Usuario registrado, debe esperar a que un administrador lo habilite");
//                    request.setAttribute("tipoAlert", "alert-warning");
//                    RequestDispatcher dispatcher = request.getRequestDispatcher("login.jsp");
//                    dispatcher.forward(request, response);

                    break;
                case "Editar":
//                    id_c = Integer.parseInt(request.getParameter("id"));
//                    Cliente cl = cdao.listar_id(id_c);
//                    request.setAttribute("cli", cl);
//
//                    request.getRequestDispatcher("Controlador?menu=Cliente&accion=Listar").forward(request, response);

                    break;
                case "Actualizar":
//                    String nombre1 = request.getParameter("txtnombre");
//                    String apellido1 = request.getParameter("txtapellido");
//                    int cedula1 = Integer.parseInt(request.getParameter("txtcedula"));
//                    String direccion1 = request.getParameter("txtdireccion");
//                    String telefono1 = request.getParameter("txttelefono");
//                    String ruc1 = request.getParameter("txtruc");
//                    c.setNombre(nombre1);
//                    c.setApellido(apellido1);
//                    c.setCi(cedula1);
//                    c.setDireccion(direccion1);
//                    c.setTelefono(telefono1);
//                    c.setRuc(ruc1);
//                    c.setId(id_c);
//                    cdao.editar(c);
//                    request.setAttribute("mensaje", 0);
//                    request.getRequestDispatcher("Controlador?menu=Cliente&accion=Listar").forward(request, response);

                    break;
                case "Eliminar":
//                    id_c = Integer.parseInt(request.getParameter("id"));
//                    cdao.borrar(id_c);
//                    request.setAttribute("mensaje", 1);
//                    request.getRequestDispatcher("Controlador?menu=Cliente&accion=Listar").forward(request, response);
                    break;
                case "Cancelar":
//                    request.getRequestDispatcher("Controlador?menu=Cliente&accion=Listar").forward(request, response);

                    break;
            }
        }
        
        
        
        
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
