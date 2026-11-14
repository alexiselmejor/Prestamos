package edu.universidad.presta.model;

import java.time.LocalDateTime;

/** Equipo del inventario (tabla equipos). */
public class Equipo {

    private int id;
    private String codigoInventario;
    private String nombre;
    private String descripcion;
    private Integer categoriaId;
    private String estado;
    private String ubicacion;
    private LocalDateTime fechaRegistro;
    private String categoriaNombre;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoInventario() {
        return codigoInventario;
    }

    public void setCodigoInventario(String codigoInventario) {
        this.codigoInventario = codigoInventario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /** Texto en minúsculas para el filtro en tiempo real del inventario (código, nombre, categoría, ubicación, descripción). */
    public String getTextoBusqueda() {
        return unir(codigoInventario, nombre, categoriaNombre == null ? "Sin categoría" : categoriaNombre, ubicacion, descripcion);
    }

    /** Texto en minúsculas para el filtro del catálogo de usuarios (nombre, código, categoría, ubicación). */
    public String getTextoCatalogo() {
        return unir(nombre, codigoInventario, categoriaNombre == null ? "General" : categoriaNombre, ubicacion);
    }

    private static String unir(String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (p != null && !p.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(p);
            }
        }
        return sb.toString().toLowerCase(java.util.Locale.ROOT);
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

}
