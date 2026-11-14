package edu.universidad.presta.filter;

import edu.universidad.presta.util.Seguridad;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Protección CSRF centralizada (equivale al token de config.php): toda petición POST debe traer el
 * campo csrf_token de la sesión. Los JSP lo incluyen con &lt;t:csrf/&gt;.
 */
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (req.getServletPath().startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession sesion = req.getSession(true);
        req.setAttribute("csrf", Seguridad.token(sesion));

        if ("POST".equalsIgnoreCase(req.getMethod()) && !Seguridad.tokenValido(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Solicitud no válida (token de seguridad incorrecto o expirado). Vuelva atrás, recargue la página e inténtelo de nuevo.");
            return;
        }
        chain.doFilter(request, response);
    }
}
