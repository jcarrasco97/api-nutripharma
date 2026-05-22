package com.nutripharma.api_nutripharma.organization.farmacias.controller.dto;

public class FarmaciaDTO {

    public record FarmaciaRequest(
            String email,
            String password,
            String nombre,
            String cif,
            String telefono,
            String direccion,
            Boolean esProvinciaLocal,
            Double porcentajeComision
    ) {
    }

    public record FarmaciaResponse(
            Long id,
            String email,
            String nombre,
            String cif,
            String telefono,
            String direccion,
            Double saldoVirtual,
            Boolean esProvinciaLocal,
            Double porcentajeComision
    ) {
    }

    public record FarmaciaUpdateRequest(
            String email,
            String password,
            String nombre,
            String cif,
            String telefono,
            String direccion,
            Boolean esProvinciaLocal,
            Double porcentajeComision
    ) {
    }
}