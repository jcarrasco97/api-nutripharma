package com.nutripharma.api_nutripharma.operations.consultas.repository;

import com.nutripharma.api_nutripharma.operations.consultas.domain.Consulta;
import com.nutripharma.api_nutripharma.operations.consultas.domain.EstadoConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByNutricionistaId(Long nutricionistaId);
    List<Consulta> findByFarmaciaId(Long farmaciaId);
    List<Consulta> findByFarmaciaUsuarioEmailOrderByFechaDesc(String email);
    List<Consulta> findByNutricionistaUsuarioEmail(String email);
    List<Consulta> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // 👇 LA LÍNEA QUE FALTABA PARA EL DASHBOARD 👇
    List<Consulta> findByNutricionistaIdAndEstadoAndFechaBetween(
            Long nutricionistaId,
            EstadoConsulta estado,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    // 👇 QUERY SENIOR: Detecta solapamientos horarios excluyendo cancelados
    @Query("SELECT COUNT(c) > 0 FROM Consulta c " +
            "WHERE c.nutricionista.id = :nutriId " +
            "AND c.fecha = :fecha " +
            "AND c.estado <> com.nutripharma.api_nutripharma.operations.consultas.domain.EstadoConsulta.CANCELADA " +
            "AND (:inicio < c.horaFin AND :fin > c.horaInicio)")
    boolean existsOverlap(
            @Param("nutriId") Long nutriId,
            @Param("fecha") LocalDate fecha,
            @Param("inicio") LocalTime inicio,
            @Param("fin") LocalTime fin
    );
}