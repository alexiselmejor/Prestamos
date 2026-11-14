package edu.universidad.presta.service;

import edu.universidad.presta.dao.Db;
import edu.universidad.presta.dao.EquipoDao;
import edu.universidad.presta.dao.HistorialDao;
import edu.universidad.presta.dao.PrestamoDao;
import edu.universidad.presta.dao.ReservaDao;
import edu.universidad.presta.model.Equipo;
import edu.universidad.presta.model.Prestamo;

import java.time.LocalDateTime;

public final class PrestamoService {

    private PrestamoService() {
    }

    /** Registra un préstamo: valida disponibilidad, reservas y morosidad, y actualiza equipo e historial. */
    public static void registrar(int usuarioId, int equipoId, LocalDateTime fechaPrestamo,
                                 LocalDateTime fechaDevolucionEsperada, String observaciones) {
        Db.tx(c -> {
            Equipo equipo = EquipoDao.porIdBloqueado(c, equipoId);
            if (equipo == null || !"disponible".equals(equipo.getEstado())) {
                throw new ReglaNegocioException("El equipo seleccionado no está disponible.");
            }
            // Una reserva confirmada que se solape con el período bloquea el préstamo
            if (ReservaDao.haySolapeConfirmada(c, equipoId, fechaPrestamo, fechaDevolucionEsperada)) {
                throw new ReglaNegocioException("El equipo tiene una reserva confirmada que se solapa con este período.");
            }
            if (PrestamoDao.usuarioTieneVencidos(c, usuarioId)) {
                throw new ReglaNegocioException("El usuario tiene préstamos vencidos. Debe devolverlos antes de solicitar otro.");
            }
            PrestamoDao.insertar(c, usuarioId, equipoId, fechaPrestamo, fechaDevolucionEsperada, observaciones);
            EquipoDao.setEstado(c, equipoId, "prestado");
            HistorialDao.registrar(c, equipoId, usuarioId, "prestamo", "Préstamo de equipo: " + equipo.getNombre());
            return null;
        });
    }

    /** Registra la devolución. Si se devuelve después de la fecha esperada el préstamo queda como "vencido". */
    public static void devolver(int prestamoId, LocalDateTime fechaReal, String estadoEquipo, String observaciones) {
        Db.tx(c -> {
            Prestamo prestamo = PrestamoDao.activoPorIdBloqueado(c, prestamoId);
            if (prestamo == null) {
                throw new ReglaNegocioException("Préstamo no encontrado o ya devuelto.");
            }
            String estadoPrestamo = fechaReal.isAfter(prestamo.getFechaDevolucionEsperada()) ? "vencido" : "devuelto";
            String nota = observaciones.isEmpty() ? "" : " - Devolución: " + observaciones;
            PrestamoDao.registrarDevolucion(c, prestamoId, fechaReal, estadoPrestamo, nota);
            EquipoDao.setEstado(c, prestamo.getEquipoId(), estadoEquipo);
            HistorialDao.registrar(c, prestamo.getEquipoId(), prestamo.getUsuarioId(), "devolucion", "Devolución de equipo");
            return null;
        });
    }
}
