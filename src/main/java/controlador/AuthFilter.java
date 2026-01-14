/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Miguel
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("usuario") != null);
        System.out.println(req.getRequestURI());
        boolean loginRequest = req.getRequestURI().endsWith("login.jsp") || req.getRequestURI().endsWith("LoginServlet"); // Permitir acceso
        boolean registerRequest = req.getRequestURI().endsWith("registrarUsuario.jsp") || req.getRequestURI().endsWith("UsuarioServlet"); // Permitir acceso
        String path = req.getRequestURI(); // Permitir acceso

        //permitir acceso a la pagina de error
        if (path.endsWith("error.jsp")) {
            chain.doFilter(request, response);
            return;
        }

        //permitir acceso a recursos (CSS, JS, imágenes)
        if (path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".jpg")) {
            chain.doFilter(request, response);
            return;
        }

        if (!loggedIn && !loginRequest && !registerRequest) {
            res.sendRedirect("login.jsp");
        } else {
            chain.doFilter(request, response);
        }
    }
}
