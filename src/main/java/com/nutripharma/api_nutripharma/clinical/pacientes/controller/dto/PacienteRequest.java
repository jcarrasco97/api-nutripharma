package com.nutripharma.api_nutripharma.clinical.pacientes.controller.dto;

import java.time.LocalDate;

public record PacienteRequest(
        String nombre,
        String dni,
        String email,
        String telefono,
        LocalDate fechaNacimiento,
        Boolean sexo,
        Double altura,
        String historial
) {}