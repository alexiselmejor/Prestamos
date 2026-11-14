package edu.universidad.presta.util;

import edu.universidad.presta.config.AppConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Utilidades de fecha y hora (zona horaria de la aplicación: America/Lima). */
public final class Fechas {

    private static final DateTimeFormatter DB = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DB_SIN_SEG = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter SOLO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Fechas() {
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(AppConfig.zone());
    }

    public static LocalDate hoy() {
        return LocalDate.now(AppConfig.zone());
    }

    /** Acepta "2026-10-09T14:30", "2026-10-09 14:30", "2026-10-09 14:30:15" y "2026-10-09 14:30:15.0". */
    public static LocalDateTime parse(String texto) {
        if (texto == null) {
            return null;
        }
        String s = texto.trim().replace('T', ' ');
        if (s.isEmpty()) {
            return null;
        }
        s = s.replaceFirst("\\.\\d+$", "");
        try {
            return s.length() <= 16 ? LocalDateTime.parse(s, DB_SIN_SEG) : LocalDateTime.parse(s, DB);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Fecha (sin hora) en formato yyyy-MM-dd; devuelve el valor por defecto si no es válida. */
    public static LocalDate parseFecha(String texto, LocalDate porDefecto) {
        if (texto == null || texto.trim().isEmpty()) {
            return porDefecto;
        }
        try {
            return LocalDate.parse(texto.trim(), SOLO_FECHA);
        } catch (DateTimeParseException e) {
            return porDefecto;
        }
    }

    /** Valor para guardar/consultar en la base (yyyy-MM-dd HH:mm:ss). */
    public static String db(LocalDateTime f) {
        return f == null ? null : f.format(DB);
    }

    /** Valor para un input datetime-local (yyyy-MM-ddTHH:mm). */
    public static String input(LocalDateTime f) {
        return f == null ? "" : f.format(INPUT);
    }
}
