package com.nutripharma.api_nutripharma.clinical.consultas.service;

import com.nutripharma.api_nutripharma.clinical.consultas.controller.dto.ConsultaRequest;
import com.nutripharma.api_nutripharma.clinical.consultas.controller.dto.ConsultaResponse;
import com.nutripharma.api_nutripharma.clinical.consultas.domain.Consulta;
import com.nutripharma.api_nutripharma.clinical.consultas.repository.ConsultaRepository;
import com.nutripharma.api_nutripharma.clinical.pacientes.domain.Paciente;
import com.nutripharma.api_nutripharma.clinical.pacientes.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;

    @Transactional
    public ConsultaResponse registrarConsulta(ConsultaRequest request) {
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Consulta nuevaConsulta = Consulta.builder()
                .paciente(paciente)
                .fechaConsulta(LocalDateTime.now())
                .peso(request.peso())
                .porcentajeGrasa(request.porcentajeGrasa())
                .observaciones(request.observaciones())
                .build();

        Consulta guardada = consultaRepository.save(nuevaConsulta);
        return mapToResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> obtenerConsultasPorPaciente(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new RuntimeException("Paciente no encontrado");
        }
        return consultaRepository.findByPacienteIdPacienteOrderByFechaConsultaDesc(pacienteId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Este método es el que soluciona el error de tipos.
     * Asegúrate de que el orden de los campos coincida con tu record ConsultaResponse.
     */
    private ConsultaResponse mapToResponse(Consulta consulta) {
        Paciente p = consulta.getPaciente();

        Double imc = 0.0;
        // Si la altura es Double, el check de null es este:
        if (p.getAltura() != null && p.getAltura() > 0) {
            double alturaMetros = p.getAltura() / 100;
            imc = consulta.getPeso() / (alturaMetros * alturaMetros);
            imc = Math.round(imc * 100.0) / 100.0;
        }

        // 2. Lógica de Alerta de Grasa (Subida > 2%)
        boolean alerta = false;
        List<Consulta> historial = consultaRepository.findByPacienteIdPacienteOrderByFechaConsultaDesc(p.getIdPaciente());

        if (historial.size() > 1) {
            // historial.get(0) es la actual (ya guardada), historial.get(1) es la anterior
            double grasaAnterior = historial.get(1).getPorcentajeGrasa();
            if ((consulta.getPorcentajeGrasa() - grasaAnterior) > 2.0) {
                alerta = true;
            }
        }

        // 3. Retorno del Record (Cuidando el orden de los tipos)
        return new ConsultaResponse(
                consulta.getId(),
                p.getIdPaciente(),
                consulta.getFechaConsulta(),
                consulta.getPeso(),
                consulta.getPorcentajeGrasa(),
                consulta.getObservaciones(),
                imc,
                determinarEstadoImc(imc),
                alerta
        );
    }

    private String determinarEstadoImc(Double imc) {
        if (imc < 18.5) return "Bajo peso";
        if (imc < 24.9) return "Normal";
        if (imc < 29.9) return "Sobrepeso";
        return "Obesidad";
    }
}