package edu.universidad.presta.service;

import edu.universidad.presta.dao.Db;
import edu.universidad.presta.dao.EquipoDao;
import edu.universidad.presta.dao.HistorialDao;
import edu.universidad.presta.dao.PrestamoDao;
import edu.universidad.presta.dao.ReservaDao;
import edu.universidad.presta.model.Equipo;

public final class EquipoService {

    private EquipoService() {
    }

    /**
     * Elimina el equipo si nunca se usó; si tiene historial de préstamos o reservas lo da de baja
     * (baja lógica) para conservar el historial. Devuelve el mensaje de éxito.
     */
    public static String eliminarODarDeBaja(int id, int usuarioId) {
        return Db.tx(c -> {
            Equipo equipo = EquipoDao.porIdBloqueado(c, id);
            if (equipo == null) {
                throw new ReglaNegocioException("El equipo no existe.");
            }
            // 1) Con un préstamo activo no se puede eliminar ni dar de baja
            if (PrestamoDao.contarActivosDeEquipo(c, id) > 0) {
                throw new ReglaNegocioException("No se puede eliminar: el equipo tiene un préstamo activo. Registre primero su devolución.");
            }
            // 2) ¿Tiene historial de préstamos o reservas?
            boolean tieneHistorial = PrestamoDao.contarDeEquipo(c, id) + ReservaDao.contarDeEquipo(c, id) > 0;
            if (tieneHistorial) {
                if ("dado_de_baja".equals(equipo.getEstado())) {
                    throw new ReglaNegocioException("El equipo ya está dado de baja. Su historial se conserva y no puede borrarse.");
                }
                EquipoDao.setEstado(c, id, "dado_de_baja");
                // Las reservas pendientes de un equipo dado de baja ya no se pueden cumplir
                int canceladas = ReservaDao.cancelarPendientesDeEquipo(c, id);
                HistorialDao.registrar(c, id, usuarioId, "baja", "Equipo dado de baja (se conserva el historial): " + equipo.getNombre());
                String mensaje = "El equipo tiene historial de préstamos, por eso se dio de baja en lugar de eliminarse.";
                if (canceladas > 0) {
                    mensaje += " Se cancelaron " + canceladas + " reserva(s) pendiente(s).";
                }
                return mensaje;
            }
            // Nunca se usó (p. ej. se registró por error): se puede eliminar definitivamente
            HistorialDao.registrar(c, id, usuarioId, "baja", "Equipo eliminado: " + equipo.getNombre());
            EquipoDao.eliminar(c, id);
            return "Equipo eliminado exitosamente.";
        });
    }
}
