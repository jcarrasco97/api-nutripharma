package com.nutripharma.api_nutripharma.clinical.consultas.controller.dto;

import java.time.LocalDateTime;

public record ConsultaResponse(
        Long idConsulta,
        Long pacienteId,
        LocalDateTime fechaConsulta,
        Double peso,
        Double porcentajeGrasa,
        String observaciones,
        Double imc,              // Número
        String estadoImc,        // Texto
        Boolean alertaGrasa      // Booleano
) {}