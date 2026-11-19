package com.duoc.ms_catalogo.controller;

import com.duoc.ms_catalogo.model.*;
import com.duoc.ms_catalogo.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
@CrossOrigin(origins = "*")
public class CatalogoController {

    private final UsuarioRepository usuarioRepo;
    private final SectorRepository sectorRepo;
    private final CultivoRepository cultivoRepo;

    public CatalogoController(UsuarioRepository usuarioRepo, SectorRepository sectorRepo, CultivoRepository cultivoRepo) {
        this.usuarioRepo = usuarioRepo;
        this.sectorRepo = sectorRepo;
        this.cultivoRepo = cultivoRepo;
    }

    // --- USUARIOS ---
    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios() {
        return usuarioRepo.findAll();
    }

    // --- SECTORES CRUD ---
    @GetMapping("/sectores")
    public List<Sector> listarSectores() {
        return sectorRepo.findAll();
    }

    @PostMapping("/sectores")
    public Sector crearSector(@RequestBody Sector sector) {
        sector.setId(null);
        String hoy = LocalDate.now().toString();
        if (sector.getFechaCreacion() == null) sector.setFechaCreacion(hoy);
        sector.setFechaActualizacion(hoy);
        return sectorRepo.save(sector);
    }

    @PutMapping("/sectores/{id}")
    public ResponseEntity<Sector> actualizarSector(@PathVariable Long id, @RequestBody Sector datos) {
        return sectorRepo.findById(id).map(existente -> {
            existente.setCodigo(datos.getCodigo());
            existente.setNombre(datos.getNombre());
            existente.setDescripcion(datos.getDescripcion());
            existente.setUbicacion(datos.getUbicacion());
            existente.setEstado(datos.getEstado());
            existente.setFechaActualizacion(LocalDate.now().toString());
            return ResponseEntity.ok(sectorRepo.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/sectores/{id}")
    public ResponseEntity<Void> eliminarSector(@PathVariable Long id) {
        if (!sectorRepo.existsById(id)) return ResponseEntity.notFound().build();
        sectorRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- CULTIVOS CRUD ---
    @GetMapping("/cultivos")
    public List<Cultivo> listarCultivos() {
        return cultivoRepo.findAll();
    }

    @PostMapping("/cultivos")
    public Cultivo crearCultivo(@RequestBody Cultivo cultivo) {
        cultivo.setId(null);
        return cultivoRepo.save(cultivo);
    }

    @PutMapping("/cultivos/{id}")
    public ResponseEntity<Cultivo> actualizarCultivo(@PathVariable Long id, @RequestBody Cultivo datos) {
        return cultivoRepo.findById(id).map(existente -> {
            existente.setNombreComun(datos.getNombreComun());
            existente.setDescripcion(datos.getDescripcion());
            existente.setSectorId(datos.getSectorId());
            existente.setFechaSiembra(datos.getFechaSiembra());
            existente.setFechaTrasplante(datos.getFechaTrasplante());
            existente.setFechaEstimadaCosecha(datos.getFechaEstimadaCosecha());
            existente.setFrecuenciaRiegoDias(datos.getFrecuenciaRiegoDias());
            existente.setEstadoDesarrollo(datos.getEstadoDesarrollo());
            existente.setObservaciones(datos.getObservaciones());
            return ResponseEntity.ok(cultivoRepo.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }
}