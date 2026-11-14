package edu.universidad.presta.servlet;

import edu.universidad.presta.dao.EquipoDao;
import edu.universidad.presta.dao.PrestamoDao;
import edu.universidad.presta.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** Panel de control (dashboard.php): estadísticas según el rol y últimos préstamos. */
@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Usuario u = usuario(req);
        if (u.isEncargado()) {
            Map<String, Integer> conteo = EquipoDao.conteoPorEstado();
            int total = 0;
            for (int n : conteo.values()) {
                total += n;
            }
            req.setAttribute("totalEquipos", total);
            req.setAttribute("equiposDisponibles", conteo.get("disponible"));
            req.setAttribute("equiposPrestados", conteo.get("prestado"));
            req.setAttribute("prestamosVencidos", PrestamoDao.contarVencidos());
            req.setAttribute("ultimosPrestamos", PrestamoDao.ultimos(null, 10));
        } else {
            req.setAttribute("misPrestamosActivos", PrestamoDao.contarActivosDe(u.getId()));
            req.setAttribute("ultimosPrestamos", PrestamoDao.ultimos(u.getId(), 10));
        }
        render(req, res, "dashboard", "Panel de Control", "dashboard");
    }
}
