package com.nutripharma.api_nutripharma.organization.nutricionistas.service;

import com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto.NutricionistaDTO;
import com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto.NutricionistaDTO.NutricionistaRequest;
import com.nutripharma.api_nutripharma.organization.nutricionistas.controller.dto.NutricionistaDTO.NutricionistaResponse;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository;
import com.nutripharma.api_nutripharma.security.domain.Rol;
import com.nutripharma.api_nutripharma.security.domain.Usuario;
import com.nutripharma.api_nutripharma.security.repository.RolRepository;
import com.nutripharma.api_nutripharma.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // @Transactional es vital: Si falla al crear el perfil, deshace también la creación del usuario.
    @Transactional
    public NutricionistaResponse crearNutricionista(NutricionistaRequest request) {

        // 1. Validaciones de negocio (Evitar duplicados)
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado en el sistema.");
        }
        if (nutricionistaRepository.existsByDni(request.dni())) {
            throw new IllegalArgumentException("Ya existe un nutricionista con ese DNI.");
        }

        // 2. Buscar el Rol en la base de datos
        Rol rolNutricionista = rolRepository.findByNombre("ROLE_NUTRICIONISTA")
                .orElseThrow(() -> new RuntimeException("Error crítico: El rol no existe en BD."));

        // 3. Crear las credenciales de acceso (Bóveda de Seguridad)
        Usuario nuevoUsuario = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // ¡Encriptación fuerte!
                .activo(true)
                .roles(Set.of(rolNutricionista))
                .build();

        // Guardamos el usuario para que MySQL le asigne un ID
        usuarioRepository.save(nuevoUsuario);

        // 4. Crear el perfil laboral (Directorio de la Organización)
        Nutricionista nuevoNutricionista = Nutricionista.builder()
                .usuario(nuevoUsuario) // Aquí hacemos el enlace 1 a 1
                .nombre(request.nombre())
                .apellidos(request.apellidos())
                .dni(request.dni())
                .horasContratoMensual(request.horasContratoMensual())
                .build();

        Nutricionista guardado = nutricionistaRepository.save(nuevoNutricionista);

        // 5. Devolver el DTO limpio al frontend
        return new NutricionistaResponse(
                guardado.getId(),
                nuevoUsuario.getEmail(),
                guardado.getNombre(),
                guardado.getApellidos(),
                guardado.getDni(),
                guardado.getHorasContratoMensual()
        );
    }

    @Transactional(readOnly = true)
    public List<NutricionistaResponse> obtenerTodos() {
        return nutricionistaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NutricionistaResponse obtenerPorId(Long id) {
        Nutricionista nutricionista = nutricionistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrado"));
        return mapToResponse(nutricionista);
    }

    @Transactional(readOnly = true)
    public NutricionistaResponse obtenerMiPerfil(String email) {
        Nutricionista nutricionista = nutricionistaRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de nutricionista no encontrado."));
        return mapToResponse(nutricionista);
    }

    @Transactional
    public NutricionistaResponse actualizarNutricionista(Long id, NutricionistaDTO.NutricionistaUpdateRequest request) {
        Nutricionista n = nutricionistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrada"));

        n.setNombre(request.nombre());
        n.setApellidos(request.apellidos());
        n.setHorasContratoMensual(request.horasContratoMensual());

        return mapToResponse(nutricionistaRepository.save(n));
    }

    @Transactional
    public void eliminarNutricionista(Long id) {
        Nutricionista n = nutricionistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrada"));

        Long usuarioId = n.getUsuario().getId();

        // Borramos el perfil laboral y luego el acceso al sistema
        nutricionistaRepository.delete(n);
        usuarioRepository.deleteById(usuarioId);
    }

    // Método auxiliar para no repetir código de mapeo
    private NutricionistaResponse mapToResponse(Nutricionista n) {
        return new NutricionistaResponse(
                n.getId(),
                n.getUsuario().getEmail(), // Sacamos el email de la tabla usuarios
                n.getNombre(),
                n.getApellidos(),
                n.getDni(),
                n.getHorasContratoMensual()
        );
    }


}