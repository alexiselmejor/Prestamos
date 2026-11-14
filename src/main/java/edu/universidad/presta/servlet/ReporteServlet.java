package edu.universidad.presta.servlet;

import edu.universidad.presta.dao.CategoriaDao;
import edu.universidad.presta.dao.PrestamoDao;
import edu.universidad.presta.dao.SolicitudDao;
import edu.universidad.presta.dao.UsuarioDao;
import edu.universidad.presta.model.Prestamo;
import edu.universidad.presta.model.Usuario;
import edu.universidad.presta.util.Fechas;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reportes de préstamos por período (reportes/index.php), solo admin y encargado. */
@WebServlet(urlPatterns = {"/reportes", "/reportes/*"})
public class ReporteServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private static final Set<String> ESTADOS = new HashSet<>(Arrays.asList("activo", "devuelto", "vencido"));
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/usuario")) {
            Integer id = entero(p(req, "id"));
            Usuario u = (id == null) ? null : UsuarioDao.porId(id);
            if (u == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            List<Prestamo> loans = PrestamoDao.porUsuario(u.getId());
            List<Prestamo> vencidos = new ArrayList<>();
            List<Prestamo> pendientes = new ArrayList<>();
            List<Prestamo> ahora = new ArrayList<>();
            for (Prestamo pr : loans) {
                if ("devuelto".equals(pr.getEstado())) continue;
                ahora.add(pr);
                if (esVencido(pr)) {
                    vencidos.add(pr);
                } else {
                    pendientes.add(pr);
                }
            }
            req.setAttribute("usuarioHistorial", u);
            req.setAttribute("prestamosHistorial", loans);
            req.setAttribute("solicitudesHistorial", SolicitudDao.deUsuario(u.getId()));
            renderFragmento(req, res, "reportes/_historial");
            return;
        }

        LocalDate hoy = Fechas.hoy();
        LocalDate desde = Fechas.parseFecha(p(req, "fecha_inicio"), hoy.withDayOfMonth(1));
        LocalDate hasta = Fechas.parseFecha(p(req, "fecha_fin"), hoy);
        if (hasta.isBefore(desde)) {
            LocalDate tmp = desde;
            desde = hasta;
            hasta = tmp;
        }
        String categoria = p(req, "categoria_id");
        String estado = ESTADOS.contains(p(req, "estado")) ? p(req, "estado") : "";

        List<Prestamo> prestamos = PrestamoDao.reporte(desde, hasta, entero(categoria), estado);

        long activos = 0;
        long devueltos = 0;
        long vencidos = 0;
        long diasTotales = 0;
        long devolucionesConFecha = 0;
        Map<String, Integer> porCategoria = new LinkedHashMap<>();

        for (Prestamo p : prestamos) {
            if ("activo".equals(p.getEstado())) {
                activos++;
            } else if ("devuelto".equals(p.getEstado())) {
                devueltos++;
            } else if ("vencido".equals(p.getEstado())) {
                vencidos++;
            }
            if (p.getFechaDevolucionReal() != null && p.getFechaPrestamo() != null) {
                diasTotales += Math.max(0, ChronoUnit.DAYS.between(p.getFechaPrestamo(), p.getFechaDevolucionReal()));
                devolucionesConFecha++;
            }
            String nombreCategoria = (p.getCategoriaNombre() == null || p.getCategoriaNombre().isEmpty())
                    ? "Sin categoría" : p.getCategoriaNombre();
            porCategoria.merge(nombreCategoria, 1, Integer::sum);
        }

        long total = prestamos.size();

        // Resumen por categoría: ordenado de mayor a menor y con barras proporcionales.
        int maxCategoria = porCategoria.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<Map<String, Object>> resumenCategorias = new ArrayList<>();
        porCategoria.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(e -> {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("nombre", e.getKey());
                    fila.put("total", e.getValue());
                    fila.put("pct", porcentaje(e.getValue(), maxCategoria));
                    resumenCategorias.add(fila);
                });

        // Ranking de equipos: se calcula sobre el listado ya filtrado para que
        // responda a los mismos filtros del reporte. El porcentaje es relativo al primero.
        Map<String, Map<String, Object>> acumuladoEquipos = new LinkedHashMap<>();
        for (Prestamo p : prestamos) {
            String clave = p.getCodigoInventario() == null || p.getCodigoInventario().isEmpty()
                    ? "ID-" + p.getEquipoId() : p.getCodigoInventario();
            Map<String, Object> fila = acumuladoEquipos.get(clave);
            if (fila == null) {
                fila = new LinkedHashMap<>();
                fila.put("nombre", p.getEquipoNombre());
                fila.put("codigo", clave);
                fila.put("total", 0L);
                acumuladoEquipos.put(clave, fila);
            }
            fila.put("total", (Long) fila.get("total") + 1);
        }
        List<Map<String, Object>> rankingEquipos = new ArrayList<>(acumuladoEquipos.values());
        rankingEquipos.sort((a, b) -> Long.compare((Long) b.get("total"), (Long) a.get("total")));
        if (rankingEquipos.size() > 10) {
            rankingEquipos = new ArrayList<>(rankingEquipos.subList(0, 10));
        }
        long maxEquipos = rankingEquipos.isEmpty() ? 0 : (Long) rankingEquipos.get(0).get("total");
        for (Map<String, Object> fila : rankingEquipos) {
            fila.put("pct", porcentaje((Long) fila.get("total"), maxEquipos));
        }

        req.setAttribute("fechaInicio", desde.toString());
        req.setAttribute("fechaFin", hasta.toString());
        req.setAttribute("categoriaSel", categoria);
        req.setAttribute("estadoSel", estado);
        req.setAttribute("periodoTexto", desde.format(DMY) + " – " + hasta.format(DMY));
        req.setAttribute("tituloPdf", "Reporte de Préstamos (" + desde.format(DMY) + " - " + hasta.format(DMY) + ")");
        req.setAttribute("categorias", CategoriaDao.listar());
        req.setAttribute("prestamos", prestamos);
        req.setAttribute("totalPrestamos", total);
        req.setAttribute("prestamosActivos", activos);
        req.setAttribute("prestamosDevueltos", devueltos);
        req.setAttribute("prestamosVencidos", vencidos);
        req.setAttribute("pctActivos", porcentaje(activos, total));
        req.setAttribute("pctDevueltos", porcentaje(devueltos, total));
        req.setAttribute("pctVencidos", porcentaje(vencidos, total));
        req.setAttribute("tasaDevolucion", porcentaje(devueltos, total));
        req.setAttribute("promedioDias", devolucionesConFecha == 0
                ? 0 : Math.round((double) diasTotales / devolucionesConFecha));
        req.setAttribute("resumenCategorias", resumenCategorias);
        req.setAttribute("rankingEquipos", rankingEquipos);

        // Usuarios del sistema con conteo de préstamos pendientes y vencidos.
        List<Usuario> usuariosReporte = UsuarioDao.listar("", "");
        Map<Integer, Long> pendientesPorUsuario = new java.util.HashMap<>();
        Map<Integer, Long> vencidosPorUsuario = new java.util.HashMap<>();
        for (Usuario u : usuariosReporte) {
            long pen = 0, ven = 0;
            for (Prestamo pr : PrestamoDao.porUsuario(u.getId())) {
                if (esVencido(pr)) {
                    ven++;
                } else if (!"devuelto".equals(pr.getEstado())) {
                    pen++;
                }
            }
            pendientesPorUsuario.put(u.getId(), pen);
            vencidosPorUsuario.put(u.getId(), ven);
        }
        req.setAttribute("usuariosReporte", usuariosReporte);
        req.setAttribute("pendientesPorUsuario", pendientesPorUsuario);
        req.setAttribute("vencidosPorUsuario", vencidosPorUsuario);

        render(req, res, "reportes/index", "Reportes", "reportes");
    }

    /** Porcentaje entero de una parte sobre un total (0 si el total es 0). */
    private static int porcentaje(long parte, long total) {
        return total == 0 ? 0 : (int) Math.round(parte * 100.0 / total);
    }

    private static boolean esVencido(Prestamo p) {
        if (p == null) return false;
        if ("devuelto".equals(p.getEstado())) return false;
        if ("vencido".equals(p.getEstado())) return true;
        return p.getFechaDevolucionEsperada() != null
                && p.getFechaDevolucionEsperada().isBefore(Fechas.ahora());
    }
}
