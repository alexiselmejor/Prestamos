package edu.universidad.presta.dao;

import edu.universidad.presta.model.AlertaMantenimiento;
import edu.universidad.presta.model.Mantenimiento;
import edu.universidad.presta.util.Fechas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/** Registro de mantenimiento y reglas de alerta preventiva. */
public final class MantenimientoDao {
    private MantenimientoDao() {}

    private static Mantenimiento map(ResultSet rs) throws SQLException {
        Mantenimiento m = new Mantenimiento();
        m.setId(rs.getInt("id"));
        m.setEquipoId(rs.getInt("equipo_id"));
        m.setEquipoNombre(rs.getString("equipo_nombre"));
        m.setCodigoInventario(rs.getString("codigo_inventario"));
        m.setTipo(rs.getString("tipo"));
        m.setDescripcion(rs.getString("descripcion"));
        m.setEstado(rs.getString("estado"));
        m.setFechaProgramada(Db.dt(rs, "fecha_programada"));
        m.setFechaInicio(Db.dt(rs, "fecha_inicio"));
        m.setFechaFin(Db.dt(rs, "fecha_fin"));
        m.setObservaciones(rs.getString("observaciones"));
        m.setResponsable(rs.getString("responsable"));
        return m;
    }

    public static List<Mantenimiento> recientes(int limite) {
        return Db.list("SELECT TOP (?) m.*, e.nombre AS equipo_nombre, e.codigo_inventario, u.nombre AS responsable "
                + "FROM mantenimientos m JOIN equipos e ON e.id=m.equipo_id LEFT JOIN usuarios u ON u.id=m.usuario_id "
                + "ORDER BY m.fecha_registro DESC, m.id DESC", MantenimientoDao::map, Math.max(1, Math.min(100, limite)));
    }

    public static long registrar(int equipoId, int usuarioId, String tipo, String descripcion, LocalDateTime fechaProgramada) {
        return Db.insert("INSERT INTO mantenimientos (equipo_id, usuario_id, tipo, descripcion, fecha_programada, estado) "
                + "VALUES (?, ?, ?, ?, ?, 'programado')", equipoId, usuarioId, tipo, descripcion, fechaProgramada);
    }

    public static boolean iniciar(int id, int usuarioId) {
        return Db.tx(c -> {
            Mantenimiento m = Db.one(c, "SELECT * FROM mantenimientos WITH (UPDLOCK, ROWLOCK) WHERE id=? AND estado='programado'",
                    rs -> {
                        Mantenimiento x = new Mantenimiento(); x.setId(rs.getInt("id")); x.setEquipoId(rs.getInt("equipo_id"));
                        x.setDescripcion(rs.getString("descripcion")); return x;
                    }, id);
            if (m == null) return false;
            if (Db.scalar(c, "SELECT COUNT(*) FROM prestamos WHERE equipo_id=? AND estado='activo'", m.getEquipoId()) > 0) return false;
            if (Db.scalar(c, "SELECT COUNT(*) FROM mantenimientos WHERE equipo_id=? AND estado='en_proceso'", m.getEquipoId()) > 0) return false;
            String estadoEquipo = Db.one(c, "SELECT estado FROM equipos WHERE id=?", rs -> rs.getString(1), m.getEquipoId());
            if (!"disponible".equals(estadoEquipo) && !"mantenimiento".equals(estadoEquipo)) return false;
            Db.update(c, "UPDATE mantenimientos SET estado='en_proceso', fecha_inicio=? WHERE id=?", Fechas.ahora(), id);
            Db.update(c, "UPDATE equipos SET estado='mantenimiento' WHERE id=? AND estado IN ('disponible','mantenimiento')", m.getEquipoId());
            HistorialDao.registrar(c, m.getEquipoId(), usuarioId, "mantenimiento", "Inicio de mantenimiento: " + m.getDescripcion());
            return true;
        });
    }

    public static boolean finalizar(int id, int usuarioId, String observaciones) {
        return Db.tx(c -> {
            Mantenimiento m = Db.one(c, "SELECT * FROM mantenimientos WITH (UPDLOCK, ROWLOCK) WHERE id=? AND estado='en_proceso'",
                    rs -> {
                        Mantenimiento x = new Mantenimiento(); x.setId(rs.getInt("id")); x.setEquipoId(rs.getInt("equipo_id"));
                        x.setDescripcion(rs.getString("descripcion")); return x;
                    }, id);
            if (m == null) return false;
            Db.update(c, "UPDATE mantenimientos SET estado='completado', fecha_fin=?, observaciones=? WHERE id=?",
                    Fechas.ahora(), observaciones, id);
            // No liberar el equipo si por alguna inconsistencia aún tiene un préstamo activo.
            Db.update(c, "UPDATE equipos SET estado='disponible' WHERE id=? AND estado='mantenimiento' "
                    + "AND NOT EXISTS (SELECT 1 FROM prestamos p WHERE p.equipo_id=equipos.id AND p.estado='activo') "
                    + "AND NOT EXISTS (SELECT 1 FROM mantenimientos otro WHERE otro.equipo_id=equipos.id AND otro.estado='en_proceso' AND otro.id<>?)",
                    m.getEquipoId(), id);
            HistorialDao.registrar(c, m.getEquipoId(), usuarioId, "mantenimiento", "Mantenimiento completado: " + m.getDescripcion());
            return true;
        });
    }

