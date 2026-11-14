package edu.universidad.presta.servlet;

import edu.universidad.presta.dao.DataAccessException;
import edu.universidad.presta.dao.EquipoDao;
import edu.universidad.presta.dao.ReservaDao;
import edu.universidad.presta.service.ReglaNegocioException;
import edu.universidad.presta.service.ReservaService;
import edu.universidad.presta.util.Fechas;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Módulo de reservas (reservas/*.php): /reservas/listar, /crear y /cambiar-estado (esta última solo admin/encargado). */
@WebServlet(urlPatterns = {"/reservas", "/reservas/*"})
public class ReservaServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ReservaServlet.class.getName());
    private static final Set<String> ESTADOS_VALIDOS = new HashSet<>(Arrays.asList("confirmada", "cancelada", "completada"));

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        switch (subRuta(req, "/listar")) {
            case "/listar":
                listar(req, res);
                break;
            case "/crear":
                formulario(req, res, new ArrayList<>());
                break;
            default:
                redirigir(req, res, "/reservas/listar");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        switch (subRuta(req, "/listar")) {
            case "/crear":
                crear(req, res);
                break;
            case "/cambiar-estado":
                cambiarEstado(req, res);
                break;
            default:
                redirigir(req, res, "/reservas/listar");
        }
    }

    private void listar(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // Admin y encargado ven todas las reservas; los usuarios solo las suyas
        req.setAttribute("reservas", esEncargado(req) ? ReservaDao.todas() : ReservaDao.deUsuario(usuario(req).getId()));
        render(req, res, "reservas/listar", "Gestión de Reservas", "reservas");
    }

    private void formulario(HttpServletRequest req, HttpServletResponse res, List<String> errores) throws ServletException, IOException {
        req.setAttribute("equiposDisponibles", EquipoDao.disponibles());
        req.setAttribute("errores", errores);
        req.setAttribute("minInicio", Fechas.input(Fechas.hoy().atStartOfDay()));
        req.setAttribute("minFin", Fechas.input(Fechas.ahora()));
        // La fecha de inicio se propone con la fecha y hora actual (el usuario puede cambiarla)
        req.setAttribute("valorInicio", Fechas.input(Fechas.ahora()));
        if (esModal(req)) {
            renderFragmento(req, res, "reservas/_crear");
            return;
        }
        render(req, res, "reservas/crear", "Nueva Reserva", "reservas");
    }

    private void crear(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        List<String> errores = new ArrayList<>();
        Integer equipoId = entero(p(req, "equipo_id"));
        String inicioTxt = p(req, "fecha_inicio");
        String finTxt = p(req, "fecha_fin");
        LocalDateTime inicio = null;
        LocalDateTime fin = null;

        if (equipoId == null) {
            errores.add("Seleccione un equipo.");
        }
        if (inicioTxt.isEmpty() || finTxt.isEmpty()) {
            errores.add("Las fechas de inicio y fin son obligatorias.");
        } else {
            inicio = Fechas.parse(inicioTxt);
            fin = Fechas.parse(finTxt);
            if (inicio == null || fin == null) {
                errores.add("El formato de las fechas no es válido.");
            } else {
                // "Hoy" es válido a cualquier hora: solo se rechazan días anteriores a hoy.
                if (inicio.isBefore(Fechas.hoy().atStartOfDay())) {
                    errores.add("La fecha de inicio no puede ser anterior a hoy.");
                }
                if (!fin.isAfter(inicio)) {
                    errores.add("La fecha de fin debe ser posterior a la fecha de inicio.");
                } else if (fin.isBefore(Fechas.ahora())) {
                    errores.add("La reserva ya habría terminado: la fecha y hora de fin debe ser posterior a la hora actual.");
                }
            }
        }

        try {
            if (errores.isEmpty() && ReservaDao.haySolapeVigente(equipoId, inicio, fin)) {
                errores.add("El equipo ya está reservado en ese rango de fechas.");
            }
            if (errores.isEmpty()) {
                ReservaDao.crear(usuario(req).getId(), equipoId, inicio, fin);
                flash(req, "Reserva creada exitosamente. Pendiente de confirmación.", "success");
                if (esAjax(req)) {
                    responderJson(res, true, "Reserva creada exitosamente. Pendiente de confirmación.", new ArrayList<>());
                    return;
                }
                redirigir(req, res, "/reservas/listar");
                return;
            }
        } catch (DataAccessException e) {
            LOG.log(Level.WARNING, "Error al crear la reserva", e);
            errores.add("Error al crear la reserva. Inténtelo de nuevo.");
        }
        if (esAjax(req)) {
            responderJson(res, false, "No se pudo crear la reserva.", errores);
            return;
        }
        formulario(req, res, errores);
    }

    private void cambiarEstado(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Integer id = entero(p(req, "id"));
        String nuevoEstado = p(req, "estado");
        if (id == null || !ESTADOS_VALIDOS.contains(nuevoEstado)) {
            flash(req, "Estado no válido.", "danger");
            redirigir(req, res, "/reservas/listar");
            return;
        }
        try {
            flash(req, ReservaService.cambiarEstado(id, nuevoEstado), "success");
        } catch (ReglaNegocioException e) {
            flash(req, "Error al actualizar: " + e.getMessage(), "danger");
        } catch (DataAccessException e) {
            LOG.log(Level.WARNING, "Error al cambiar el estado de la reserva", e);
            flash(req, "Error al actualizar la reserva. Inténtelo de nuevo.", "danger");
        }
        redirigir(req, res, "/reservas/listar");
    }
}
