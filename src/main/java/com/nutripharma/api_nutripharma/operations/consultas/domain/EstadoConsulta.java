package com.nutripharma.api_nutripharma.operations.consultas.domain;

public enum EstadoConsulta {
    BORRADOR,
    PENDIENTE_VALIDACION, // La Nutricionista terminó, espera a Paco
    VALIDADA,             // Paco aprueba -> Genera dinero a Farmacia y suma a objetivos
    LIQUIDADA,            // Cierre de caja: facturación cerrada para este turno
    CON_INCIDENCIA,       // Hay un error reportado
    CANCELADA             // Anulada permanentemente por Admin
}