package com.nutripharma.api_nutripharma.operations.consultas.service;

import com.nutripharma.api_nutripharma.operations.consultas.controller.dto.ConsultaDTO.ConsultaRequest;
import com.nutripharma.api_nutripharma.operations.consultas.controller.dto.ConsultaDTO.ConsultaResponse;
import com.nutripharma.api_nutripharma.operations.consultas.domain.Consulta;
import com.nutripharma.api_nutripharma.operations.consultas.domain.EstadoConsulta;
import com.nutripharma.api_nutripharma.operations.consultas.repository.ConsultaRepository;
import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import com.nutripharma.api_nutripharma.organization.farmacias.repository.FarmaciaRepository;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final FarmaciaRepository farmaciaRepository;

    // 1. CREAR EL TURNO (Por defecto nace como BORRADOR)
    @Transactional
    public ConsultaResponse registrarTurno(ConsultaRequest request) {
        Nutricionista nutricionista = nutricionistaRepository.findById(request.nutricionistaId())
                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrado"));

        Farmacia farmacia = farmaciaRepository.findById(request.farmaciaId())
                .orElseThrow(() -> new IllegalArgumentException("Farmacia no encontrada"));

        Consulta nuevaConsulta = Consulta.builder()
                .nutricionista(nutricionista)
                .farmacia(farmacia)
                .fecha(request.fecha())
                .tipoTurno(request.tipoTurno())
                .horaInicio(request.horaInicio())
                .horaFin(request.horaFin())
                .nuevas(request.nuevas() != null ? request.nuevas() : 0)
                .revisiones(request.revisiones() != null ? request.revisiones() : 0)
                .promociones(request.promociones() != null ? request.promociones() : 0)
                .personalFarmacia(request.personalFarmacia() != null ? request.personalFarmacia() : 0)
                .observacionesJornada(request.observacionesJornada())
                .estado(EstadoConsulta.BORRADOR) // Nace como borrador
                .build();

        return mapToResponse(consultaRepository.save(nuevaConsulta));
    }

    // 2. CONFIRMAR EL TURNO Y CALCULAR COMISIÓN (REGLA 70/30)
    @Transactional
    public ConsultaResponse confirmarTurno(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

        // Protección: Evitar doble ingreso si ya estaba confirmada
        if (consulta.getEstado() == EstadoConsulta.CONFIRMADA || consulta.getEstado() == EstadoConsulta.CON_INCIDENCIA) {
            throw new IllegalStateException("Esta consulta ya ha sido confirmada y liquidada anteriormente.");
        }

        // --- CÁLCULO DEL SALDO VIRTUAL PARA LA FARMACIA ---
        double ingresosNuevas = consulta.getNuevas() * 25.0;
        double ingresosRevisiones = consulta.getRevisiones() * 20.0;
        // Promociones y Personal Farmacia valen 0€, así que no suman.

        double totalGenerado = ingresosNuevas + ingresosRevisiones;

        if (totalGenerado > 0) {
            double comisionFarmacia = totalGenerado * 0.30; // El famoso 30%
            Farmacia farmacia = consulta.getFarmacia();

            // Le sumamos el dinero a su monedero
            double saldoActual = farmacia.getSaldoVirtual() != null ? farmacia.getSaldoVirtual() : 0.0;
            farmacia.setSaldoVirtual(saldoActual + comisionFarmacia);

            farmaciaRepository.save(farmacia);
        }
        // --------------------------------------------------

        consulta.setEstado(EstadoConsulta.CONFIRMADA);
        return mapToResponse(consultaRepository.save(consulta));
    }

    // 3. ABRIR INCIDENCIA (Anti-WhatsApp)
    @Transactional
    public ConsultaResponse reportarIncidencia(Long id, String mensaje) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

        if (consulta.getEstado() != EstadoConsulta.CONFIRMADA) {
            throw new IllegalStateException("Solo se pueden abrir incidencias sobre turnos confirmados.");
        }

        consulta.setEstado(EstadoConsulta.CON_INCIDENCIA);
        consulta.setMensajeIncidencia(mensaje);
        return mapToResponse(consultaRepository.save(consulta));
    }

    // 4. LISTAR TODAS
    @Transactional(readOnly = true)
    public List<ConsultaResponse> obtenerTodas() {
        return consultaRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private ConsultaResponse mapToResponse(Consulta c) {
        return new ConsultaResponse(
                c.getId(),
                c.getNutricionista().getNombre() + " " + c.getNutricionista().getApellidos(),
                c.getFarmacia().getNombre(),
                c.getFecha(),
                c.getTipoTurno(),
                c.getHoraInicio(),
                c.getHoraFin(),
                c.getNuevas(),
                c.getRevisiones(),
                c.getPromociones(),
                c.getPersonalFarmacia(),
                c.getObservacionesJornada(),
                c.getEstado(),
                c.getMensajeIncidencia()
        );
    }
}