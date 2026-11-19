package com.duoc.ms_operaciones.controller;

import com.duoc.ms_operaciones.model.*;
import com.duoc.ms_operaciones.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/operaciones")
@CrossOrigin(origins = "*")
public class OperacionesController {

    private final TareaRepository tareaRepo;
    private final ValidacionRepository validacionRepo;
    private final ReporteProblemaRepository reporteRepo;

    public OperacionesController(TareaRepository tareaRepo, ValidacionRepository validacionRepo, ReporteProblemaRepository reporteRepo) {
        this.tareaRepo = tareaRepo;
        this.validacionRepo = validacionRepo;
        this.reporteRepo = reporteRepo;
    }

    // --- TAREAS CRUD Y FLUJO DE ESTADOS ---
    @GetMapping("/tareas")
    public List<Tarea> listarTareas() {
        return tareaRepo.findAll();
    }

    @PostMapping("/tareas")
    public Tarea crearTarea(@RequestBody Tarea tarea) {
        tarea.setId(null);
        return tareaRepo.save(tarea);
    }

    @PutMapping("/tareas/{id}")
    public ResponseEntity<Tarea> actualizarTarea(@PathVariable Long id, @RequestBody Tarea datos) {
        return tareaRepo.findById(id).map(existente -> {
            existente.setTipoTarea(datos.getTipoTarea());
            existente.setDescripcion(datos.getDescripcion());
            existente.setSectorId(datos.getSectorId());
            existente.setCultivoId(datos.getCultivoId());
            existente.setFechaProgramada(datos.getFechaProgramada());
            existente.setResponsableId(datos.getResponsableId());
            existente.setEstado(datos.getEstado());
            existente.setFechaHoraRealizacion(datos.getFechaHoraRealizacion());
            existente.setComentarioEstudiante(datos.getComentarioEstudiante());
            return ResponseEntity.ok(tareaRepo.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- VALIDACIONES ---
    @GetMapping("/validaciones")
    public List<Validacion> listarValidaciones() {
        return validacionRepo.findAll();
    }

    @PostMapping("/validaciones")
    public ResponseEntity<Validacion> registrarValidacion(@RequestBody Validacion validacion) {
        if (validacion.getValidadorId() != null && validacion.getTareaId() != null) {
            Tarea tarea = tareaRepo.findById(validacion.getTareaId()).orElse(null);
            if (tarea != null && validacion.getValidadorId().equals(tarea.getResponsableId())) {
                return ResponseEntity.badRequest().build(); // Regla: no puede aprobar su propia tarea
            }
            if (tarea != null) {
                tarea.setEstado("APROBADA".equalsIgnoreCase(validacion.getResultado()) ? "VALIDADA" : "RECHAZADA");
                tareaRepo.save(tarea);
            }
        }
        validacion.setId(null);
        if (validacion.getFechaHoraValidacion() == null) {
            validacion.setFechaHoraValidacion(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        return ResponseEntity.ok(validacionRepo.save(validacion));
    }

    // --- REPORTES DE PROBLEMAS CRUD ---
    @GetMapping("/reportes")
    public List<ReporteProblema> listarReportes() {
        return reporteRepo.findAll();
    }

    @PostMapping("/reportes")
    public ReporteProblema crearReporte(@RequestBody ReporteProblema reporte) {
        reporte.setId(null);
        return reporteRepo.save(reporte);
    }

    @PutMapping("/reportes/{id}")
    public ResponseEntity<ReporteProblema> actualizarReporte(@PathVariable Long id, @RequestBody ReporteProblema datos) {
        return reporteRepo.findById(id).map(existente -> {
            existente.setEstado(datos.getEstado());
            existente.setComentarioAdmin(datos.getComentarioAdmin());
            existente.setFechaCierre(datos.getFechaCierre());
            return ResponseEntity.ok(reporteRepo.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }
}