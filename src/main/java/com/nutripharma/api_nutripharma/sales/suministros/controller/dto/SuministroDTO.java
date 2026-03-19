package com.nutripharma.api_nutripharma.sales.suministros.controller.dto;

import com.nutripharma.api_nutripharma.sales.suministros.domain.EstadoPeticion;
import java.time.LocalDate;
import java.util.List;

public class SuministroDTO {
    // Para el catálogo de materiales (Añadido el boolean 'disponible' para la regla anti-spam)
    public record MaterialRequest(String nombre, Integer cantidadEstandar) {}
    public record MaterialResponse(Long id, String nombre, Integer cantidadEstandar, boolean disponible) {}

    // Para la petición (El Nutricionista ya NO envía su ID. El backend lo saca del Token por seguridad)
    public record PeticionRequest(List<Long> materialIds) {}

    public record PeticionResponse(Long id, String nutricionistaNombre, LocalDate fechaPeticion, EstadoPeticion estado, List<MaterialResponse> materiales) {}
}