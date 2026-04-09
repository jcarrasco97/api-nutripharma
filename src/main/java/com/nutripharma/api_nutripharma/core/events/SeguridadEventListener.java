package com.nutripharma.api_nutripharma.core.events;

import com.nutripharma.api_nutripharma.security.domain.RegistroAcceso;
import com.nutripharma.api_nutripharma.security.repository.RegistroAccesoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeguridadEventListener {

    private final RegistroAccesoRepository accesoRepository;

    @Async // Lo hacemos asíncrono para no retrasar el inicio de sesión del usuario
    @EventListener
    public void handleLoginSuccess(LoginSuccessEvent event) {
        log.info("🛡️ Guardando log de acceso para usuario: {} desde IP: {}", event.usuarioEmail(), event.ipAddress());

        try {
            RegistroAcceso registro = RegistroAcceso.builder()
                    .usuarioEmail(event.usuarioEmail())
                    .ipAddress(event.ipAddress())
                    .userAgent(event.userAgent())
                    .fechaAcceso(LocalDateTime.now())
                    .exito(true) // Es un evento de éxito
                    .build();

            accesoRepository.save(registro);
        } catch (Exception e) {
            log.error("❌ Fallo al guardar el registro de acceso: {}", e.getMessage());
        }
    }
}