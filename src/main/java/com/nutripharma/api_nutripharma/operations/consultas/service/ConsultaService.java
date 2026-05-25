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
import com.nutripharma.api_nutripharma.security.repository.UsuarioRepository;
import com.nutripharma.api_nutripharma.documents.documentacion.service.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final FarmaciaRepository farmaciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final GoogleDriveService googleDriveService;

    @Transactional
    public ConsultaResponse registrarTurno(ConsultaRequest request) {

        // 1. Validaciones de Coherencia Temporal Básica
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
                .orElseThrow(() -> new NoSuchElementException("Nutricionista no encontrado"));

        Farmacia farmacia = farmaciaRepository.findById(request.farmaciaId())
                .orElseThrow(() -> new NoSuchElementException("Farmacia no encontrada"));

        Consulta nuevaConsulta = Consulta.builder()
                .nutricionista(nutricionista)
                .farmacia(farmacia)
                .fecha(request.fecha())
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
                .orElseThrow(() -> new NoSuchElementException("Consulta no encontrada"));

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
                .orElseThrow(() -> new NoSuchElementException("Consulta no encontrada"));

        if (consulta.getEstado() == EstadoConsulta.VALIDADA) {
            throw new IllegalStateException("Esta consulta ya ha sido validada y liquidada anteriormente.");
        }
        if (consulta.getEstado() == EstadoConsulta.CANCELADA) {
            throw new IllegalStateException("No se puede validar una consulta cancelada.");
        }

        consulta.setMensajeIncidencia(null);
        aplicarSaldoFarmacia(consulta);

        consulta.setEstado(EstadoConsulta.VALIDADA);
        return mapToResponse(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaResponse editarYValidarTurnoAdmin(Long id, Integer nuevas, Integer revisiones, Integer promociones, Integer personalFarmacia, java.time.LocalTime horaInicio, java.time.LocalTime horaFin) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Consulta no encontrada"));

        if (consulta.getEstado() == EstadoConsulta.CANCELADA) {
            throw new IllegalStateException("No se puede editar una consulta cancelada.");
        }

        if (horaInicio != null && horaFin != null) {
            if (!horaFin.isAfter(horaInicio)) {
                throw new IllegalArgumentException("La hora de salida debe ser posterior a la de entrada.");
            }
            consulta.setHoraInicio(horaInicio);
            consulta.setHoraFin(horaFin);
        }

        if (consulta.getEstado() == EstadoConsulta.VALIDADA) {
            revertirSaldoFarmacia(consulta);
        }

        consulta.setNuevas(nuevas != null ? nuevas : 0);
        consulta.setRevisiones(revisiones != null ? revisiones : 0);
        consulta.setPromociones(promociones != null ? promociones : 0);
        consulta.setPersonalFarmacia(personalFarmacia != null ? personalFarmacia : consulta.getPersonalFarmacia());

        consulta.setMensajeIncidencia(null);
        aplicarSaldoFarmacia(consulta);

        consulta.setEstado(EstadoConsulta.VALIDADA);
        return mapToResponse(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaResponse cancelarTurnoAdmin(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Consulta no encontrada"));

        if (consulta.getEstado() == EstadoConsulta.CANCELADA) {
            throw new IllegalStateException("La consulta ya está cancelada.");
        }

        if (consulta.getEstado() == EstadoConsulta.VALIDADA) {
            revertirSaldoFarmacia(consulta);
        }

        consulta.setEstado(EstadoConsulta.CANCELADA);
        return mapToResponse(consultaRepository.save(consulta));
    }

    @Transactional
    public List<ConsultaResponse> liquidarTurnosLote(List<Long> ids) {
        List<Consulta> consultas = consultaRepository.findAllById(ids);

        for (Consulta c : consultas) {
            if (c.getEstado() != EstadoConsulta.VALIDADA) {
                throw new IllegalStateException(
                        "Solo se pueden liquidar consultas que previamente hayan sido validadas. " +
                        "La consulta con ID " + c.getId() + " tiene estado: " + c.getEstado()
                );
            }
        }

        consultas.forEach(c -> c.setEstado(EstadoConsulta.LIQUIDADA));

        return consultaRepository.saveAll(consultas).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================================================
    // 🛡️ MÉTODOS PRIVADOS CONTABLES
    // =========================================================================================

    private void aplicarSaldoFarmacia(Consulta consulta) {
        // Escudo: Si la farmacia fue borrada, no podemos darle dinero
        if (consulta.getFarmacia() == null) return;

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
        // Escudo: Si la farmacia fue borrada, no hay a quien quitarle el dinero
        if (consulta.getFarmacia() == null) return;

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
                .orElseThrow(() -> new NoSuchElementException("Consulta no encontrada"));
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
    public ConsultaResponse obtenerPorId(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Consulta no encontrada"));
        return mapToResponse(consulta);
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> obtenerMisConsultas(String email) {
        return consultaRepository.findByNutricionistaUsuarioEmail(email).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> obtenerHistorialFarmacia(String email) {
        return consultaRepository.findByFarmaciaUsuarioEmailOrderByFechaDesc(email).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void adjuntarEvidencia(Long consultaId, org.springframework.web.multipart.MultipartFile archivo, String emailNutricionista) throws Exception {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new NoSuchElementException("Consulta no encontrada"));

        boolean esDueño = consulta.getNutricionista() != null && consulta.getNutricionista().getUsuario().getEmail().equals(emailNutricionista);
        boolean isAdmin = usuarioRepository.findByEmailIgnorandoBajas(emailNutricionista)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + emailNutricionista))
                .getRoles().stream().anyMatch(r -> r.getNombre().contains("ADMIN"));

        if (!esDueño && !isAdmin) {
            throw new AccessDeniedException("No tienes permiso para adjuntar evidencias a este turno.");
        }

        // Calcular carpeta: Agendas/{nombre nutricionista}/{año-mes de la consulta}
        String nombreNutri = consulta.getNutricionista() != null
                ? consulta.getNutricionista().getNombre() + " " + consulta.getNutricionista().getApellidos()
                : "Desconocido";
        String mesAnio = consulta.getFecha() != null
                ? consulta.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
                : java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

        // Nombre del archivo: fechaTurno-Idturno-IdNutri-NombreFarmacia-IdFarmacia.ext
        String fechaTurno = consulta.getFecha() != null
                ? consulta.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yy"))
                : "sin-fecha";
        String idNutri = consulta.getNutricionista() != null ? consulta.getNutricionista().getId().toString() : "0";
        String nombreFarmacia = consulta.getFarmacia() != null ? consulta.getFarmacia().getNombre().replaceAll("\\s+", "_") : "SinFarmacia";
        String idFarmacia = consulta.getFarmacia() != null ? consulta.getFarmacia().getId().toString() : "0";

        String extension = "";
        String originalFilename = archivo.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String nombreArchivo = fechaTurno + "-" + consultaId + "-" + idNutri + "-" + nombreFarmacia + "-" + idFarmacia + extension;

        String driveFileId = googleDriveService.subirEvidencia(archivo, nombreArchivo, "Agendas", nombreNutri, mesAnio);
        consulta.setEvidenciaUrl(driveFileId);
        consulta.setEvidenciaFecha(java.time.LocalDateTime.now());
        consultaRepository.save(consulta);
    }

    @Transactional(readOnly = true)
    public byte[] descargarEvidencia(Long consultaId, String emailUsuario) throws Exception {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new NoSuchElementException("Consulta no encontrada"));

        if (consulta.getEvidenciaUrl() == null) {
            throw new IllegalStateException("Esta consulta no tiene ninguna foto adjunta.");
        }

        return googleDriveService.descargarArchivo(consulta.getEvidenciaUrl());
    }

    @Transactional
    public ConsultaResponse eliminarEvidenciaAdmin(Long consultaId) throws Exception {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new NoSuchElementException("Consulta no encontrada"));

        if (consulta.getEvidenciaUrl() == null) {
            throw new IllegalStateException("Esta consulta no tiene evidencia para borrar.");
        }

        googleDriveService.eliminarArchivo(consulta.getEvidenciaUrl());
        consulta.setEvidenciaUrl(null);
        consulta.setEvidenciaFecha(null);

        return mapToResponse(consultaRepository.save(consulta));
    }

    private ConsultaResponse mapToResponse(Consulta c) {
        // 🛡️ ESCUDO ANTI-NULOS: Si el nutri o la farmacia fueron borrados, devolvemos un nombre de seguridad
        String nombreNutri = c.getNutricionista() != null
                ? c.getNutricionista().getNombre() + " " + c.getNutricionista().getApellidos()
                : "[Nutricionista Borrado]";

        String nombreFarmacia = c.getFarmacia() != null
                ? c.getFarmacia().getNombre()
                : "[Farmacia Borrada]";

        Long nutricionistaId = c.getNutricionista() != null ? c.getNutricionista().getId() : null;
        Long farmaciaId = c.getFarmacia() != null ? c.getFarmacia().getId() : null;

        return new ConsultaResponse(
                c.getId(),
                nutricionistaId,
                nombreNutri,
                farmaciaId,
                nombreFarmacia,
                c.getFecha(),
                c.getHoraInicio(),
                c.getHoraFin(),
                c.getNuevas(),
                c.getRevisiones(),
                c.getPromociones(),
                c.getPersonalFarmacia(),
                c.getObservacionesJornada(),
                c.getEstado(),
                c.getMensajeIncidencia(),
                c.getEvidenciaUrl(),
                c.getEvidenciaFecha(),
                c.getFechaCreacion()
        );
    }
}