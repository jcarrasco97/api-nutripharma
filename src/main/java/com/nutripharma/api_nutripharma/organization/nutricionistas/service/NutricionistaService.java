package com.nutripharma.api_nutripharma.organization.nutricionistas.service;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import com.nutripharma.api_nutripharma.organization.farmacias.repository.FarmaciaRepository;
import com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto.NutricionistaDTO;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.AsignacionFarmacia;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository;
import com.nutripharma.api_nutripharma.security.domain.Rol;
import com.nutripharma.api_nutripharma.security.domain.Usuario;
import com.nutripharma.api_nutripharma.security.repository.RolRepository;
import com.nutripharma.api_nutripharma.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NutricionistaService {

    private final NutricionistaRepository nutricionistaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final FarmaciaRepository farmaciaRepository;

    @Transactional
    public NutricionistaDTO.NutricionistaResponse crearNutricionista(NutricionistaDTO.NutricionistaRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado en el sistema.");
        }
        if (nutricionistaRepository.existsByDni(request.dni())) {
            throw new IllegalArgumentException("Ya existe un nutricionista con ese DNI.");
        }

        Rol rolNutricionista = rolRepository.findByNombre("ROLE_NUTRICIONISTA")
                .orElseThrow(() -> new RuntimeException("Error crítico: El rol no existe en BD."));

        Usuario nuevoUsuario = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .activo(true)
                .roles(Set.of(rolNutricionista))
                .build();

        usuarioRepository.save(nuevoUsuario);

        Nutricionista nuevoNutricionista = Nutricionista.builder()
                .usuario(nuevoUsuario)
                .nombre(request.nombre())
                .apellidos(request.apellidos())
                .dni(request.dni())
                .horasContratoMensual(request.horasContratoMensual())
                .asignaciones(new ArrayList<>()) // Inicializamos la lista vacía
                .build();

        // --- NUEVO: Construimos las asignaciones con los kilómetros ---
        if (request.asignaciones() != null && !request.asignaciones().isEmpty()) {
            for (NutricionistaDTO.AsignacionRequest asigReq : request.asignaciones()) {
                Farmacia f = farmaciaRepository.findById(asigReq.farmaciaId())
                        .orElseThrow(() -> new IllegalArgumentException("Farmacia no encontrada con ID: " + asigReq.farmaciaId()));

                AsignacionFarmacia asignacion = AsignacionFarmacia.builder()
                        .nutricionista(nuevoNutricionista)
                        .farmacia(f)
                        .kilometros(asigReq.kilometros() != null ? asigReq.kilometros() : 0)
                        .build();

                nuevoNutricionista.getAsignaciones().add(asignacion);
            }
        }

        Nutricionista guardado = nutricionistaRepository.save(nuevoNutricionista);
        return mapToResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<NutricionistaDTO.NutricionistaResponse> obtenerTodos() {
        return nutricionistaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NutricionistaDTO.NutricionistaResponse obtenerPorId(Long id) {
        Nutricionista nutricionista = nutricionistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrado"));
        return mapToResponse(nutricionista);
    }

    @Transactional(readOnly = true)
    public NutricionistaDTO.NutricionistaResponse obtenerMiPerfil(String email) {
        Nutricionista nutricionista = nutricionistaRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de nutricionista no encontrado."));
        return mapToResponse(nutricionista);
    }

    @Transactional
    public NutricionistaDTO.NutricionistaResponse actualizarNutricionista(Long id, NutricionistaDTO.NutricionistaUpdateRequest request) {
        Nutricionista n = nutricionistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrada"));

        n.setNombre(request.nombre());
        n.setApellidos(request.apellidos());
        n.setHorasContratoMensual(request.horasContratoMensual());

        // --- NUEVO: Borramos las antiguas y guardamos las nuevas ---
        // Gracias a orphanRemoval=true, esto borrará de la BD las que se hayan quitado
        n.getAsignaciones().clear();

        if (request.asignaciones() != null && !request.asignaciones().isEmpty()) {
            for (NutricionistaDTO.AsignacionRequest asigReq : request.asignaciones()) {
                Farmacia f = farmaciaRepository.findById(asigReq.farmaciaId())
                        .orElseThrow(() -> new IllegalArgumentException("Farmacia no encontrada con ID: " + asigReq.farmaciaId()));

                AsignacionFarmacia asignacion = AsignacionFarmacia.builder()
                        .nutricionista(n)
                        .farmacia(f)
                        .kilometros(asigReq.kilometros() != null ? asigReq.kilometros() : 0)
                        .build();

                n.getAsignaciones().add(asignacion);
            }
        }

        return mapToResponse(nutricionistaRepository.save(n));
    }

    @Transactional
    public void eliminarNutricionista(Long id) {
        Nutricionista nutri = nutricionistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nutricionista no encontrada"));

        // Desactivamos su usuario
        Usuario usuario = nutri.getUsuario();
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        // Al borrar la Nutricionista, sus asignaciones se borran por el
        // cascade = CascadeType.ALL y orphanRemoval = true que tienes en la entidad.
        nutricionistaRepository.delete(nutri);
    }

    private NutricionistaDTO.NutricionistaResponse mapToResponse(Nutricionista n) {
        // --- NUEVO: Convertimos las entidades AsignacionFarmacia en AsignacionResponse DTOs ---
        List<NutricionistaDTO.AsignacionResponse> asignacionesResponse = n.getAsignaciones().stream()
                .map(a -> new NutricionistaDTO.AsignacionResponse(
                        a.getFarmacia().getId(),
                        a.getFarmacia().getNombre(),
                        a.getKilometros()
                ))
                .collect(Collectors.toList());

        return new NutricionistaDTO.NutricionistaResponse(
                n.getId(),
                n.getUsuario().getEmail(),
                n.getNombre(),
                n.getApellidos(),
                n.getDni(),
                n.getHorasContratoMensual(),
                asignacionesResponse // <-- Retornamos la lista compleja
        );
    }

    @Transactional(readOnly = true)
    public List<com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository.NutriInactivoProjection> obtenerBajas() {
        return nutricionistaRepository.findHistorialBajas();
    }

    @org.springframework.transaction.annotation.Transactional
    public void restaurarNutricionista(Long id) {
        nutricionistaRepository.reactivarUsuario(id);
        nutricionistaRepository.reactivarNutricionista(id);
    }
}