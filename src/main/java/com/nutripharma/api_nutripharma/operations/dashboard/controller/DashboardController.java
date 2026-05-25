package com.nutripharma.api_nutripharma.operations.dashboard.controller;

import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.ResumenMensualNutricionista;
import com.nutripharma.api_nutripharma.operations.dashboard.service.DashboardService;
import com.nutripharma.api_nutripharma.operations.dashboard.service.InformeExcelService;
import com.nutripharma.api_nutripharma.operations.dashboard.service.InformePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final InformePdfService informePdfService;
    private final InformeExcelService informeExcelService;

    private static final String EXCEL_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @GetMapping("/nutricionistas/{id}/resumen")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NUTRICIONISTA')")
    public ResponseEntity<ResumenMensualNutricionista> obtenerResumen(
            @PathVariable Long id,
            @RequestParam int anio,
            @RequestParam int mes) {
        return ResponseEntity.ok(dashboardService.calcularResumenNutricionista(id, anio, mes));
    }

    // 👇 NUEVAS RUTAS PARA EL ADMIN 👇

    @GetMapping("/admin/facturacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardDTO.ReporteJerarquicoDTO<List<DashboardDTO.FacturacionMensualDTO>>> obtenerFacturacionGlobal(
            @RequestParam int anioInicio,
            @RequestParam int anioFin,
            @RequestParam(required = false) Long farmaciaId,
            @RequestParam(required = false) Long nutricionistaId) {
        return ResponseEntity
                .ok(dashboardService.obtenerFacturacionRango(anioInicio, anioFin, farmaciaId, nutricionistaId));
    }

    @GetMapping("/admin/calendario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DashboardDTO.EventoCalendarioDTO>> obtenerEventosCalendario(
            @RequestParam int anio,
            @RequestParam int mes) {
        return ResponseEntity.ok(dashboardService.obtenerEventosCalendario(anio, mes));
    }

    @GetMapping("/admin/auditoria/{nutricionistaId}/meses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> obtenerMesesAuditoria(@PathVariable Long nutricionistaId) {
        return ResponseEntity.ok(dashboardService.obtenerMesesActividadNutricionista(nutricionistaId));
    }

    @GetMapping("/admin/rendimiento-productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardDTO.ReporteJerarquicoDTO<List<DashboardDTO.RendimientoProductoDTO>>> obtenerRendimientoProductos(
            @RequestParam int anioInicio,
            @RequestParam int anioFin,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Long farmaciaId,
            @RequestParam(required = false) Long nutricionistaId) {
        return ResponseEntity.ok(dashboardService.obtenerRendimientoProductosRango(anioInicio, anioFin, mes, farmaciaId,
                nutricionistaId));
    }

    @PostMapping(value = "/admin/rendimiento-productos/pdf", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarInformeProductosPdf(@RequestBody DashboardDTO.InformePdfRequestDTO request) {
        byte[] pdf = informePdfService.generarInformeRendimientoProductosPdf(request);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentDispositionFormData("attachment", "informe_productos_" + request.getAnioInicio() + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @PostMapping(value = "/admin/facturacion/pdf", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarInformeFacturacionPdf(
            @RequestBody DashboardDTO.InformePdfRequestDTO request) {
        byte[] pdf = informePdfService.generarInformeFacturacionPdf(request);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentDispositionFormData("attachment", "informe_facturacion_" + request.getAnioInicio() + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/admin/rendimiento-clinico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardDTO.ReporteJerarquicoDTO<List<DashboardDTO.RendimientoClinicoDTO>>> obtenerRendimientoClinico(
            @RequestParam int anioInicio,
            @RequestParam int anioFin,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Long farmaciaId,
            @RequestParam(required = false) Long nutricionistaId) {
        return ResponseEntity
                .ok(dashboardService.obtenerRendimientoClinicoRango(anioInicio, anioFin, mes, nutricionistaId, farmaciaId));
    }

    @PostMapping(value = "/admin/clinico/pdf", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarInformeClinicoPdf(@RequestBody DashboardDTO.InformePdfRequestDTO request) {
        byte[] pdf = informePdfService.generarInformeClinicoPdf(request);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentDispositionFormData("attachment", "informe_clinico_" + request.getAnioInicio() + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ENDPOINTS DE INFORMES EXCEL (.xlsx)
    // ─────────────────────────────────────────────────────────────────────

    @PostMapping(value = "/admin/rendimiento-productos/excel", produces = EXCEL_MIME)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarInformeProductosExcel(@RequestBody DashboardDTO.InformePdfRequestDTO request) {
        byte[] xlsx = informeExcelService.generarInformeRendimientoProductosExcel(request);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentDispositionFormData("attachment", "informe_productos_" + request.getAnioInicio() + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(xlsx);
    }

    @PostMapping(value = "/admin/facturacion/excel", produces = EXCEL_MIME)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarInformeFacturacionExcel(@RequestBody DashboardDTO.InformePdfRequestDTO request) {
        byte[] xlsx = informeExcelService.generarInformeFacturacionExcel(request);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentDispositionFormData("attachment", "informe_facturacion_" + request.getAnioInicio() + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(xlsx);
    }

    @GetMapping("/admin/ventas-farmacia")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DashboardDTO.FacturacionPorFarmaciaDTO>> obtenerVentasPorFarmacia(
            @RequestParam int anioInicio,
            @RequestParam int anioFin,
            @RequestParam(required = false) Long nutricionistaId) {
        return ResponseEntity.ok(dashboardService.obtenerVentasPorFarmacia(anioInicio, anioFin, nutricionistaId));
    }

    @PostMapping(value = "/admin/ventas-farmacia/pdf", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarInformeVentasFarmaciaPdf(@RequestBody DashboardDTO.InformePdfRequestDTO request) {
        byte[] pdf = informePdfService.generarInformeVentasPorFarmaciaPdf(request);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentDispositionFormData("attachment", "informe_ventas_farmacia_" + request.getAnioInicio() + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @PostMapping(value = "/admin/clinico/excel", produces = EXCEL_MIME)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarInformeClinicoExcel(@RequestBody DashboardDTO.InformePdfRequestDTO request) {
        byte[] xlsx = informeExcelService.generarInformeClinicoExcel(request);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentDispositionFormData("attachment", "informe_clinico_" + request.getAnioInicio() + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(xlsx);
    }

    @PostMapping(value = "/admin/ventas-farmacia/excel", produces = EXCEL_MIME)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarInformeVentasFarmaciaExcel(@RequestBody DashboardDTO.InformePdfRequestDTO request) {
        byte[] xlsx = informeExcelService.generarInformeVentasFarmaciaExcel(request);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentDispositionFormData("attachment", "informe_ventas_farmacia_" + request.getAnioInicio() + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(xlsx);
    }
}