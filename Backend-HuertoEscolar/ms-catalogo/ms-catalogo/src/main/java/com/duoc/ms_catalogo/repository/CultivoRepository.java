package com.duoc.ms_catalogo.repository;

import com.duoc.ms_catalogo.model.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CultivoRepository extends JpaRepository<Cultivo, Long> {
    List<Cultivo> findBySectorId(Long sectorId);
}