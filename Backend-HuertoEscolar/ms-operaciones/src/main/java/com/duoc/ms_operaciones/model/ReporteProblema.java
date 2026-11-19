package com.duoc.ms_operaciones.model;
import jakarta.persistence.*;

@Entity
@Table(name = "reportes_problemas")
public class ReporteProblema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long sectorId;
    private Long cultivoId;
    private String tipoProblema;
    private String descripcion;
    private String fechaHoraReporte;
    private Long reportadoPorId;
    private String estado;
    private String comentarioAdmin;
    private String fechaCierre;

    public ReporteProblema() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }
    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }
    public String getTipoProblema() { return tipoProblema; }
    public void setTipoProblema(String tipoProblema) { this.tipoProblema = tipoProblema; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFechaHoraReporte() { return fechaHoraReporte; }
    public void setFechaHoraReporte(String fechaHoraReporte) { this.fechaHoraReporte = fechaHoraReporte; }
    public Long getReportadoPorId() { return reportadoPorId; }
    public void setReportadoPorId(Long reportadoPorId) { this.reportadoPorId = reportadoPorId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getComentarioAdmin() { return comentarioAdmin; }
    public void setComentarioAdmin(String comentarioAdmin) { this.comentarioAdmin = comentarioAdmin; }
    public String getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(String fechaCierre) { this.fechaCierre = fechaCierre; }
}