package com.nutripharma.api_nutripharma.operations.consultas.controller.dto;

import com.nutripharma.api_nutripharma.operations.consultas.domain.EstadoConsulta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ConsultaDTO {

    public record ConsultaRequest(
            Long nutricionistaId,
            Long farmaciaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            Integer nuevas,
            Integer revisiones,
            Integer promociones,
            Integer personalFarmacia,
            String observacionesJornada
    ) {}

    public record ConsultaResponse(
            Long id,
            String nutricionistaNombre,
            String farmaciaNombre,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            Integer nuevas,
            Integer revisiones,
            Integer promociones,
            Integer personalFarmacia,
            String observacionesJornada,
            EstadoConsulta estado,
            String mensajeIncidencia,
            String evidenciaUrl,
            LocalDateTime evidenciaFecha,
            LocalDateTime fechaCreacion
    ) {}
}