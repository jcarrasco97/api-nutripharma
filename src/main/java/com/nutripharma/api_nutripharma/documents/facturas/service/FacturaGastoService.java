package com.nutripharma.api_nutripharma.documents.facturas.service;

import com.nutripharma.api_nutripharma.documents.documentacion.service.GoogleDriveService;
import com.nutripharma.api_nutripharma.documents.facturas.domain.FacturaGasto;
import com.nutripharma.api_nutripharma.documents.facturas.repository.FacturaGastoRepository;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturaGastoService {

    private final FacturaGastoRepository facturaGastoRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final GoogleDriveService googleDriveService;

    @Transactional
    public FacturaGasto subirFactura(MultipartFile archivo, String mesCorresponde) throws IOException, GeneralSecurityException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Nutricionista nutricionista = nutricionistaRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrada para el email: " + email));

        String extension = "";
        String originalFilename = archivo.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fechaStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String nombreSeguro = (nutricionista.getNombre() != null ? nutricionista.getNombre() : "").replaceAll("\\s+", "_");
        String apellidosSeguros = (nutricionista.getApellidos() != null ? nutricionista.getApellidos() : "").replaceAll("\\s+", "_");
        
        String nombreGenerado = fechaStr + "_Km_" + nombreSeguro + "_" + apellidosSeguros + extension;

        String carpetaNutri = nombreSeguro + "_" + apellidosSeguros;
        String driveFileId = googleDriveService.subirFactura(archivo, nombreGenerado, "Facturas", carpetaNutri);

        FacturaGasto factura = FacturaGasto.builder()
                .nutricionista(nutricionista)
                .nombreArchivo(nombreGenerado)
                .driveFileId(driveFileId)
                .fechaSubida(LocalDate.now())
                .mesCorresponde(mesCorresponde)
                .build();

        return facturaGastoRepository.save(factura);
    }

    @Transactional(readOnly = true)
    public List<FacturaGasto> obtenerMisFacturas() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return facturaGastoRepository.findByNutricionistaUsuarioEmail(email);
    }

    @Transactional(readOnly = true)
    public List<FacturaGasto> obtenerTodas() {
        return facturaGastoRepository.findAllByOrderByFechaSubidaDesc();
    }

    public byte[] descargarFactura(Long id) throws IOException, GeneralSecurityException {
        FacturaGasto factura = facturaGastoRepository.findById(id).orElseThrow();
        return googleDriveService.descargarArchivo(factura.getDriveFileId());
    }

    @Transactional
    public void eliminarFactura(Long id) throws IOException, GeneralSecurityException {
        FacturaGasto factura = facturaGastoRepository.findById(id).orElseThrow();
        googleDriveService.eliminarArchivo(factura.getDriveFileId());
        facturaGastoRepository.delete(factura);
    }
}
