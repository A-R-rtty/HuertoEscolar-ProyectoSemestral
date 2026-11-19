package com.duoc.ms_catalogo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigoFicticio;
    private String nombreFicticio;
    private String correoFicticio;
    private String rol;
    private String tallerGrupo;
    private Boolean activo = true;

    public Usuario() {}

    public Usuario(String codigoFicticio, String nombreFicticio, String correoFicticio, String rol, String tallerGrupo) {
        this.codigoFicticio = codigoFicticio;
        this.nombreFicticio = nombreFicticio;
        this.correoFicticio = correoFicticio;
        this.rol = rol;
        this.tallerGrupo = tallerGrupo;
        this.activo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoFicticio() { return codigoFicticio; }
    public void setCodigoFicticio(String codigoFicticio) { this.codigoFicticio = codigoFicticio; }
    public String getNombreFicticio() { return nombreFicticio; }
    public void setNombreFicticio(String nombreFicticio) { this.nombreFicticio = nombreFicticio; }
    public String getCorreoFicticio() { return correoFicticio; }
    public void setCorreoFicticio(String correoFicticio) { this.correoFicticio = correoFicticio; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getTallerGrupo() { return tallerGrupo; }
    public void setTallerGrupo(String tallerGrupo) { this.tallerGrupo = tallerGrupo; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}