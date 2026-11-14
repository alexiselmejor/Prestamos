package edu.universidad.presta.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Funciones EL usadas en los JSP: ${f:h(x)}, ${f:fecha(x, 'dd/MM/yyyy')}, ${f:cap(x)}, ${f:hace(x)}. */
public final class Fn {

    private Fn() {
    }

    /** Escapa HTML (equivale a htmlspecialchars). Null-safe. */
    public static String h(Object o) {
        if (o == null) {
            return "";
        }
        String s = o.toString();
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#039;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Formatea una fecha/hora; devuelve "" si es nula. */
    public static String fecha(LocalDateTime f, String patron) {
        return f == null ? "" : f.format(DateTimeFormatter.ofPattern(patron));
    }

    /** Primera letra en mayúscula (equivale a ucfirst). */
    public static String cap(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** Tiempo transcurrido en palabras: "Ahora mismo", "Hace 5 minutos", "Hace 2 horas", "Hace 3 días". */
    public static String hace(LocalDateTime f) {
        if (f == null) {
            return "";
        }
        long minutos = Duration.between(f, Fechas.ahora()).toMinutes();
        if (minutos < 1) {
            return "Ahora mismo";
        }
        if (minutos < 60) {
            return "Hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        }
        long horas = minutos / 60;
        if (horas < 24) {
            return "Hace " + horas + (horas == 1 ? " hora" : " horas");
        }
        long dias = horas / 24;
        if (dias < 7) {
            return "Hace " + dias + (dias == 1 ? " día" : " días");
        }
        return "El " + f.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
