package edu.universidad.presta.dao;

import edu.universidad.presta.model.MensajeIA;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Persistencia del historial privado del asistente por usuario. */
public final class AsistenteDao {
    private AsistenteDao() {}

    public static long crearConversacion(int usuarioId, String titulo) {
        return Db.insert("INSERT INTO ia_conversaciones (usuario_id, titulo) VALUES (?, ?)", usuarioId, limitar(titulo, 150));
    }

    public static boolean pertenece(long conversacionId, int usuarioId) {
        return Db.scalar("SELECT COUNT(*) FROM ia_conversaciones WHERE id = ? AND usuario_id = ?", conversacionId, usuarioId) > 0;
    }

    public static void guardarMensaje(long conversacionId, String rol, String contenido) {
        Db.update("INSERT INTO ia_mensajes (conversacion_id, rol, contenido) VALUES (?, ?, ?)",
                conversacionId, rol, limitar(contenido, 12000));
        Db.update("UPDATE ia_conversaciones SET fecha_actualizacion = SYSDATETIME() WHERE id = ?", conversacionId);
    }

    public static List<MensajeIA> historial(long conversacionId, int limite) {
        List<MensajeIA> lista = Db.list("SELECT TOP (?) rol, contenido FROM ia_mensajes WHERE conversacion_id = ? ORDER BY id DESC",
                rs -> new MensajeIA(rs.getString("rol"), rs.getString("contenido")), Math.max(1, Math.min(12, limite)), conversacionId);
        Collections.reverse(lista);
        return lista;
    }

    private static String limitar(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
