package edu.universidad.presta.filter;

import edu.universidad.presta.dao.NotificacionDao;
import edu.universidad.presta.dao.PrestamoDao;
import edu.universidad.presta.dao.SolicitudDao;
import edu.universidad.presta.dao.UsuarioDao;
import edu.universidad.presta.model.Notificacion;
import edu.universidad.presta.model.Usuario;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Autenticación y roles (equivale a requireLogin() / requireRole() de PHP):
 * <ul>
 *   <li>Sin sesión → /login.</li>
 *   <li>Solo admin: /usuarios/*, /equipos/eliminar.</li>
 *   <li>Admin y encargado: /prestamos/*, /reportes, /equipos/crear, /equipos/editar.</li>
 *   <li>Módulos retirados: /reservas/* (redirige al panel para todos los roles).</li>
 *   <li>Cualquier usuario autenticado: el resto.</li>
 * </ul>
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String ctx = req.getContextPath();
        String ruta = rutaCompleta(req);

        if (esPublica(ruta)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession sesion = req.getSession(false);
        Object uid = sesion == null ? null : sesion.getAttribute("usuarioId");
        if (uid == null) {
            res.sendRedirect(ctx + "/login");
            return;
        }

        // El usuario se lee de la base en cada petición: refleja cambios de rol y bajas de cuenta.
        Usuario usuario = UsuarioDao.porId((Integer) uid);
        if (usuario == null || !usuario.isActivo()) {
            sesion.invalidate();
            res.sendRedirect(ctx + "/login");
            return;
        }
        req.setAttribute("usuarioActual", usuario);
        req.setAttribute("esAdmin", usuario.isAdmin());
        req.setAttribute("esEncargado", usuario.isEncargado());
        if (!usuario.isEncargado()) {
            long pendientes = SolicitudDao.deUsuario(usuario.getId()).stream()
                    .filter(s -> "pendiente".equals(s.getEstado())).count();
            req.setAttribute("misSolicitudesPendientes", pendientes);
            req.setAttribute("prestamosUsuarioNotif", PrestamoDao.ultimos(usuario.getId(), 5));
            List<Notificacion> notifs = NotificacionDao.deUsuario(usuario.getId());
            req.setAttribute("notificacionesLista", notifs);
            req.setAttribute("notificacionesNoLeidas", NotificacionDao.contarNoLeidas(notifs));
        }

        boolean permitido;
        if (deshabilitado(ruta)) {
            // Módulo retirado del sistema: /reservas ya no se ofrece a ningún rol.
            res.sendRedirect(ctx + "/dashboard");
            return;
        } else if (soloAdmin(ruta)) {
            permitido = usuario.isAdmin();
        } else if (gestion(ruta)) {
            permitido = usuario.isEncargado();
        } else {
            permitido = true;
        }
        if (!permitido) {
            res.sendRedirect(ctx + "/dashboard");
            return;
        }
        chain.doFilter(request, response);
    }

    private static String rutaCompleta(HttpServletRequest req) {
        String ruta = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        while (ruta.length() > 1 && ruta.endsWith("/")) {
            ruta = ruta.substring(0, ruta.length() - 1);
        }
        return ruta;
    }

    private static boolean esPublica(String ruta) {
        return ruta.equals("/") || ruta.equals("/index.jsp") || ruta.equals("/login") || ruta.equals("/logout") || ruta.equals("/recuperar-password")
                || ruta.startsWith("/assets/");
    }

    private static boolean soloAdmin(String ruta) {
        return ruta.equals("/usuarios") || ruta.startsWith("/usuarios/") || ruta.equals("/equipos/eliminar");
    }

    /** Módulos retirados: el Gestión de Reservas ya no está disponible para ningún rol. */
    private static boolean deshabilitado(String ruta) {
        return ruta.equals("/reservas") || ruta.startsWith("/reservas/");
    }

    private static boolean gestion(String ruta) {
        if (ruta.equals("/prestamos/mis") || ruta.equals("/prestamos/devolver")) {
            return false; // accesible para cualquier usuario autenticado
        }
        return ruta.equals("/prestamos") || ruta.startsWith("/prestamos/")
                || ruta.equals("/reportes") || ruta.startsWith("/reportes/")
                || ruta.equals("/equipos/crear") || ruta.equals("/equipos/editar");
    }
}
