package com.nutripharma.api_nutripharma.operations.consultas.repository;

import com.nutripharma.api_nutripharma.operations.consultas.domain.Consulta;
import com.nutripharma.api_nutripharma.operations.consultas.domain.EstadoConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Spring Data nos crea automáticamente estas búsquedas para los futuros resúmenes
    List<Consulta> findByNutricionistaId(Long nutricionistaId);

    List<Consulta> findByFarmaciaId(Long farmaciaId);

    // Busca las consultas de un nutricionista, que estén confirmadas, en un rango de fechas
    List<Consulta> findByNutricionistaIdAndEstadoAndFechaBetween(
            Long nutricionistaId,
            EstadoConsulta estado,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    // --- NUEVO MÉTODO PARA FARMACIAS ---
    // Busca las consultas realizadas en una farmacia usando el email de su cuenta
    List<Consulta> findByFarmaciaUsuarioEmailOrderByFechaDesc(String email);

    List<Consulta> findByNutricionistaUsuarioEmail(String email);
    // Para el calendario y facturación global del Admin
    List<Consulta> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
}