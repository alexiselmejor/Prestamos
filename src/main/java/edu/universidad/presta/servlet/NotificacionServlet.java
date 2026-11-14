package edu.universidad.presta.servlet;

import edu.universidad.presta.dao.Db;
import edu.universidad.presta.dao.NotificacionDao;
import edu.universidad.presta.model.Notificacion;
import edu.universidad.presta.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Campana de la cabecera: lista completa de avisos y acción "Marcar todas como leídas".
 * La campana desplegable de la cabecera usa esta misma acción por AJAX.
 */
@WebServlet(urlPatterns = {"/notificaciones", "/notificaciones/*"})
public class NotificacionServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if ("/ir".equals(subRuta(req, ""))) {
            marcarYRedirigir(req, res);
            return;
        }
        if (!esEncargado(req)) {
            redirigir(req, res, "/dashboard");
            return;
        }
        listar(req, res);
    }

    private void marcarYRedirigir(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Usuario u = usuario(req);
        String clave = p(req, "clave");
        String url = p(req, "url");
        if (!clave.isEmpty()) {
            Db.update("IF NOT EXISTS (SELECT 1 FROM notificaciones_leidas WHERE usuario_id = ? AND clave = ?) "
                    + "INSERT INTO notificaciones_leidas (usuario_id, clave) VALUES (?, ?)",
                    u.getId(), clave, u.getId(), clave);
        }
        if (url.isEmpty() || !url.startsWith("/")) {
            url = "/notificaciones";
        }
        redirigir(req, res, url);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!esEncargado(req)) {
            redirigir(req, res, "/dashboard");
            return;
        }
        if ("marcar_todas".equals(p(req, "accion"))) {
            NotificacionDao.marcarTodas(usuario(req).getId());
            if (esAjax(req)) {
                responderJson(res, true, "Todas las notificaciones se marcaron como leídas.", null);
            } else {
                flash(req, "Todas las notificaciones se marcaron como leídas.", "success");
                redirigir(req, res, "/notificaciones");
            }
            return;
        }
        redirigir(req, res, "/notificaciones");
    }

    private void listar(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Usuario u = usuario(req);
        boolean soloNoLeidas = "no-leidas".equals(p(req, "filtro"));
        List<Notificacion> todas = NotificacionDao.deEncargado(u.getId());
        long noLeidas = NotificacionDao.contarNoLeidas(todas);
        List<Notificacion> visibles = soloNoLeidas
                ? todas.stream().filter(n -> !n.isLeida()).toList()
                : todas;

        req.setAttribute("notificacionesLista", todas);
        req.setAttribute("notificacionesVisibles", visibles);
        req.setAttribute("notificacionesTodas", todas.size());
        req.setAttribute("notificacionesNoLeidas", noLeidas);
        req.setAttribute("filtroNoLeidas", soloNoLeidas);
        req.setAttribute("vistaNotificaciones", Boolean.TRUE);
        render(req, res, "notificaciones/listar", "Notificaciones", "notificaciones");
    }
}
