package com.nutripharma.api_nutripharma.organization.farmacias.service;

import com.nutripharma.api_nutripharma.organization.farmacias.controller.dto.FarmaciaDTO;
import com.nutripharma.api_nutripharma.organization.farmacias.controller.dto.FarmaciaDTO.FarmaciaRequest;
import com.nutripharma.api_nutripharma.organization.farmacias.controller.dto.FarmaciaDTO.FarmaciaResponse;
import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import com.nutripharma.api_nutripharma.organization.farmacias.repository.FarmaciaRepository;
import com.nutripharma.api_nutripharma.organization.nutricionistas.repository.AsignacionFarmaciaRepository;
import com.nutripharma.api_nutripharma.security.domain.Rol;
import com.nutripharma.api_nutripharma.security.domain.Usuario;
import com.nutripharma.api_nutripharma.security.repository.RolRepository;
import com.nutripharma.api_nutripharma.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmaciaService {

    private final FarmaciaRepository farmaciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public FarmaciaResponse crearFarmacia(FarmaciaRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }
        if (farmaciaRepository.existsByCif(request.cif())) {
            throw new IllegalArgumentException("Ya existe una farmacia con ese CIF.");
        }

        Rol rolFarmacia = rolRepository.findByNombre("ROLE_FARMACIA")
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));

        Usuario nuevoUsuario = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .activo(true)
                .roles(Set.of(rolFarmacia))
                .build();
        usuarioRepository.save(nuevoUsuario);

        Farmacia nuevaFarmacia = Farmacia.builder()
                .usuario(nuevoUsuario)
                .nombre(request.nombre())
                .cif(request.cif())
                .direccion(request.direccion())
                .esProvinciaLocal(request.esProvinciaLocal() != null ? request.esProvinciaLocal() : true)
                .porcentajeComision(request.porcentajeComision() != null ? request.porcentajeComision() : 30.0) // <-- NUEVO
                .build();
        Farmacia guardada = farmaciaRepository.save(nuevaFarmacia);

        return mapToResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<FarmaciaResponse> obtenerTodas() {
        return farmaciaRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FarmaciaResponse obtenerMiPerfil(String email) {
        Farmacia farmacia = farmaciaRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de farmacia no encontrado."));
        return mapToResponse(farmacia);
    }

    @Transactional
    public FarmaciaResponse actualizarFarmacia(Long id, FarmaciaDTO.FarmaciaUpdateRequest request) {
        Farmacia f = farmaciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Farmacia no encontrada"));

        f.setNombre(request.nombre());
        f.setCif(request.cif());
        f.setDireccion(request.direccion());
        f.setEsProvinciaLocal(request.esProvinciaLocal());

        // --- NUEVO ---
        if (request.porcentajeComision() != null) {
            f.setPorcentajeComision(request.porcentajeComision());
        }

        return mapToResponse(farmaciaRepository.save(f));
    }

    // Inyecta el nuevo repositorio en el constructor
    private final AsignacionFarmaciaRepository asignacionRepository;

    @Transactional
    public void eliminarFarmacia(Long id) {
        Farmacia farmacia = farmaciaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Farmacia no encontrada"));

        // 1. Desactivamos el Usuario (Alma)
        Usuario usuario = farmacia.getUsuario();
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        // 2. Limpiamos las asignaciones (Vínculos)
        // Esto evita que las Nutricionistas intenten cargar una farmacia inactiva
        asignacionRepository.deleteByFarmaciaId(id);

        // 3. Desactivamos la Farmacia (Cuerpo)
        // El @SQLDelete se encargará de hacer el UPDATE activo = false automáticamente
        farmaciaRepository.delete(farmacia);
    }

    private FarmaciaResponse mapToResponse(Farmacia f) {
        return new FarmaciaResponse(
                f.getId(),
                f.getUsuario().getEmail(),
                f.getNombre(),
                f.getCif(),
                f.getDireccion(),
                f.getSaldoVirtual(),
                f.getEsProvinciaLocal(),
                f.getPorcentajeComision() // <-- NUEVO
        );
    }

    @Transactional(readOnly = true)
    public List<com.nutripharma.api_nutripharma.organization.farmacias.repository.FarmaciaRepository.FarmaciaInactivaProjection> obtenerBajas() {
        return farmaciaRepository.findHistorialBajas();
    }

    @org.springframework.transaction.annotation.Transactional
    public void restaurarFarmacia(Long id) {
        farmaciaRepository.reactivarUsuario(id);
        farmaciaRepository.reactivarFarmacia(id);
    }
}