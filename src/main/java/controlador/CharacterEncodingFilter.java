package controlador;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * Fuerza UTF-8 en el request y el response para TODAS las peticiones.
 *
 * Sin esto, GlassFish/Payara lee el body de los POST como ISO-8859-1: los acentos
 * enviados desde los formularios (UTF-8) se persisten corruptos (mojibake, p.ej.
 * "devoluciÃÂ³n" en vez de "devolución"). En la salida, garantiza que el HTML
 * (incluido el menu lateral) se escriba siempre en UTF-8.
 *
 * IMPORTANTE: setCharacterEncoding sobre el request solo tiene efecto si se llama
 * ANTES de leer cualquier parametro; por eso el filtro corre para urlPatterns=/*.
 */
@WebFilter(urlPatterns = {"/*"})
public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
