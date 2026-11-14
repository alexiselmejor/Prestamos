package edu.universidad.presta.model;

import java.time.LocalDateTime;

/**
 * Aviso que se muestra en la campana de la cabecera.
 * No se guarda en una tabla: se calcula a partir de las solicitudes y los préstamos,
 * y lo único que se persiste es qué avisos ya leyó cada usuario.
 */
public class Notificacion {

    /** Identificador estable (tipo + id del registro) para recordar si ya se leyó. */
    private String clave;
    /** solicitud | vencido | por_vencer */
    private String tipo;
    private String titulo;
    private String mensaje;
    private LocalDateTime fecha;
    /** Ruta interna a la que lleva el aviso (sin el contexto de la aplicación). */
    private String enlace;
    private boolean leida;

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    /** Icono de Remix Icon según el tipo de aviso. */
    public String getIcono() {
        if (tipo == null) {
            return "ri-notification-3-line";
        }
        switch (tipo) {
            case "solicitud":
                return "ri-inbox-fill";
            case "vencido":
                return "ri-error-warning-fill";
            case "por_vencer":
                return "ri-time-line";
            case "prestamo":
                return "ri-arrow-left-right-line";
            default:
                return "ri-notification-3-line";
        }
    }

    /** Color de Bootstrap para el icono. */
    public String getTono() {
        if (tipo == null) {
            return "primary";
        }
        switch (tipo) {
            case "vencido":
                return "danger";
            case "por_vencer":
                return "warning";
            case "prestamo":
                return "success";
            default:
                return "primary";
        }
    }
}
