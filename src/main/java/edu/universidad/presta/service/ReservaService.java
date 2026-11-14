package edu.universidad.presta.service;

import edu.universidad.presta.dao.Db;
import edu.universidad.presta.dao.EquipoDao;
import edu.universidad.presta.dao.HistorialDao;
import edu.universidad.presta.dao.PrestamoDao;
import edu.universidad.presta.dao.ReservaDao;
import edu.universidad.presta.model.Equipo;
import edu.universidad.presta.model.Reserva;
import edu.universidad.presta.util.Fechas;

public final class ReservaService {

    private ReservaService() {
    }

    /**
     * Cambia el estado de una reserva (confirmada / cancelada / completada).
     * Al confirmar se genera el préstamo real; al completar se cierra el préstamo activo asociado.
     * Devuelve el mensaje de éxito.
     */
    public static String cambiarEstado(int id, String nuevoEstado) {
        return Db.tx(c -> {
            Reserva reserva = ReservaDao.porIdBloqueada(c, id);
            if (reserva == null) {
                throw new ReglaNegocioException("La reserva no existe.");
            }
            if (!transicionValida(reserva.getEstado(), nuevoEstado)) {
                throw new ReglaNegocioException("La reserva está \"" + reserva.getEstado() + "\" y no puede pasar a \"" + nuevoEstado + "\".");
            }

            if ("confirmada".equals(nuevoEstado)) {
                // El encargado convierte la reserva en un préstamo real: el equipo queda prestado
                // y aparece en el historial del usuario.
                Equipo equipo = EquipoDao.porIdBloqueado(c, reserva.getEquipoId());
                if (equipo == null || !"disponible".equals(equipo.getEstado())) {
                    throw new ReglaNegocioException("No se puede confirmar: el equipo ya no está disponible para préstamo.");
                }
                PrestamoDao.insertar(c, reserva.getUsuarioId(), reserva.getEquipoId(), Fechas.ahora(), reserva.getFechaFin(),
                        "Préstamo generado automáticamente al confirmar la reserva #" + reserva.getId());
                EquipoDao.setEstado(c, reserva.getEquipoId(), "prestado");
                HistorialDao.registrar(c, reserva.getEquipoId(), reserva.getUsuarioId(), "prestamo",
                        "Préstamo generado desde reserva confirmada: " + equipo.getNombre());
            }

            if ("completada".equals(nuevoEstado)) {
                // Si la reserva se había convertido en préstamo activo, se marca como devuelto
                // y el equipo vuelve a estar disponible.
                Integer prestamoActivo = PrestamoDao.idActivoDe(c, reserva.getUsuarioId(), reserva.getEquipoId());
                if (prestamoActivo != null) {
                    PrestamoDao.marcarDevuelto(c, prestamoActivo, Fechas.ahora());
                    EquipoDao.setEstado(c, reserva.getEquipoId(), "disponible");
                    HistorialDao.registrar(c, reserva.getEquipoId(), reserva.getUsuarioId(), "devolucion",
                            "Devolución registrada al completar la reserva #" + reserva.getId());
                }
            }

            ReservaDao.setEstado(c, id, nuevoEstado);

            switch (nuevoEstado) {
                case "confirmada":
                    return "Reserva confirmada: el préstamo fue registrado y el equipo ahora figura como prestado al usuario.";
                case "cancelada":
                    return "Reserva cancelada exitosamente.";
                default:
                    return "Reserva marcada como completada.";
            }
        });
    }

    /** Solo las transiciones que ofrece la interfaz: pendiente → confirmada/cancelada; confirmada → completada/cancelada. */
    private static boolean transicionValida(String actual, String nuevo) {
        if ("pendiente".equals(actual)) {
            return "confirmada".equals(nuevo) || "cancelada".equals(nuevo);
        }
        if ("confirmada".equals(actual)) {
            return "completada".equals(nuevo) || "cancelada".equals(nuevo);
        }
        return false;
    }
}
