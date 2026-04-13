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

    // --- CATÁLOGO DE MATERIALES ---
    @Transactional
    public MaterialResponse crearMaterial(MaterialRequest req) {
        Material m = materialRepository.save(Material.builder().nombre(req.nombre()).cantidadEstandar(req.cantidadEstandar()).build());
        return new MaterialResponse(m.getId(), m.getNombre(), m.getCantidadEstandar(), true);
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> listarMateriales(String email) {
        List<Material> todosLosMateriales = materialRepository.findAll();
        List<Long> materialesBloqueados = peticionRepository.findMaterialesBloqueadosParaNutricionista(email);

        return todosLosMateriales.stream()
                .map(m -> new MaterialResponse(
                        m.getId(),
                        m.getNombre(),
                        m.getCantidadEstandar(),
                        !materialesBloqueados.contains(m.getId())
                )).toList();
    }

    // --- PETICIONES (Nutricionistas) ---
    @Transactional
    public PeticionResponse crearPeticion(String email, PeticionRequest req) {
        Nutricionista n = nutricionistaRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrada."));

        List<Long> bloqueados = peticionRepository.findMaterialesBloqueadosParaNutricionista(email);
        boolean intentoIlegal = req.materialIds().stream().anyMatch(bloqueados::contains);

        if (intentoIlegal) {
            throw new IllegalStateException("No puedes solicitar materiales que ya están en estado SOLICITADO.");
        }

        List<Material> materiales = materialRepository.findAllById(req.materialIds());

        PeticionSuministro p = PeticionSuministro.builder()
                .nutricionista(n)
                .fechaPeticion(LocalDate.now())
                .estado(EstadoPeticion.SOLICITADO)
                .materialesSolicitados(materiales)
                .build();

        return mapPeticion(peticionRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<PeticionResponse> obtenerMisPeticiones(String email) {
        return peticionRepository.findByNutricionistaUsuarioEmailOrderByFechaPeticionDesc(email)
                .stream().map(this::mapPeticion).toList();
    }

    private PeticionResponse mapPeticion(PeticionSuministro p) {
        List<MaterialResponse> mats = p.getMaterialesSolicitados().stream()
                .map(m -> new MaterialResponse(m.getId(), m.getNombre(), m.getCantidadEstandar(), true)).toList();

        // 🛡️ ESCUDO ANTI-NULOS
        String nombreNutri = p.getNutricionista() != null
                ? p.getNutricionista().getNombre()
                : "[Nutricionista Borrado]";

        return new PeticionResponse(p.getId(), nombreNutri, p.getFechaPeticion(), p.getEstado(), mats);
    }
    // --- MÉTODOS EXCLUSIVOS PARA EL ADMIN ---

    @Transactional(readOnly = true)
    public List<PeticionResponse> listarTodasPeticionesAdmin() {
        return peticionRepository.findAll()
                .stream()
                .sorted((a, b) -> {
                    if (a.getEstado() == EstadoPeticion.SOLICITADO && b.getEstado() != EstadoPeticion.SOLICITADO)
                        return -1;
                    if (a.getEstado() != EstadoPeticion.SOLICITADO && b.getEstado() == EstadoPeticion.SOLICITADO)
                        return 1;
                    return b.getFechaPeticion().compareTo(a.getFechaPeticion());
                })
                .map(this::mapPeticion)
                .toList();
    }

    @Transactional
    public PeticionResponse actualizarEstadoPeticion(Long id, String nuevoEstado) {
        PeticionSuministro peticion = peticionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Petición no encontrada"));

        peticion.setEstado(EstadoPeticion.valueOf(nuevoEstado.toUpperCase()));
        return mapPeticion(peticionRepository.save(peticion));
    }
}