package com.duoc.ms_operaciones.config;

import com.duoc.ms_operaciones.model.Tarea;
import com.duoc.ms_operaciones.repository.TareaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;

@Configuration
public class DataSeederOperaciones {

    @Bean
    CommandLineRunner initOperaciones(TareaRepository tareaRepo) {
        return args -> {
            if (tareaRepo.count() == 0) {
                Tarea t1 = new Tarea();
                t1.setTipoTarea("RIEGO");
                t1.setDescripcion("Regar suavemente el bancal de lechugas (sincronizado desde Spring Boot).");
                t1.setSectorId(1L);
                t1.setCultivoId(1L);
                t1.setFechaCreacion(LocalDate.now().toString());
                t1.setFechaProgramada(LocalDate.now().toString());
                t1.setResponsableId(2L);
                t1.setCreadorId(1L);
                t1.setEstado("PENDIENTE");
                tareaRepo.save(t1);
            }
        };
    }
}