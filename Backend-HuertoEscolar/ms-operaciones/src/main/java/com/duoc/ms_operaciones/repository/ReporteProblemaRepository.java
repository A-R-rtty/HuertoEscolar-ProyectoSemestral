package com.duoc.ms_operaciones.repository;
import com.duoc.ms_operaciones.model.*; // o la entidad específica
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteProblemaRepository extends JpaRepository<ReporteProblema, Long> {}