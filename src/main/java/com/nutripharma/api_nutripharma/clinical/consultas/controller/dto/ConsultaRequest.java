package com.nutripharma.api_nutripharma.clinical.consultas.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO para representar una consulta clínica.
 */
public record ConsultaRequest(
        /**
         * El ID del paciente. No puede ser nulo.
         */
        @NotNull(message = "El ID del paciente es obligatorio")
        Long pacienteId,

        /**
         * El peso del paciente. No puede ser nulo ni menor a 1 kg ni mayor a 500 kg.
         */
        @NotNull(message = "El peso es obligatorio")
        @Positive(message = "El peso debe ser mayor a 0")
        @Min(value = 1, message = "El peso debe ser al menos 1 kg")
        @Max(value = 500, message = "El peso no puede superar 500 kg")
        Double peso,

        /**
         * El porcentaje de grasa corporal del paciente. No puede ser menor a 1% ni mayor a 100%.
         */
        @Positive(message = "El porcentaje de grasa debe ser mayor a 0")
        @Min(value = 1, message = "El porcentaje de grasa debe ser al menos 1%")
        @Max(value = 100, message = "El porcentaje de grasa no puede superar 100%")
        Double porcentajeGrasa,

        /**
         * Observaciones adicionales sobre la consulta.
         */
        String observaciones
) {}