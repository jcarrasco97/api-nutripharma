package com.nutripharma.api_nutripharma.operations.consultas.repository;

import com.nutripharma.api_nutripharma.operations.consultas.domain.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Spring Data nos crea automáticamente estas búsquedas para los futuros resúmenes
    List<Consulta> findByNutricionistaId(Long nutricionistaId);

    List<Consulta> findByFarmaciaId(Long farmaciaId);
}