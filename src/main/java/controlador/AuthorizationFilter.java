package controlador;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import modelo.Permiso;

@WebFilter(urlPatterns = {"/FacturaCompraServlet", "/PedidoCompraServlet", "/PresupuestoServlet", "/OrdenCompraServlet", "/NotaCreditoDebitoServlet", "/CuentaServlet"})
public class AuthorizationFilter implements Filter {

    private static final Map<String, String> URL_MODULO = new HashMap<>();
    static {
        URL_MODULO.put("FacturaCompraServlet", "compra");
        URL_MODULO.put("PedidoCompraServlet", "compra");
        URL_MODULO.put("PresupuestoServlet", "compra");
        URL_MODULO.put("OrdenCompraServlet", "compra");
        URL_MODULO.put("NotaCreditoDebitoServlet", "compra");
        URL_MODULO.put("CuentaServlet", "tesoreria");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        // Si no hay sesión, dejar que AuthFilter maneje la redirección al login
        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        // Obtener el nombre del servlet desde la URL
        String uri = req.getRequestURI();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);

        // Si la URL no está en el mapeo, no aplicar restricción
        String modulo = URL_MODULO.get(servletName);
        if (modulo == null) {
            chain.doFilter(request, response);
            return;
        }

        // Obtener mapa de permisos de la sesión
        @SuppressWarnings("unchecked")
        Map<String, Permiso> permisos = (Map<String, Permiso>) session.getAttribute("permisos");

        // Si no hay permisos cargados (sesión sin permisos), dejar pasar
        if (permisos == null) {
            chain.doFilter(request, response);
            return;
        }

        Permiso permiso = permisos.get(modulo);

        // Si no existe permiso para el módulo, denegar acceso
        if (permiso == null || !Boolean.TRUE.equals(permiso.getLeer())) {
            req.setAttribute("Message", "No tiene permisos para acceder a este módulo");
            req.setAttribute("tipoAlert", "alert-danger");
            req.getRequestDispatcher("MenuPrincipal.jsp").forward(request, response);
            return;
        }

        // Setear flags de permisos como atributos del request para el servlet y JSP
        req.setAttribute("puedeInsertar", Boolean.TRUE.equals(permiso.getInsertar()));
        req.setAttribute("puedeEditar", Boolean.TRUE.equals(permiso.getEditar()));
        req.setAttribute("puedeBorrar", Boolean.TRUE.equals(permiso.getBorrar()));

        chain.doFilter(request, response);
    }
}
