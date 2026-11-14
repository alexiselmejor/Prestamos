package edu.universidad.presta.model;

/** Fila del ranking "Equipos más prestados" de los reportes. */
public class TopEquipo {

    private final String nombre;
    private final String codigoInventario;
    private final long totalPrestamos;

    public TopEquipo(String nombre, String codigoInventario, long totalPrestamos) {
        this.nombre = nombre;
        this.codigoInventario = codigoInventario;
        this.totalPrestamos = totalPrestamos;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigoInventario() {
        return codigoInventario;
    }

    public long getTotalPrestamos() {
        return totalPrestamos;
    }
}
