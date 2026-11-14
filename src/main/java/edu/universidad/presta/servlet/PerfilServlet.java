package edu.universidad.presta.servlet;

import edu.universidad.presta.dao.DataAccessException;
import edu.universidad.presta.dao.UsuarioDao;
import edu.universidad.presta.model.Usuario;
import edu.universidad.presta.util.Seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Perfil del usuario con sesión iniciada y cambio de su propia contraseña (perfil.php). */
@WebServlet("/perfil")
public class PerfilServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PerfilServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (esModal(req)) {
            renderFragmento(req, res, "perfil/_form");
            return;
        }
        mostrar(req, res, new ArrayList<>());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!"cambiar_password".equals(p(req, "accion"))) {
            if (esAjax(req)) {
                responderJson(res, false, "Solicitud no válida.", new ArrayList<>());
                return;
            }
            redirigir(req, res, "/perfil");
            return;
        }
        Usuario u = usuario(req);
        String actual = raw(req, "password_actual");
        String nueva = raw(req, "nueva_password");
        String confirmar = raw(req, "confirmar_password");
        List<String> errores = new ArrayList<>();

        if (actual.isEmpty()) {
            errores.add("Debe ingresar su contraseña actual.");
        } else if (!Seguridad.verificar(actual, u.getPasswordHash())) {
            errores.add("La contraseña actual ingresada es incorrecta.");
        }
        if (nueva.length() < 8) {
            errores.add("La nueva contraseña debe tener al menos 8 caracteres.");
        }
        if (!nueva.equals(confirmar)) {
            errores.add("Las nuevas contraseñas no coinciden.");
        }
        if (errores.isEmpty()) {
            try {
                UsuarioDao.setPassword(u.getId(), Seguridad.hash(nueva));
                flash(req, "¡Tu contraseña ha sido actualizada exitosamente!", "success");
                if (esAjax(req)) {
                    responderJson(res, true, "Contraseña actualizada exitosamente.", new ArrayList<>());
                    return;
                }
                redirigir(req, res, "/perfil");
                return;
            } catch (DataAccessException e) {
                LOG.log(Level.WARNING, "Error al actualizar la contraseña", e);
                errores.add("Error al actualizar la contraseña. Inténtelo de nuevo.");
            }
        }
        if (esAjax(req)) {
            responderJson(res, false, "No se pudo actualizar la contraseña.", errores);
            return;
        }
        mostrar(req, res, errores);
    }

    private void mostrar(HttpServletRequest req, HttpServletResponse res, List<String> errores) throws ServletException, IOException {
        req.setAttribute("errores", errores);
        render(req, res, "perfil", "Mi Perfil y Contraseña", "perfil");
    }
}
