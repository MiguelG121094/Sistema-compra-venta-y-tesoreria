/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controlador;

import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.TipoArticulo;
import modelo.TipoArticuloDAO;
import service.TipoArticuloService;

/**
 *
 * @author Miguel
 */
@WebServlet(name = "TipoArticuloServlet", urlPatterns = {"/TipoArticuloServlet"})
public class TipoArticuloServlet extends HttpServlet {


    TipoArticulo tipoArticulo = new TipoArticulo();
    TipoArticuloService tipoArticuloService = new TipoArticuloService();
    Long idTipoArt;
    
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
        RequestDispatcher dispatcher;
        
        if (menu.equals("TipoArticulo")) {
            try {
                switch (accion) {
                    case "Listar":
                        List lista = tipoArticuloService.listarTipoArticulo();
                        request.setAttribute("tipoArticulo", lista);
                        request.getRequestDispatcher("tipoArticulo.jsp").forward(request, response);
                        break;
                    case "Insertar":
                        String descripcion = request.getParameter("txtdescripcion");
                        if (descripcion == null || descripcion.trim().isEmpty()) {
                            request.setAttribute("Message", "La descripción no puede estar vacía");
                            request.setAttribute("tipoAlert", "alert-danger");
                        } else {
                            TipoArticulo nuevoTipo = new TipoArticulo();
                            nuevoTipo.setDescripcion(descripcion.trim());
                            tipoArticuloService.insertarTipoArticulo(nuevoTipo);
                            request.setAttribute("Message", "Tipo de Artículo insertado correctamente");
                            request.setAttribute("tipoAlert", "alert-success");
                        }
                        request.getRequestDispatcher("TipoArticuloServlet?menu=TipoArticulo&accion=Listar").forward(request, response);

                        break;
                    case "Editar":
                        try {
                            idTipoArt = Long.parseLong(request.getParameter("id"));
                            TipoArticulo ta = tipoArticuloService.cargarTipoaArticulo(idTipoArt);
                            if (ta != null) {
                                request.setAttribute("tipoArt", ta);
                            } else {
                                request.setAttribute("Message", "No se pudo cargar el Tipo de Artículo");
                                request.setAttribute("tipoAlert", "alert-warning");
                            }
                        } catch (NumberFormatException e) {
                            request.setAttribute("Message", "Id inválido para editar");
                            request.setAttribute("tipoAlert", "alert-danger");
                        }
                        request.getRequestDispatcher("TipoArticuloServlet?menu=TipoArticulo&accion=Listar").forward(request, response);

                        break;
                    case "Actualizar":
                        String descripcionActualizar = request.getParameter("txtdescripcion");

                        if (idTipoArt == null || idTipoArt <= 0) {
                            request.setAttribute("Message", "Debe seleccionar un Tipo de Artículo para actualizar");
                            request.setAttribute("tipoAlert", "alert-warning");
                        } else if (descripcionActualizar == null || descripcionActualizar.trim().isEmpty()) {
                            request.setAttribute("Message", "La descripción no puede estar vacía");
                            request.setAttribute("tipoAlert", "alert-danger");
                        } else {
                            TipoArticulo tipoActualizar = new TipoArticulo();
                            tipoActualizar.setIdTipoArticulo(idTipoArt);
                            tipoActualizar.setDescripcion(descripcionActualizar.trim());
                            tipoArticuloService.actualizarTipoArticulo(tipoActualizar);
                            request.setAttribute("Message", "Tipo de Artículo actualizado correctamente.");
                            request.setAttribute("tipoAlert", "alert-success");
                        }
                        request.getRequestDispatcher("TipoArticuloServlet?menu=TipoArticulo&accion=Listar").forward(request, response);

                        break;
                    case "Eliminar":
                        try {
                            idTipoArt = Long.parseLong(request.getParameter("id"));
                            tipoArticuloService.eliminarTipoArticulo(idTipoArt);
                            request.setAttribute("Message", "Tipo de Artículo eliminado correctamente");
                            request.setAttribute("tipoAlert", "alert-success");
                        } catch (NumberFormatException e) {
                            request.setAttribute("Message", "Id inválido para eliminar");
                            request.setAttribute("tipoAlert", "alert-danger");
                        }
                        request.getRequestDispatcher("TipoArticuloServlet?menu=TipoArticulo&accion=Listar").forward(request, response);
                        
                        break;
                    case "Cancelar":
                        request.getRequestDispatcher("TipoArticuloServlet?menu=TipoArticulo&accion=Listar").forward(request, response);
                        break;
                }
            } catch (Exception e) {
                request.setAttribute("Message", "Ocurrió un error inesperado: " + e.getMessage());
                request.setAttribute("tipoAlert", "alert-danger");
                request.getRequestDispatcher("tipoArticulo.jsp").forward(request, response);
                e.printStackTrace();
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
