package com.nutripharma.api_nutripharma.operations.consultas.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nutripharma.api_nutripharma.operations.consultas.domain.EstadoConsulta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ConsultaDTO {

    public record ConsultaRequest(
            Long nutricionistaId,
            Long farmaciaId,
            LocalDate fecha,
            @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
            @JsonFormat(pattern = "HH:mm") LocalTime horaFin,
            Integer nuevas,
            Integer revisiones,
            Integer promociones,
            Integer personalFarmacia,
            String observacionesJornada
    ) {}

    public record ConsultaResponse(
            Long id,
            Long nutricionistaId,
            String nutricionistaNombre,
            Long farmaciaId,
            String farmaciaNombre,
            LocalDate fecha,
            @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
            @JsonFormat(pattern = "HH:mm") LocalTime horaFin,
            Integer nuevas,
            Integer revisiones,
            Integer promociones,
            Integer personalFarmacia,
            String observacionesJornada,
            EstadoConsulta estado,
            String mensajeIncidencia,
            String evidenciaUrl,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime evidenciaFecha,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fechaCreacion
    ) {}
}