package edu.universidad.presta.servlet;

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

/** Inicio de sesión (login.php) con bloqueo temporal tras intentos fallidos. */
@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute("usuarioId") != null) {
            redirigir(req, res, "/dashboard");
            return;
        }
        mostrar(req, res, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String email = p(req, "email");
        String clave = raw(req, "password");
        String ip = req.getRemoteAddr() == null ? "0.0.0.0" : req.getRemoteAddr();
        String emailClave = email.toLowerCase();

        String error;
        // Límite de intentos: máx. 5 fallos por correo+IP o 20 por IP en 15 minutos
        if (LoginIntentoDao.fallosDeCuenta(ip, emailClave) >= 5 || LoginIntentoDao.fallosDeIp(ip) >= 20) {
            error = "Demasiados intentos fallidos. Espere 15 minutos e inténtelo de nuevo.";
        } else if (email.isEmpty() || clave.isEmpty()) {
            error = "Por favor complete todos los campos.";
        } else {
            Usuario u = UsuarioDao.porEmailActivo(email);
            if (u != null && Seguridad.verificar(clave, u.getPasswordHash())) {
                // Evita la fijación de sesión: sesión nueva al autenticarse
                HttpSession vieja = req.getSession(false);
                if (vieja != null) {
                    vieja.invalidate();
                }
                HttpSession s = req.getSession(true);
                s.setAttribute("usuarioId", u.getId());
                s.setAttribute("usuarioNombre", u.getNombre());
                s.setAttribute("usuarioRol", u.getRol());
                LoginIntentoDao.limpiar(ip, emailClave);
                redirigir(req, res, "/dashboard");
                return;
            }
            LoginIntentoDao.registrar(emailClave, ip);
            error = "Credenciales incorrectas o usuario inactivo.";
        }
        mostrar(req, res, error);
    }

    private void mostrar(HttpServletRequest req, HttpServletResponse res, String error) throws ServletException, IOException {
        req.setAttribute("error", error);
        req.setAttribute("anio", Fechas.hoy().getYear());
        renderSimple(req, res, "login");
    }
}
