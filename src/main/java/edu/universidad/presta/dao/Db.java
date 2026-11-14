package edu.universidad.presta.dao;

import edu.universidad.presta.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper JDBC: ejecuta consultas con PreparedStatement y maneja transacciones.
 * Las versiones sin Connection abren y cierran su propia conexión; las que reciben
 * Connection participan en la transacción abierta con {@link #tx(Work)}.
 */
public final class Db {

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface Work<T> {
        T run(Connection c) throws SQLException;
    }

    private Db() {
    }

    // ------------------------------------------------------------------ consultas

    public static <T> List<T> list(String sql, RowMapper<T> m, Object... p) {
        try (Connection c = Database.getConnection()) {
            return list(c, sql, m, p);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static <T> List<T> list(Connection c, String sql, RowMapper<T> m, Object... p) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(m.map(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    /** Primera fila o null. */
    public static <T> T one(String sql, RowMapper<T> m, Object... p) {
        List<T> r = list(sql, m, p);
        return r.isEmpty() ? null : r.get(0);
    }

    public static <T> T one(Connection c, String sql, RowMapper<T> m, Object... p) {
        List<T> r = list(c, sql, m, p);
        return r.isEmpty() ? null : r.get(0);
    }

    /** Primera columna de la primera fila como número (COUNT, etc.). 0 si no hay fila. */
    public static long scalar(String sql, Object... p) {
        Long v = one(sql, rs -> rs.getLong(1), p);
        return v == null ? 0L : v;
    }

    public static long scalar(Connection c, String sql, Object... p) {
        Long v = one(c, sql, rs -> rs.getLong(1), p);
        return v == null ? 0L : v;
    }

    // ------------------------------------------------------------------ escritura

    public static int update(String sql, Object... p) {
        try (Connection c = Database.getConnection()) {
            return update(c, sql, p);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static int update(Connection c, String sql, Object... p) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    /** INSERT que devuelve el id autogenerado. */
    public static long insert(String sql, Object... p) {
        try (Connection c = Database.getConnection()) {
            return insert(c, sql, p);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static long insert(Connection c, String sql, Object... p) {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    // ------------------------------------------------------------------ transacciones

    /**
     * Ejecuta el trabajo en una transacción: commit si termina bien, rollback si lanza cualquier excepción
     * (las excepciones de negocio se relanzan tal cual).
     */
    public static <T> T tx(Work<T> trabajo) {
        Connection c = null;
        try {
            c = Database.getConnection();
            c.setAutoCommit(false);
            T r = trabajo.run(c);
            c.commit();
            return r;
        } catch (SQLException e) {
            rollback(c);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(c);
            throw e;
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException ignorada) {
                    // nada que hacer
                }
            }
        }
    }

    private static void rollback(Connection c) {
        if (c != null) {
            try {
                c.rollback();
            } catch (SQLException ignorada) {
                // la conexión ya falló; se cierra en finally
            }
        }
    }

    // ------------------------------------------------------------------ utilidades para mappers

    private static void bind(PreparedStatement ps, Object[] p) throws SQLException {
        for (int i = 0; i < p.length; i++) {
            Object v = p[i];
            if (v == null) {
                ps.setNull(i + 1, Types.VARCHAR);
            } else if (v instanceof LocalDateTime) {
                ps.setObject(i + 1, v);  // LocalDateTime -> datetime2 (sin conversiones de zona horaria)
            } else if (v instanceof String) {
                ps.setString(i + 1, (String) v);
            } else {
                ps.setObject(i + 1, v);
            }
        }
    }

    public static LocalDateTime dt(ResultSet rs, String col) throws SQLException {
        return rs.getObject(col, LocalDateTime.class);
    }

    public static Integer integer(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    /** true si el ResultSet tiene una columna con ese nombre/alias. */
    public static boolean has(ResultSet rs, String col) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (col.equalsIgnoreCase(md.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }
}