    public static List<AlertaMantenimiento> alertasPreventivas() {
        List<AlertaMantenimiento> datos = Db.list(
                "SELECT e.id, e.nombre, e.codigo_inventario, e.estado, COALESCE(c.nombre,'General') AS categoria, "
                + "COUNT(DISTINCT CASE WHEN p.fecha_prestamo >= DATEADD(day,-90,SYSDATETIME()) THEN p.id END) AS prestamos_90, "
                + "COUNT(DISTINCT CASE WHEN m.fecha_registro >= DATEADD(day,-365,SYSDATETIME()) AND m.estado IN ('en_proceso','completado') THEN m.id END) AS mantenimientos_12, "
                + "COUNT(DISTINCT CASE WHEN h.fecha >= DATEADD(day,-365,SYSDATETIME()) THEN h.id END) AS mantenimientos_historial_12, "
                + "MAX(CASE WHEN m.estado='completado' THEN m.fecha_fin END) AS ultimo_mantenimiento, "
                + "MAX(h.fecha) AS ultimo_historial_mantenimiento "
                + "FROM equipos e LEFT JOIN categorias c ON c.id=e.categoria_id "
                + "LEFT JOIN prestamos p ON p.equipo_id=e.id "
                + "LEFT JOIN mantenimientos m ON m.equipo_id=e.id "
                + "LEFT JOIN historial_movimientos h ON h.equipo_id=e.id AND h.tipo='mantenimiento' "
                + "WHERE e.estado <> 'dado_de_baja' "
                + "GROUP BY e.id,e.nombre,e.codigo_inventario,e.estado,c.nombre ORDER BY e.nombre",
                rs -> {
                    AlertaMantenimiento a = new AlertaMantenimiento();
                    a.setEquipoId(rs.getInt("id")); a.setEquipoNombre(rs.getString("nombre"));
                    a.setCodigoInventario(rs.getString("codigo_inventario")); a.setEstadoEquipo(rs.getString("estado"));
                    a.setCategoria(rs.getString("categoria")); a.setPrestamos90Dias(rs.getInt("prestamos_90"));
                    int mantenimientosRegistrados = rs.getInt("mantenimientos_12");
                    int mantenimientosHistoricos = rs.getInt("mantenimientos_historial_12");
                    a.setMantenimientos12Meses(mantenimientosRegistrados > 0 ? mantenimientosRegistrados : mantenimientosHistoricos);
                    LocalDateTime ultimo = Db.dt(rs, "ultimo_mantenimiento");
                    LocalDateTime ultimoHistorico = Db.dt(rs, "ultimo_historial_mantenimiento");
                    a.setUltimoMantenimiento(ultimo == null || (ultimoHistorico != null && ultimoHistorico.isAfter(ultimo)) ? ultimoHistorico : ultimo);
                    return a;
                });
        LocalDateTime ahora = Fechas.ahora();
        for (AlertaMantenimiento a : datos) {
            int score = 0;
            StringBuilder motivo = new StringBuilder();
            if ("mantenimiento".equalsIgnoreCase(a.getEstadoEquipo())) {
                score += 100; motivo.append("Equipo actualmente en mantenimiento. ");
            }
            if (a.getPrestamos90Dias() >= 8) { score += 35; motivo.append("Uso elevado: ").append(a.getPrestamos90Dias()).append(" préstamos en 90 días. "); }
            else if (a.getPrestamos90Dias() >= 5) { score += 20; motivo.append("Uso frecuente: ").append(a.getPrestamos90Dias()).append(" préstamos en 90 días. "); }
            long diasSinMantenimiento = a.getUltimoMantenimiento() == null ? 9999
                    : java.time.Duration.between(a.getUltimoMantenimiento(), ahora).toDays();
            if (diasSinMantenimiento >= 180 && a.getPrestamos90Dias() >= 3) {
                score += 35; motivo.append("Sin mantenimiento registrado hace más de 180 días. ");
            } else if (diasSinMantenimiento >= 90 && a.getPrestamos90Dias() >= 5) {
                score += 20; motivo.append("Revisar intervalo desde el último mantenimiento. ");
            }
            if (a.getMantenimientos12Meses() >= 3) { score += 15; motivo.append("Registra varios mantenimientos durante el último año. "); }
            score = Math.min(100, score);
            a.setPuntaje(score);
            a.setPrioridad(score >= 70 ? "Alta" : score >= 35 ? "Media" : "Baja");
            a.setMotivo(motivo.length() == 0 ? "Sin indicadores preventivos relevantes con los datos actuales." : motivo.toString().trim());
        }
        return datos.stream().filter(a -> a.getPuntaje() > 0)
                .sorted((a,b) -> Integer.compare(b.getPuntaje(), a.getPuntaje())).toList();
    }
}
