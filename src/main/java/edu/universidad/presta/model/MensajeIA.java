package edu.universidad.presta.model;

/** Mensaje guardado en el historial del asistente. */
public class MensajeIA {
    private String rol;
    private String contenido;
    public MensajeIA(String rol, String contenido) { this.rol = rol; this.contenido = contenido; }
    public String getRol() { return rol; }
    public String getContenido() { return contenido; }
}
