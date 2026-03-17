package com.nutripharma.api_nutripharma.sales.suministros.controller.dto;
import com.nutripharma.api_nutripharma.sales.suministros.domain.EstadoPeticion;
import java.time.LocalDate;
import java.util.List;

public class SuministroDTO {
    // Para el catálogo de materiales
    public record MaterialRequest(String nombre, Integer cantidadEstandar) {}
    public record MaterialResponse(Long id, String nombre, Integer cantidadEstandar) {}

    // Para la petición (El Nutricionista solo envía su ID y los IDs de lo que marcó en el checklist)
    public record PeticionRequest(Long nutricionistaId, List<Long> materialIds) {}
    public record PeticionResponse(Long id, String nutricionistaNombre, LocalDate fechaPeticion, EstadoPeticion estado, List<MaterialResponse> materiales) {}
}