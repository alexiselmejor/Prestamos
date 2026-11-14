package edu.universidad.presta.model;

import java.time.LocalDateTime;

/** Indicador preventivo calculado a partir de préstamos e historial, no es un diagnóstico automático. */
public class AlertaMantenimiento {
    private int equipoId;
    private String equipoNombre;
    private String codigoInventario;
    private String estadoEquipo;
    private String categoria;
    private int prestamos90Dias;
    private int mantenimientos12Meses;
    private LocalDateTime ultimoMantenimiento;
    private int puntaje;
    private String prioridad;
    private String motivo;

    public int getEquipoId() { return equipoId; }
    public void setEquipoId(int equipoId) { this.equipoId = equipoId; }
    public String getEquipoNombre() { return equipoNombre; }
    public void setEquipoNombre(String equipoNombre) { this.equipoNombre = equipoNombre; }
    public String getCodigoInventario() { return codigoInventario; }
    public void setCodigoInventario(String codigoInventario) { this.codigoInventario = codigoInventario; }
    public String getEstadoEquipo() { return estadoEquipo; }
    public void setEstadoEquipo(String estadoEquipo) { this.estadoEquipo = estadoEquipo; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getPrestamos90Dias() { return prestamos90Dias; }
    public void setPrestamos90Dias(int prestamos90Dias) { this.prestamos90Dias = prestamos90Dias; }
    public int getMantenimientos12Meses() { return mantenimientos12Meses; }
    public void setMantenimientos12Meses(int mantenimientos12Meses) { this.mantenimientos12Meses = mantenimientos12Meses; }
    public LocalDateTime getUltimoMantenimiento() { return ultimoMantenimiento; }
    public void setUltimoMantenimiento(LocalDateTime ultimoMantenimiento) { this.ultimoMantenimiento = ultimoMantenimiento; }
    public int getPuntaje() { return puntaje; }
    public void setPuntaje(int puntaje) { this.puntaje = puntaje; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
