package com.nutripharma.api_nutripharma.operations.consultas.controller;

import com.nutripharma.api_nutripharma.operations.consultas.controller.dto.ConsultaDTO.ConsultaRequest;
import com.nutripharma.api_nutripharma.operations.consultas.controller.dto.ConsultaDTO.ConsultaResponse;
import com.nutripharma.api_nutripharma.operations.consultas.service.ConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConsultaController {

    private final ConsultaService consultaService;

    @PostMapping
    @PreAuthorize("hasRole('NUTRICIONISTA') or hasRole('ADMIN')")
    public ResponseEntity<ConsultaResponse> registrarTurno(@RequestBody ConsultaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.registrarTurno(request));
    }

    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('NUTRICIONISTA') or hasRole('ADMIN')")
    public ResponseEntity<ConsultaResponse> confirmarTurno(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.confirmarTurno(id));
    }

    @PutMapping("/{id}/incidencia")
    @PreAuthorize("hasRole('NUTRICIONISTA')")
    public ResponseEntity<ConsultaResponse> reportarIncidencia(@PathVariable Long id, @RequestBody String mensaje) {
        return ResponseEntity.ok(consultaService.reportarIncidencia(id, mensaje));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA')")
    public ResponseEntity<List<ConsultaResponse>> listarTodas() {
        return ResponseEntity.ok(consultaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA') or hasRole('FARMACIA')")
    public ResponseEntity<ConsultaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.obtenerPorId(id));
    }

    @GetMapping("/mis-consultas")
    @PreAuthorize("hasRole('NUTRICIONISTA')")
    public ResponseEntity<List<ConsultaResponse>> obtenerMisConsultas(java.security.Principal principal) {
        return ResponseEntity.ok(consultaService.obtenerMisConsultas(principal.getName()));
    }

    @GetMapping("/historial-farmacia")
    @PreAuthorize("hasRole('FARMACIA')")
    public ResponseEntity<List<ConsultaResponse>> obtenerHistorialFarmacia(java.security.Principal principal) {
        return ResponseEntity.ok(consultaService.obtenerHistorialFarmacia(principal.getName()));
    }

    // 👇 NUEVA RUTA PARA EL ADMIN 👇
    @PutMapping("/{id}/validar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConsultaResponse> validarTurnoAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.validarTurno(id));
    }

    // 👇 NUEVAS RUTAS DE GESTIÓN AVANZADA (MODO EDICIÓN E INCIDENCIAS) 👇

    @PutMapping("/{id}/editar-validar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConsultaResponse> editarYValidarTurnoAdmin(
            @PathVariable Long id,
            @RequestBody com.nutripharma.api_nutripharma.operations.consultas.controller.dto.ConsultaDTO.ConsultaRequest request) {

        // Usamos el request que ya tenías para aprovechar sus campos
        return ResponseEntity.ok(consultaService.editarYValidarTurnoAdmin(
                id,
                request.nuevas(),
                request.revisiones(),
                request.promociones(),
                request.personalFarmacia(),
                request.horaInicio(),
                request.horaFin()
        ));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConsultaResponse> cancelarTurnoAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.cancelarTurnoAdmin(id));
    }

    @PostMapping(value = "/{id}/evidencia", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('NUTRICIONISTA') or hasRole('ADMIN')")
    public ResponseEntity<Void> subirEvidenciaFotografica(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            java.security.Principal principal) {
        try {
            consultaService.adjuntarEvidencia(id, file, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/{id}/evidencia", produces = org.springframework.http.MediaType.IMAGE_JPEG_VALUE)
    @PreAuthorize("hasRole('NUTRICIONISTA') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> verEvidenciaFotografica(@PathVariable Long id, java.security.Principal principal) {
        try {
            byte[] imagen = consultaService.descargarEvidencia(id, principal.getName());
            return ResponseEntity.ok().body(imagen);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/evidencia")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConsultaResponse> eliminarEvidenciaAdmin(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(consultaService.eliminarEvidenciaAdmin(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/liquidar-lote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConsultaResponse>> liquidarTurnosLote(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(consultaService.liquidarTurnosLote(ids));
    }
}