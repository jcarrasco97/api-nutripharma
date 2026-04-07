package com.nutripharma.api_nutripharma.operations.consultas.controller.dto;

import com.nutripharma.api_nutripharma.operations.consultas.domain.EstadoConsulta;
import com.nutripharma.api_nutripharma.operations.consultas.domain.TipoTurno;

import java.time.LocalDate;
import java.time.LocalTime;

public class ConsultaDTO {

    public record ConsultaRequest(
            Long nutricionistaId,
            Long farmaciaId,
            LocalDate fecha,
            TipoTurno tipoTurno,
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
            TipoTurno tipoTurno,
            LocalTime horaInicio,
            LocalTime horaFin,
            Integer nuevas,
            Integer revisiones,
            Integer promociones,
            Integer personalFarmacia,
            String observacionesJornada,
            EstadoConsulta estado,
            String mensajeIncidencia,
            String evidenciaUrl
    ) {}
}