package com.duoc.ms_operaciones.model;

import jakarta.persistence.*;

@Entity
@Table(name = "validaciones")
public class Validacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tareaId;
    private Long validadorId;
    private String resultado;
    private String comentario;
    private String fechaHoraValidacion;

    public Validacion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTareaId() { return tareaId; }
    public void setTareaId(Long tareaId) { this.tareaId = tareaId; }
    public Long getValidadorId() { return validadorId; }
    public void setValidadorId(Long validadorId) { this.validadorId = validadorId; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public String getFechaHoraValidacion() { return fechaHoraValidacion; }
    public void setFechaHoraValidacion(String fechaHoraValidacion) { this.fechaHoraValidacion = fechaHoraValidacion; }
}