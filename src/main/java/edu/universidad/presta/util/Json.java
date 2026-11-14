package edu.universidad.presta.util;

import edu.universidad.presta.model.Usuario;

import java.util.List;

/** Generador JSON mínimo para pasar datos a JavaScript sin depender de librerías externas. */
public final class Json {

    private Json() {
    }

    public static String texto(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '<': sb.append("\\u003c"); break;   // evita cerrar el <script>
                case '>': sb.append("\\u003e"); break;
                case '&': sb.append("\\u0026"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }

    /** Lista de usuarios para el buscador en vivo del formulario de préstamos. */
    public static String usuarios(List<Usuario> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            Usuario u = lista.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(u.getId())
              .append(",\"nombre\":").append(texto(u.getNombre()))
              .append(",\"email\":").append(texto(u.getEmail()))
              .append(",\"dni\":").append(texto(u.getDni()))
              .append(",\"celular\":").append(texto(u.getCelular()))
              .append('}');
        }
        return sb.append(']').toString();
    }
}
