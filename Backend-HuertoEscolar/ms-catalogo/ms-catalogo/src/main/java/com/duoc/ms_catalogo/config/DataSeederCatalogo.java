package com.duoc.ms_catalogo.config;

import com.duoc.ms_catalogo.model.*;
import com.duoc.ms_catalogo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataSeederCatalogo {

    @Bean
    CommandLineRunner initCatalogo(UsuarioRepository usuarioRepo, SectorRepository sectorRepo, CultivoRepository cultivoRepo) {
        return args -> {
            if (usuarioRepo.count() == 0) {
                usuarioRepo.saveAll(List.of(
                        new Usuario("ADM-01", "Prof. Elena Rojas (Ficticia)", "admin.huerto@demo.cl", "ADMINISTRADOR", "Coordinación Huerto Escolar"),
                        new Usuario("EST-01", "Mateo Vega (Ficticio)", "mateo.est@demo.cl", "COLABORADOR", "Taller 5° Básico A"),
                        new Usuario("EST-02", "Sofía Mora (Ficticia)", "sofia.est@demo.cl", "COLABORADOR", "Taller 6° Básico B"),
                        new Usuario("EST-03", "Lucas Soto (Ficticio)", "lucas.est@demo.cl", "COLABORADOR", "Taller 7° Básico A")
                ));
            }
            if (sectorRepo.count() == 0) {
                String hoy = LocalDate.now().toString();
                Sector s1 = new Sector();
                s1.setCodigo("SEC-NORTE");
                s1.setNombre("Bancal Norte - Hortalizas");
                s1.setDescripcion("Bancal elevado con exposición solar directa de mañana.");
                s1.setUbicacion("Patio Interior Norte");
                s1.setEstado("EN_USO");
                s1.setResponsableId(1L);
                s1.setFechaCreacion(hoy);
                s1.setFechaActualizacion(hoy);
                s1 = sectorRepo.save(s1);

                Sector s2 = new Sector();
                s2.setCodigo("SEC-INV");
                s2.setNombre("Invernadero Almácigos");
                s2.setDescripcion("Espacio protegido para germinación y plantas aromáticas.");
                s2.setUbicacion("Costado Sala de Ciencias");
                s2.setEstado("EN_USO");
                s2.setResponsableId(1L);
                s2.setFechaCreacion(hoy);
                s2.setFechaActualizacion(hoy);
                s2 = sectorRepo.save(s2);

                Cultivo c1 = new Cultivo();
                c1.setNombreComun("Lechuga Costina");
                c1.setDescripcion("Hortaliza de hoja verde sembrada por estudiantes de 5° Básico.");
                c1.setSectorId(s1.getId());
                c1.setFechaSiembra(LocalDate.now().minusDays(25).toString());
                c1.setFechaEstimadaCosecha(LocalDate.now().plusDays(15).toString());
                c1.setFrecuenciaRiegoDias(2);
                c1.setEstadoDesarrollo("EN_CRECIMIENTO");
                c1.setObservaciones("Buen vigor foliar.");
                cultivoRepo.save(c1);
            }
        };
    }
}