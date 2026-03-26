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

    @Transactional
    public ConsultaResponse registrarTurno(ConsultaRequest request) {

        // 1. Validaciones de Coherencia Temporal Básica (El inicio no puede ser después del fin)
        if (request.horaFin().isBefore(request.horaInicio()) || request.horaFin().equals(request.horaInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        // 2. Validación Anti-Solapamiento (El Escudo)
        boolean solapado = consultaRepository.existsOverlap(
                request.nutricionistaId(),
                request.fecha(),
                request.horaInicio(),
                request.horaFin()
        );

        if (solapado) {
            throw new IllegalStateException("Conflicto de agenda: Ya tienes un turno registrado que se solapa con este horario.");
        }

        // 3. Si pasa el escudo, procedemos con la creación normal
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
                .estado(EstadoConsulta.BORRADOR)
                .build();

        return mapToResponse(consultaRepository.save(nuevaConsulta));
    }

    @Transactional
    public ConsultaResponse confirmarTurno(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

        if (consulta.getEstado() != EstadoConsulta.BORRADOR && consulta.getEstado() != EstadoConsulta.CON_INCIDENCIA) {
            throw new IllegalStateException("Solo se pueden confirmar consultas en Borrador o con Incidencia resuelta.");
        }

        consulta.setEstado(EstadoConsulta.PENDIENTE_VALIDACION);
        return mapToResponse(consultaRepository.save(consulta));
    }

    // =========================================================================================
    // ⚙️ MOTOR CONTABLE Y VALIDACIONES (ADMIN)
    // =========================================================================================

    @Transactional
    public ConsultaResponse validarTurno(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

        if (consulta.getEstado() == EstadoConsulta.VALIDADA) {
            throw new IllegalStateException("Esta consulta ya ha sido validada y liquidada anteriormente.");
        }
        if (consulta.getEstado() == EstadoConsulta.CANCELADA) {
            throw new IllegalStateException("No se puede validar una consulta cancelada.");
        }

        // Limpiamos el mensaje de incidencia si lo hubiera, ya que se da por resuelta
        consulta.setMensajeIncidencia(null);

        aplicarSaldoFarmacia(consulta);

        consulta.setEstado(EstadoConsulta.VALIDADA);
        return mapToResponse(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaResponse editarYValidarTurnoAdmin(Long id, Integer nuevas, Integer revisiones, Integer promociones, Integer personalFarmacia) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

        if (consulta.getEstado() == EstadoConsulta.CANCELADA) {
            throw new IllegalStateException("No se puede editar una consulta cancelada.");
        }

        // 1. REVERSIÓN: Si ya estaba validada, restamos el saldo antiguo antes de poner los datos nuevos
        if (consulta.getEstado() == EstadoConsulta.VALIDADA) {
            revertirSaldoFarmacia(consulta);
        }

        // 2. EDICIÓN: Actualizamos los valores numéricos
        consulta.setNuevas(nuevas != null ? nuevas : 0);
        consulta.setRevisiones(revisiones != null ? revisiones : 0);
        consulta.setPromociones(promociones != null ? promociones : 0);
        // Si no te envían el personal de farmacia por DTO, mantenemos el que había o ponemos 0
        consulta.setPersonalFarmacia(personalFarmacia != null ? personalFarmacia : consulta.getPersonalFarmacia());

        // 3. APLICACIÓN: Limpiamos incidencia y sumamos el nuevo saldo calculado
        consulta.setMensajeIncidencia(null);
        aplicarSaldoFarmacia(consulta);

        consulta.setEstado(EstadoConsulta.VALIDADA);
        return mapToResponse(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaResponse cancelarTurnoAdmin(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

        if (consulta.getEstado() == EstadoConsulta.CANCELADA) {
            throw new IllegalStateException("La consulta ya está cancelada.");
        }

        // Si la consulta ya había generado dinero, tenemos que restárselo a la farmacia
        if (consulta.getEstado() == EstadoConsulta.VALIDADA) {
            revertirSaldoFarmacia(consulta);
        }

        consulta.setEstado(EstadoConsulta.CANCELADA);
        return mapToResponse(consultaRepository.save(consulta));
    }

    // =========================================================================================
    // 🛡️ MÉTODOS PRIVADOS CONTABLES
    // =========================================================================================

    private void aplicarSaldoFarmacia(Consulta consulta) {
        double totalGenerado = (consulta.getNuevas() * 25.0) + (consulta.getRevisiones() * 20.0);
        if (totalGenerado > 0) {
            Farmacia farmacia = consulta.getFarmacia();
            double porcentajeDecimal = (farmacia.getPorcentajeComision() != null ? farmacia.getPorcentajeComision() : 30.0) / 100.0;
            double comisionFarmacia = totalGenerado * porcentajeDecimal;
            double saldoActual = farmacia.getSaldoVirtual() != null ? farmacia.getSaldoVirtual() : 0.0;
            farmacia.setSaldoVirtual(saldoActual + comisionFarmacia);
            farmaciaRepository.save(farmacia);
        }
    }

    private void revertirSaldoFarmacia(Consulta consulta) {
        double totalGeneradoAnterior = (consulta.getNuevas() * 25.0) + (consulta.getRevisiones() * 20.0);
        if (totalGeneradoAnterior > 0) {
            Farmacia farmacia = consulta.getFarmacia();
            double porcentajeDecimal = (farmacia.getPorcentajeComision() != null ? farmacia.getPorcentajeComision() : 30.0) / 100.0;
            double comisionRevertir = totalGeneradoAnterior * porcentajeDecimal;
            double saldoActual = farmacia.getSaldoVirtual() != null ? farmacia.getSaldoVirtual() : 0.0;
            farmacia.setSaldoVirtual(saldoActual - comisionRevertir);
            farmaciaRepository.save(farmacia);
        }
    }

    // =========================================================================================
    // 📩 TICKETING (INCIDENCIAS)
    // =========================================================================================

    @Transactional
    public ConsultaResponse reportarIncidencia(Long id, String mensaje) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));
        if (consulta.getEstado() == EstadoConsulta.BORRADOR || consulta.getEstado() == EstadoConsulta.CANCELADA) {
            throw new IllegalStateException("No se pueden abrir incidencias en este estado.");
        }
        consulta.setEstado(EstadoConsulta.CON_INCIDENCIA);
        consulta.setMensajeIncidencia(mensaje);
        return mapToResponse(consultaRepository.save(consulta));
    }

    // =========================================================================================
    // 🔍 QUERIES
    // =========================================================================================

    @Transactional(readOnly = true)
    public List<ConsultaResponse> obtenerTodas() {
        return consultaRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> obtenerMisConsultas(String email) {
        return consultaRepository.findByNutricionistaUsuarioEmail(email).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> obtenerHistorialFarmacia(String email) {
        return consultaRepository.findByFarmaciaUsuarioEmailOrderByFechaDesc(email).stream().map(this::mapToResponse).collect(Collectors.toList());
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