package com.nutripharma.api_nutripharma.clinical.pacientes.service;

import com.nutripharma.api_nutripharma.clinical.pacientes.controller.dto.PacienteRequest;
import com.nutripharma.api_nutripharma.clinical.pacientes.controller.dto.PacienteResponse;
import com.nutripharma.api_nutripharma.clinical.pacientes.domain.Paciente;
import com.nutripharma.api_nutripharma.clinical.pacientes.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public List<PacienteResponse> getAllPacientes() {
        return pacienteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PacienteResponse getPacienteById(Long id) {
        Paciente p = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        return mapToResponse(p);
    }

    @Transactional
    public PacienteResponse createPaciente(PacienteRequest request) {
        Paciente nuevo = Paciente.builder()
                .dni(request.dni())
                .nombre(request.nombre())
                .email(request.email())
                .telefono(request.telefono())
                .fechaNacimiento(request.fechaNacimiento())
                .sexo(request.sexo())
                .altura(request.altura())
                .historial(request.historial())
                .build();

        return mapToResponse(pacienteRepository.save(nuevo));
    }

    private PacienteResponse mapToResponse(Paciente p) {
        return new PacienteResponse(
                p.getIdPaciente(),p.getDni(), p.getNombre(), p.getEmail(), p.getTelefono(),
                p.getFechaNacimiento(), p.getSexo(), p.getAltura(), p.getHistorial()
        );
    }
    public PacienteResponse findByCriteria(String dni, String email) {
        Paciente p = pacienteRepository.findByDniOrEmail(dni, email)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado por DNI o Email"));
        return mapToResponse(p);
    }

    // ... (dentro de la clase)
    public List<PacienteResponse> buscarPorDniPredictivo(String dni) {
        return pacienteRepository.findByDniStartingWith(dni)
                .stream()
                .map(this::mapToResponse) // Usando el método mapToResponse que ya tenías
                .toList();
    }
}