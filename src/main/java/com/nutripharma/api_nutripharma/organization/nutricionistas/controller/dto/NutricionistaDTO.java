package com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto;

public class NutricionistaDTO {

    // Lo que esperamos recibir desde el Frontend (React)
    public record NutricionistaRequest(
            String email,
            String password,
            String nombre,
            String apellidos,
            String dni,
            Integer horasContratoMensual
    ) {
    }

    // Lo que le devolveremos al Frontend después de crearlo con éxito
    public record NutricionistaResponse(
            Long id,
            String email,
            String nombre,
            String apellidos,
            String dni,
            Integer horasContratoMensual
    ) {
    }

    // Petición para modificar a un empleado
    public record NutricionistaUpdateRequest(
            String nombre,
            String apellidos,
            Integer horasContratoMensual
    ) {
    }
}