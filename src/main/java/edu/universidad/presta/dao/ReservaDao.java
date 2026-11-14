package edu.universidad.presta.dao;

import edu.universidad.presta.util.Fechas;
import edu.universidad.presta.model.Reserva;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public final class ReservaDao {

    private static final String SELECT_UNIDO =
            "SELECT r.*, e.nombre AS equipo_nombre, e.codigo_inventario, u.nombre AS usuario_nombre, u.email AS usuario_email "
            + "FROM reservas r JOIN equipos e ON r.equipo_id = e.id JOIN usuarios u ON r.usuario_id = u.id ";

    private ReservaDao() {
    }

    private static Reserva map(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setId(rs.getInt("id"));
        r.setUsuarioId(rs.getInt("usuario_id"));
        r.setEquipoId(rs.getInt("equipo_id"));
        r.setFechaReserva(Db.dt(rs, "fecha_reserva"));
        r.setFechaInicio(Db.dt(rs, "fecha_inicio"));
        r.setFechaFin(Db.dt(rs, "fecha_fin"));
        r.setEstado(rs.getString("estado"));
        if (Db.has(rs, "equipo_nombre")) {
            r.setEquipoNombre(rs.getString("equipo_nombre"));
            r.setCodigoInventario(rs.getString("codigo_inventario"));
            r.setUsuarioNombre(rs.getString("usuario_nombre"));
            r.setUsuarioEmail(rs.getString("usuario_email"));
        }
        return r;
    }

    public static List<Reserva> todas() {
        return Db.list(SELECT_UNIDO + "ORDER BY r.fecha_reserva DESC", ReservaDao::map);
    }

    public static List<Reserva> deUsuario(int usuarioId) {
        return Db.list(SELECT_UNIDO + "WHERE r.usuario_id = ? ORDER BY r.fecha_reserva DESC", ReservaDao::map, usuarioId);
    }

    public static Reserva porIdBloqueada(Connection c, int id) {
        return Db.one(c, "SELECT * FROM reservas WITH (UPDLOCK, ROWLOCK) WHERE id = ?", ReservaDao::map, id);
    }

    public static long contarPendientes() {
        return Db.scalar("SELECT COUNT(*) FROM reservas WHERE estado = 'pendiente'");
    }

    public static long contarVigentesDe(int usuarioId) {
        return Db.scalar("SELECT COUNT(*) FROM reservas WHERE usuario_id = ? AND estado IN ('pendiente', 'confirmada')", usuarioId);
    }

    public static long contarDeEquipo(Connection c, int equipoId) {
        return Db.scalar(c, "SELECT COUNT(*) FROM reservas WHERE equipo_id = ?", equipoId);
    }

    /** ¿Hay otra reserva pendiente/confirmada del equipo que se solape con el rango pedido? */
    public static boolean haySolapeVigente(int equipoId, LocalDateTime inicio, LocalDateTime fin) {
        return Db.scalar("SELECT COUNT(*) FROM reservas WHERE equipo_id = ? AND estado IN ('pendiente', 'confirmada') AND ("
                + "(fecha_inicio BETWEEN ? AND ?) OR (fecha_fin BETWEEN ? AND ?) OR (fecha_inicio <= ? AND fecha_fin >= ?))",
                equipoId, inicio, fin, inicio, fin, inicio, fin) > 0;
    }

    /** ¿Hay una reserva confirmada del equipo que se solape con el período de un préstamo? */
    public static boolean haySolapeConfirmada(Connection c, int equipoId, LocalDateTime prestamo, LocalDateTime devolucion) {
        return Db.scalar(c, "SELECT COUNT(*) FROM reservas WHERE equipo_id = ? AND estado = 'confirmada' AND fecha_inicio < ? AND fecha_fin > ?",
                equipoId, devolucion, prestamo) > 0;
    }

    public static void crear(int usuarioId, int equipoId, LocalDateTime inicio, LocalDateTime fin) {
        Db.update("INSERT INTO reservas (usuario_id, equipo_id, fecha_reserva, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?, 'pendiente')",
                usuarioId, equipoId, Fechas.ahora(), inicio, fin);
    }

    public static void setEstado(Connection c, int id, String estado) {
        Db.update(c, "UPDATE reservas SET estado = ? WHERE id = ?", estado, id);
    }

    /** Cancela las reservas pendientes de un equipo; devuelve cuántas fueron. */
    public static int cancelarPendientesDeEquipo(Connection c, int equipoId) {
        return Db.update(c, "UPDATE reservas SET estado = 'cancelada' WHERE equipo_id = ? AND estado = 'pendiente'", equipoId);
    }
}
