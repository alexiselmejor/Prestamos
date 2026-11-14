package edu.universidad.presta.dao;

import edu.universidad.presta.util.Fechas;

import java.sql.Connection;

/** Tabla historial_movimientos: alta, edicion, prestamo, devolucion, mantenimiento, baja. */
public final class HistorialDao {

    private static final String INSERT =
            "INSERT INTO historial_movimientos (equipo_id, usuario_id, tipo, detalle, fecha) VALUES (?, ?, ?, ?, ?)";

    private HistorialDao() {
    }

    public static void registrar(int equipoId, int usuarioId, String tipo, String detalle) {
        Db.update(INSERT, equipoId, usuarioId, tipo, detalle, Fechas.ahora());
    }

    public static void registrar(Connection c, int equipoId, int usuarioId, String tipo, String detalle) {
        Db.update(c, INSERT, equipoId, usuarioId, tipo, detalle, Fechas.ahora());
    }
}
