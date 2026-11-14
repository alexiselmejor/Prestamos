package edu.universidad.presta.model;

import java.time.LocalDateTime;

/** Registro de mantenimiento preventivo o correctivo de un equipo. */
public class Mantenimiento {
    private int id;
    private int equipoId;
    private String equipoNombre;
    private String codigoInventario;
    private String tipo;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaProgramada;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String observaciones;
    private String responsable;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEquipoId() { return equipoId; }
    public void setEquipoId(int equipoId) { this.equipoId = equipoId; }
    public String getEquipoNombre() { return equipoNombre; }
    public void setEquipoNombre(String equipoNombre) { this.equipoNombre = equipoNombre; }
    public String getCodigoInventario() { return codigoInventario; }
    public void setCodigoInventario(String codigoInventario) { this.codigoInventario = codigoInventario; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDateTime fechaProgramada) { this.fechaProgramada = fechaProgramada; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
}
