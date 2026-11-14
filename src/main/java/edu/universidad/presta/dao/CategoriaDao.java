package edu.universidad.presta.dao;

import edu.universidad.presta.util.Fechas;
import edu.universidad.presta.model.Categoria;
import edu.universidad.presta.model.CategoriaResumen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class CategoriaDao {

    private CategoriaDao() {
    }

    private static Categoria map(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setFechaCreacion(Db.dt(rs, "fecha_creacion"));
        return c;
    }

    public static List<Categoria> listar() {
        return Db.list("SELECT * FROM categorias ORDER BY nombre", CategoriaDao::map);
    }

    /** Categorías con su cantidad de equipos, de mayor a menor (chips del inventario de encargados). */
    public static List<CategoriaResumen> listarConConteo() {
        return Db.list("SELECT c.id, c.nombre, COUNT(e.id) AS total FROM categorias c "
                + "LEFT JOIN equipos e ON e.categoria_id = c.id GROUP BY c.id, c.nombre ORDER BY total DESC, c.nombre ASC",
                rs -> new CategoriaResumen(rs.getInt("id"), rs.getString("nombre"), rs.getInt("total"), 0));
    }

    /** Categorías con equipos (total y disponibles) para el catálogo de usuarios; los equipos sin categoría van a "General". */
    public static List<CategoriaResumen> resumenCatalogo() {
        return Db.list("SELECT COALESCE(c.nombre, 'General') AS nombre, COUNT(e.id) AS total, "
                + "SUM(CASE WHEN e.estado = 'disponible' THEN 1 ELSE 0 END) AS disponibles "
                + "FROM equipos e LEFT JOIN categorias c ON e.categoria_id = c.id GROUP BY c.id, c.nombre ORDER BY total DESC",
                rs -> new CategoriaResumen(0, rs.getString("nombre"), rs.getInt("total"), rs.getInt("disponibles")));
    }

    /** Id de la categoría con ese nombre; la crea si no existe (se usa para "Accesorios"). */
    public static int idOCrear(String nombre, String descripcion) {
        Integer id = Db.one("SELECT id FROM categorias WHERE nombre = ?", rs -> rs.getInt(1), nombre);
        if (id != null) {
            return id;
        }
        return (int) Db.insert("INSERT INTO categorias (nombre, descripcion, fecha_creacion) VALUES (?, ?, ?)", nombre, descripcion, Fechas.ahora());
    }
}
