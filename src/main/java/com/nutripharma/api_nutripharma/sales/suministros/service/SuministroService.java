package com.nutripharma.api_nutripharma.sales.suministros.service;

import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository;
import com.nutripharma.api_nutripharma.sales.suministros.controller.dto.SuministroDTO.*;
import com.nutripharma.api_nutripharma.sales.suministros.domain.EstadoPeticion;
import com.nutripharma.api_nutripharma.sales.suministros.domain.Material;
import com.nutripharma.api_nutripharma.sales.suministros.domain.PeticionSuministro;
import com.nutripharma.api_nutripharma.sales.suministros.repository.MaterialRepository;
import com.nutripharma.api_nutripharma.sales.suministros.repository.PeticionSuministroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuministroService {

    private final MaterialRepository materialRepository;
    private final PeticionSuministroRepository peticionRepository;
    private final NutricionistaRepository nutricionistaRepository;

    // --- CATÁLOGO DE MATERIALES (Admin) ---
    @Transactional
    public MaterialResponse crearMaterial(MaterialRequest req) {
        Material m = materialRepository.save(Material.builder().nombre(req.nombre()).cantidadEstandar(req.cantidadEstandar()).build());
        return new MaterialResponse(m.getId(), m.getNombre(), m.getCantidadEstandar());
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> listarMateriales() {
        return materialRepository.findAll().stream().map(m -> new MaterialResponse(m.getId(), m.getNombre(), m.getCantidadEstandar())).toList();
    }

    // --- PETICIONES (Nutricionistas) ---
    @Transactional
    public PeticionResponse crearPeticion(PeticionRequest req) {
        Nutricionista n = nutricionistaRepository.findById(req.nutricionistaId()).orElseThrow();
        List<Material> materiales = materialRepository.findAllById(req.materialIds());

        PeticionSuministro p = PeticionSuministro.builder()
                .nutricionista(n)
                .fechaPeticion(LocalDate.now())
                .estado(EstadoPeticion.PENDIENTE)
                .materialesSolicitados(materiales)
                .build();
        return mapPeticion(peticionRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<PeticionResponse> listarPeticiones() {
        return peticionRepository.findAll().stream().map(this::mapPeticion).toList();
    }

    private PeticionResponse mapPeticion(PeticionSuministro p) {
        List<MaterialResponse> mats = p.getMaterialesSolicitados().stream()
                .map(m -> new MaterialResponse(m.getId(), m.getNombre(), m.getCantidadEstandar())).toList();
        return new PeticionResponse(p.getId(), p.getNutricionista().getNombre(), p.getFechaPeticion(), p.getEstado(), mats);
    }
}