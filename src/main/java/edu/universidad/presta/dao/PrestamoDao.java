package edu.universidad.presta.dao;

import edu.universidad.presta.util.Fechas;
import edu.universidad.presta.model.Prestamo;
import edu.universidad.presta.model.TopEquipo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PrestamoDao {

    private PrestamoDao() {
    }

    /** Lee las columnas de prestamos y, si vienen en la consulta, las del equipo/usuario/categoría unidos. */
    private static Prestamo map(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setId(rs.getInt("id"));
        p.setUsuarioId(rs.getInt("usuario_id"));
        p.setEquipoId(rs.getInt("equipo_id"));
        p.setFechaPrestamo(Db.dt(rs, "fecha_prestamo"));
        p.setFechaDevolucionEsperada(Db.dt(rs, "fecha_devolucion_esperada"));
        p.setFechaDevolucionReal(Db.dt(rs, "fecha_devolucion_real"));
        p.setEstado(rs.getString("estado"));
        p.setObservaciones(rs.getString("observaciones"));
        if (Db.has(rs, "equipo_nombre")) {
            p.setEquipoNombre(rs.getString("equipo_nombre"));
        }
        if (Db.has(rs, "codigo_inventario")) {
            p.setCodigoInventario(rs.getString("codigo_inventario"));
        }
        if (Db.has(rs, "usuario_nombre")) {
            p.setUsuarioNombre(rs.getString("usuario_nombre"));
        }
        if (Db.has(rs, "usuario_email")) {
            p.setUsuarioEmail(rs.getString("usuario_email"));
        }
        if (Db.has(rs, "categoria_nombre")) {
            p.setCategoriaNombre(rs.getString("categoria_nombre"));
        }
        if (Db.has(rs, "dias_vencido")) {
            p.setDiasVencido(rs.getLong("dias_vencido"));
        }
        return p;
    }

    // ------------------------------------------------------------------ conteos

    /** Todos los préstamos del sistema (para la vista de préstamos del encargado). */
    public static List<Prestamo> todos() {
        return Db.list("SELECT p.*, e.nombre AS equipo_nombre, e.codigo_inventario, u.nombre AS usuario_nombre, u.email AS usuario_email "
                + "FROM prestamos p JOIN equipos e ON p.equipo_id = e.id JOIN usuarios u ON p.usuario_id = u.id "
                + "ORDER BY p.fecha_prestamo DESC", PrestamoDao::map);
    }

    /** Préstamos activos cuya fecha esperada ya pasó. */
    public static long contarVencidos() {
        return Db.scalar("SELECT COUNT(*) FROM prestamos WHERE estado = 'activo' AND fecha_devolucion_esperada < ?", Fechas.ahora());
    }

    public static long contarActivosDe(int usuarioId) {
        return Db.scalar("SELECT COUNT(*) FROM prestamos WHERE usuario_id = ? AND estado = 'activo'", usuarioId);
    }

    public static boolean usuarioTieneVencidos(Connection c, int usuarioId) {
        return Db.scalar(c, "SELECT COUNT(*) FROM prestamos WHERE usuario_id = ? AND estado = 'activo' AND fecha_devolucion_esperada < ?", usuarioId, Fechas.ahora()) > 0;
    }

    public static long contarActivosDeEquipo(Connection c, int equipoId) {
        return Db.scalar(c, "SELECT COUNT(*) FROM prestamos WHERE equipo_id = ? AND estado = 'activo'", equipoId);
    }

    public static long contarDeEquipo(Connection c, int equipoId) {
        return Db.scalar(c, "SELECT COUNT(*) FROM prestamos WHERE equipo_id = ?", equipoId);
    }

    // ------------------------------------------------------------------ listados

    /** Préstamos activos (no devueltos) de un usuario, para "Mis Equipos". */
    public static List<Prestamo> activosDeUsuario(int usuarioId) {
        return Db.list("SELECT p.*, e.codigo_inventario, e.nombre AS equipo_nombre, u.nombre AS usuario_nombre "
                + "FROM prestamos p JOIN equipos e ON p.equipo_id = e.id JOIN usuarios u ON p.usuario_id = u.id "
                + "WHERE p.usuario_id = ? AND p.fecha_devolucion_real IS NULL ORDER BY p.fecha_prestamo DESC",
                PrestamoDao::map, usuarioId);
    }

    /** Últimos préstamos (del usuario indicado, o de todos si usuarioId es null). */
    public static List<Prestamo> ultimos(Integer usuarioId, int limite) {
        String sql = "SELECT TOP (" + limite + ") p.*, e.nombre AS equipo_nombre, u.nombre AS usuario_nombre "
                + "FROM prestamos p JOIN equipos e ON p.equipo_id = e.id JOIN usuarios u ON p.usuario_id = u.id "
                + (usuarioId != null ? "WHERE p.usuario_id = ? " : "")
                + "ORDER BY p.fecha_prestamo DESC";
        return usuarioId != null ? Db.list(sql, PrestamoDao::map, usuarioId) : Db.list(sql, PrestamoDao::map);
    }

    public static Prestamo porId(int id) {
        return Db.one("SELECT p.*, e.nombre AS equipo_nombre, e.codigo_inventario, u.nombre AS usuario_nombre, u.email AS usuario_email "
                + "FROM prestamos p JOIN equipos e ON p.equipo_id = e.id JOIN usuarios u ON p.usuario_id = u.id WHERE p.id = ?",
                PrestamoDao::map, id);
    }

    /** Préstamos activos para el formulario de devolución. */
    public static List<Prestamo> activos() {
        return Db.list("SELECT p.*, e.codigo_inventario, e.nombre AS equipo_nombre, u.nombre AS usuario_nombre "
                + "FROM prestamos p JOIN equipos e ON p.equipo_id = e.id JOIN usuarios u ON p.usuario_id = u.id "
                + "WHERE p.estado = 'activo' ORDER BY p.fecha_prestamo DESC", PrestamoDao::map);
    }

    /** Préstamos pendientes de devolución, los vencidos primero. */
    public static List<Prestamo> activosYVencidos() {
        return Db.list("SELECT p.*, e.codigo_inventario, e.nombre AS equipo_nombre, u.nombre AS usuario_nombre, u.email AS usuario_email, "
                + "DATEDIFF(MINUTE, p.fecha_devolucion_esperada, ?) / 1440 AS dias_vencido "
                + "FROM prestamos p JOIN equipos e ON p.equipo_id = e.id JOIN usuarios u ON p.usuario_id = u.id "
                + "WHERE p.estado IN ('activo', 'vencido') AND p.fecha_devolucion_real IS NULL "
                + "ORDER BY CASE WHEN p.fecha_devolucion_esperada < ? THEN 0 ELSE 1 END, p.fecha_devolucion_esperada ASC",
                PrestamoDao::map, Fechas.ahora(), Fechas.ahora());
    }

    // ------------------------------------------------------------------ operaciones (dentro de transacción)

    public static Prestamo activoPorIdBloqueado(Connection c, int id) {
        return Db.one(c, "SELECT * FROM prestamos WITH (UPDLOCK, ROWLOCK) WHERE id = ? AND estado = 'activo'", PrestamoDao::map, id);
    }

    /** Id del préstamo activo más reciente de ese usuario para ese equipo (o null). */
    public static Integer idActivoDe(Connection c, int usuarioId, int equipoId) {
        return Db.one(c, "SELECT TOP (1) id FROM prestamos WHERE usuario_id = ? AND equipo_id = ? AND estado = 'activo' ORDER BY id DESC",
                rs -> rs.getInt(1), usuarioId, equipoId);
    }

    public static long insertar(Connection c, int usuarioId, int equipoId, LocalDateTime fechaPrestamo,
                                LocalDateTime fechaEsperada, String observaciones) {
        return Db.insert(c, "INSERT INTO prestamos (usuario_id, equipo_id, fecha_prestamo, fecha_devolucion_esperada, observaciones, estado) "
                + "VALUES (?, ?, ?, ?, ?, 'activo')", usuarioId, equipoId, fechaPrestamo, fechaEsperada, observaciones);
    }

    /** Cierra el préstamo. notaAdicional se concatena a las observaciones existentes. */
    public static void registrarDevolucion(Connection c, int id, LocalDateTime fechaReal, String estado, String notaAdicional) {
        Db.update(c, "UPDATE prestamos SET fecha_devolucion_real = ?, estado = ?, observaciones = CONCAT(ISNULL(observaciones, N''), ?) WHERE id = ?",
                fechaReal, estado, notaAdicional, id);
    }

    public static void marcarDevuelto(Connection c, int id, LocalDateTime fechaReal) {
        Db.update(c, "UPDATE prestamos SET fecha_devolucion_real = ?, estado = 'devuelto' WHERE id = ?", fechaReal, id);
    }

    // ------------------------------------------------------------------ reportes

    /** Todos los préstamos de un usuario (para su historial en reportes). */
    public static List<Prestamo> porUsuario(int usuarioId) {
        return Db.list("SELECT p.*, e.nombre AS equipo_nombre, e.codigo_inventario, c.nombre AS categoria_nombre, "
                + "u.nombre AS usuario_nombre, u.email AS usuario_email "
                + "FROM prestamos p JOIN equipos e ON p.equipo_id = e.id "
                + "LEFT JOIN categorias c ON e.categoria_id = c.id "
                + "JOIN usuarios u ON p.usuario_id = u.id "
                + "WHERE p.usuario_id = ? ORDER BY p.fecha_prestamo DESC",
                PrestamoDao::map, usuarioId);
    }

    public static List<Prestamo> reporte(LocalDate desde, LocalDate hasta, Integer categoriaId, String estado) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, e.nombre AS equipo_nombre, e.codigo_inventario, c.nombre AS categoria_nombre, u.nombre AS usuario_nombre "
                + "FROM prestamos p JOIN equipos e ON p.equipo_id = e.id LEFT JOIN categorias c ON e.categoria_id = c.id "
                + "JOIN usuarios u ON p.usuario_id = u.id WHERE p.fecha_prestamo BETWEEN ? AND ?");
        List<Object> p = new ArrayList<>();
        p.add(desde.atStartOfDay());
        p.add(hasta.atTime(23, 59, 59));
        if (categoriaId != null) {
            sql.append(" AND e.categoria_id = ?");
            p.add(categoriaId);
        }
        if (!estado.isEmpty()) {
            sql.append(" AND p.estado = ?");
            p.add(estado);
        }
        sql.append(" ORDER BY p.fecha_prestamo DESC");
        return Db.list(sql.toString(), PrestamoDao::map, p.toArray());
    }

    public static List<TopEquipo> topEquipos(LocalDate desde, LocalDate hasta) {
        return Db.list("SELECT TOP (10) e.nombre, e.codigo_inventario, COUNT(p.id) AS total_prestamos "
                + "FROM prestamos p JOIN equipos e ON p.equipo_id = e.id "
                + "WHERE p.fecha_prestamo BETWEEN ? AND ? "
                + "GROUP BY e.id, e.nombre, e.codigo_inventario ORDER BY total_prestamos DESC",
                rs -> new TopEquipo(rs.getString("nombre"), rs.getString("codigo_inventario"), rs.getLong("total_prestamos")),
                desde.atStartOfDay(), hasta.atTime(23, 59, 59));
    }
}
