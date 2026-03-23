package com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto;

import java.util.List;

public class NutricionistaDTO {

    // --- NUEVOS RECORDS PARA MANEJAR LA ASIGNACIÓN CON KILÓMETROS ---
    public record AsignacionRequest(
            Long farmaciaId,
            Integer kilometros
    ) {}

    public record AsignacionResponse(
            Long farmaciaId,
            String farmaciaNombre,
            Integer kilometros
    ) {}

    // --- PETICIONES PRINCIPALES ---
    public record NutricionistaRequest(
            String email,
            String password,
            String nombre,
            String apellidos,
            String dni,
            Integer horasContratoMensual,
            List<AsignacionRequest> asignaciones // <-- ACTUALIZADO
    ) {}

    public record NutricionistaResponse(
            Long id,
            String email,
            String nombre,
            String apellidos,
            String dni,
            Integer horasContratoMensual,
            List<AsignacionResponse> asignaciones // <-- ACTUALIZADO
    ) {}

    public record NutricionistaUpdateRequest(
            String nombre,
            String apellidos,
            Integer horasContratoMensual,
            List<AsignacionRequest> asignaciones // <-- ACTUALIZADO
    ) {}
}