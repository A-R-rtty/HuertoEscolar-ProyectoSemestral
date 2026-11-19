package com.duoc.ms_catalogo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cultivos")
public class Cultivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombreComun;
    private String descripcion;
    private Long sectorId;
    private String fechaSiembra;
    private String fechaTrasplante;
    private String fechaEstimadaCosecha;
    private Integer frecuenciaRiegoDias;
    private String estadoDesarrollo;
    private String observaciones;

    public Cultivo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreComun() { return nombreComun; }
    public void setNombreComun(String nombreComun) { this.nombreComun = nombreComun; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }
    public String getFechaSiembra() { return fechaSiembra; }
    public void setFechaSiembra(String fechaSiembra) { this.fechaSiembra = fechaSiembra; }
    public String getFechaTrasplante() { return fechaTrasplante; }
    public void setFechaTrasplante(String fechaTrasplante) { this.fechaTrasplante = fechaTrasplante; }
    public String getFechaEstimadaCosecha() { return fechaEstimadaCosecha; }
    public void setFechaEstimadaCosecha(String fechaEstimadaCosecha) { this.fechaEstimadaCosecha = fechaEstimadaCosecha; }
    public Integer getFrecuenciaRiegoDias() { return frecuenciaRiegoDias; }
    public void setFrecuenciaRiegoDias(Integer frecuenciaRiegoDias) { this.frecuenciaRiegoDias = frecuenciaRiegoDias; }
    public String getEstadoDesarrollo() { return estadoDesarrollo; }
    public void setEstadoDesarrollo(String estadoDesarrollo) { this.estadoDesarrollo = estadoDesarrollo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}