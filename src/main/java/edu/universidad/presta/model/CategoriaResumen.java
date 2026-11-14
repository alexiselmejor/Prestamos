package edu.universidad.presta.model;

/** Categoría con la cantidad de equipos que contiene (chips de filtro del catálogo). */
public class CategoriaResumen {

    private final int id;
    private final String nombre;
    private final int total;
    private final int disponibles;

    public CategoriaResumen(int id, String nombre, int total, int disponibles) {
        this.id = id;
        this.nombre = nombre;
        this.total = total;
        this.disponibles = disponibles;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getTotal() {
        return total;
    }

    public int getDisponibles() {
        return disponibles;
    }
}
