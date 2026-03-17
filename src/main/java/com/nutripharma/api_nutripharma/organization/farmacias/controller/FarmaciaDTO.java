package com.nutripharma.api_nutripharma.organization.farmacias.controller;

public class FarmaciaDTO {

    public record FarmaciaRequest(
            String email,
            String password,
            String nombre,
            String cif,
            String direccion
    ) {}

    public record FarmaciaResponse(
            Long id,
            String email,
            String nombre,
            String cif,
            String direccion
    ) {}
}