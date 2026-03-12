package com.nutripharma.api_nutripharma.clinical.pacientes.controller.dto;

import java.time.LocalDate;

public record PacienteResponse(
        Long idPaciente,
        String dni,
        String nombre,
        String email,
        String telefono,
        LocalDate fechaNacimiento,
        Boolean sexo,
        Double altura,
        String historial
) {}