package edu.universidad.presta.model;

/** Presentación de un estado de equipo (etiqueta, icono Bootstrap y colores). */
public class EstadoInfo {

    private final String label;
    private final String icono;
    private final String color;       // clases de fondo: "bg-warning text-dark"
    private final String colorTexto;  // clases de texto: "text-warning text-dark"

    public EstadoInfo(String label, String icono, String color) {
        this.label = label;
        this.icono = icono;
        this.color = color;
        this.colorTexto = color.replace("bg-", "text-");
    }

    public String getLabel() {
        return label;
    }

    public String getIcono() {
        return icono;
    }

    public String getColor() {
        return color;
    }

    public String getColorTexto() {
        return colorTexto;
    }
}
