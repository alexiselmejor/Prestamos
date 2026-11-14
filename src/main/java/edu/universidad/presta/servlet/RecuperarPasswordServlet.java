package edu.universidad.presta.servlet;

import edu.universidad.presta.dao.DataAccessException;
import edu.universidad.presta.dao.LoginIntentoDao;
import edu.universidad.presta.dao.UsuarioDao;
import edu.universidad.presta.model.Usuario;
import edu.universidad.presta.util.Fechas;
import edu.universidad.presta.util.Seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Recuperación de contraseña sin sesión (recuperar_password.php), en tres pasos:
 * 1) verificar correo + DNI, 2) definir la nueva contraseña, 3) confirmación.
 */
@WebServlet("/recuperar-password")
public class RecuperarPasswordServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(RecuperarPasswordServlet.class.getName());
    private static final String S_USUARIO = "recuperar_usuario_id";
    private static final String S_NOMBRE = "recuperar_nombre";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (haySesion(req)) {
            redirigir(req, res, "/perfil");
            return;
        }
        // Entrar de nuevo a la página reinicia cualquier proceso a medias
        HttpSession s = req.getSession(false);
        if (s != null) {
            s.removeAttribute(S_USUARIO);
            s.removeAttribute(S_NOMBRE);
        }
        mostrar(req, res, 1, null, null, "", "");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (haySesion(req)) {
            redirigir(req, res, "/perfil");
            return;
        }
        if ("cambiar".equals(p(req, "accion"))) {
            cambiar(req, res);
        } else {
            verificar(req, res);
        }
    }

    private boolean haySesion(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null && s.getAttribute("usuarioId") != null;
    }

    // ------------------------------------------------------------------ paso 1: correo + DNI

    private void verificar(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String email = p(req, "email");
        String dni = p(req, "dni");
        if (email.isEmpty() || dni.isEmpty()) {
            mostrar(req, res, 1, "Por favor ingrese su correo institucional y su DNI.", null, email, dni);
            return;
        }
        // Mismo bloqueo temporal que el login (5 fallos por correo+IP o 20 por IP en 15 minutos)
        String ip = req.getRemoteAddr() == null ? "0.0.0.0" : req.getRemoteAddr();
        String clave = ("rec:" + email.toLowerCase());
        if (clave.length() > 100) {
            clave = clave.substring(0, 100);
        }
        try {
            if (LoginIntentoDao.fallosDeCuenta(ip, clave) >= 5 || LoginIntentoDao.fallosDeIp(ip) >= 20) {
                mostrar(req, res, 1, "Demasiados intentos fallidos. Espere 15 minutos e inténtelo de nuevo.", null, email, dni);
                return;
            }
            Usuario u = UsuarioDao.porEmailYDniActivo(email, dni);
            if (u == null) {
                LoginIntentoDao.registrar(clave, ip);
                mostrar(req, res, 1, "Los datos ingresados no coinciden con ningún usuario activo registrado.", null, email, dni);
                return;
            }
            LoginIntentoDao.limpiar(ip, clave);
            HttpSession s = req.getSession(true);
            s.setAttribute(S_USUARIO, u.getId());
            s.setAttribute(S_NOMBRE, u.getNombre());
            mostrar(req, res, 2, null, null, email, dni);
        } catch (DataAccessException e) {
            LOG.log(Level.WARNING, "Error al verificar la identidad", e);
            mostrar(req, res, 1, "No se pudo verificar los datos. Inténtelo de nuevo.", null, email, dni);
        }
    }

    // ------------------------------------------------------------------ paso 2: nueva contraseña

    private void cambiar(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        Integer id = s == null ? null : (Integer) s.getAttribute(S_USUARIO);
        String clave = raw(req, "password");

        if (id == null) {
            mostrar(req, res, 1, "Sesión de recuperación expirada. Inicie el proceso nuevamente.", null, "", "");
        } else if (clave.length() < 8) {
            mostrar(req, res, 2, "La nueva contraseña debe tener al menos 8 caracteres.", null, "", "");
        } else if (!clave.equals(raw(req, "password_confirm"))) {
            mostrar(req, res, 2, "Las contraseñas no coinciden.", null, "", "");
        } else {
            try {
                Usuario u = UsuarioDao.porId(id);
                if (u == null || !u.isActivo()) {
                    s.removeAttribute(S_USUARIO);
                    s.removeAttribute(S_NOMBRE);
                    mostrar(req, res, 1, "Sesión de recuperación expirada. Inicie el proceso nuevamente.", null, "", "");
                    return;
                }
                UsuarioDao.setPassword(id, Seguridad.hash(clave));
                s.removeAttribute(S_USUARIO);
                s.removeAttribute(S_NOMBRE);
                mostrar(req, res, 3, null, "¡Contraseña restablecida exitosamente! Ya puedes iniciar sesión con tu nueva contraseña.", "", "");
            } catch (DataAccessException e) {
                LOG.log(Level.WARNING, "Error al restablecer la contraseña", e);
                mostrar(req, res, 2, "Error al actualizar la contraseña. Inténtelo de nuevo.", null, "", "");
            }
        }
    }

    private void mostrar(HttpServletRequest req, HttpServletResponse res, int paso, String error, String exito,
                         String email, String dni) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        req.setAttribute("paso", paso);
        req.setAttribute("error", error);
        req.setAttribute("exito", exito);
        req.setAttribute("emailVal", email);
        req.setAttribute("dniVal", dni);
        req.setAttribute("nombre", s == null ? "" : s.getAttribute(S_NOMBRE));
        req.setAttribute("anio", Fechas.hoy().getYear());
        renderSimple(req, res, "recuperar");
    }
}
