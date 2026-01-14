/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import modelo.Usuario;
import modelo.UsuarioDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import service.UsuarioService;

/**
 *
 * @author Miguel
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("user");
        String password = request.getParameter("pass");
        String recordar = request.getParameter("rememberMe");

        UsuarioService usuarioService = new UsuarioService();
        Usuario usuario = null;
        try {
            usuario = usuarioService.validarUsuario(username, password);
        } catch (SQLException ex) {
            Logger.getLogger(LoginServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (usuario != null) {
            if (usuario.getEstado().equalsIgnoreCase("activo")){
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);
                
            // manejo de cookies
            if (recordar != null) { // si el usuario marcó "Recordar"
                Cookie userCookie = new Cookie("user", username);
                Cookie passCookie = new Cookie("pass", password);
                Cookie remeberCookie = new Cookie("rememberMe", "on");
                userCookie.setMaxAge(60 * 60 * 24 * 7);  // 7 días
                passCookie.setMaxAge(60 * 60 * 24 * 7);  // 7 días
                remeberCookie.setMaxAge(60 * 60 * 24 * 7);  // 7 días
                response.addCookie(userCookie);
                response.addCookie(passCookie);
                response.addCookie(remeberCookie);
            } else { // si no marcó "Recordar", eliminar cookies
                Cookie userCookie = new Cookie("user", "");
                Cookie passCookie = new Cookie("pass", "");
                Cookie remeberCookie = new Cookie("rememberMe", "");
                userCookie.setMaxAge(0);
                passCookie.setMaxAge(0);
                remeberCookie.setMaxAge(0);
                response.addCookie(userCookie);
                response.addCookie(passCookie);
                response.addCookie(remeberCookie);
            }
                
                response.sendRedirect("MenuPrincipal.jsp"); // Redirige a la página principal
            }else{
                request.setAttribute("Message", "Usuario no encuentra activo");
                request.setAttribute("tipoAlert", "alert-warning");
                RequestDispatcher dispatcher = request.getRequestDispatcher("login.jsp");
                dispatcher.forward(request, response);
            }
        } else {
            request.setAttribute("Message", "Usuario o contraseña incorrectos");
            request.setAttribute("tipoAlert", "alert-danger");
            RequestDispatcher dispatcher = request.getRequestDispatcher("login.jsp");
            dispatcher.forward(request, response);
        }
    }
}
