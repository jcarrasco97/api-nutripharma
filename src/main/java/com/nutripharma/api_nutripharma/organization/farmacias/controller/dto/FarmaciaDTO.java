package com.nutripharma.api_nutripharma.organization.farmacias.controller.dto;

public class FarmaciaDTO {

    public record FarmaciaRequest(
            String email,
            String password,
            String nombre,
            String cif,
            String direccion
    ) {
    }

    public record FarmaciaResponse(
            Long id,
            String email,
            String nombre,
            String cif,
            String direccion,
            Double saldoVirtual // <-- NUEVO CAMPO
    ) {
    }

    // Petición para modificar una farmacia existente (sin tocar contraseñas ni saldos)
    public record FarmaciaUpdateRequest(
            String nombre,
            String cif,
            String direccion
    ) {
    }
}