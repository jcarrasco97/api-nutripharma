package com.nutripharma.api_nutripharma.security.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_accesos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_email", nullable = false)
    private String usuarioEmail;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    // Aquí guardaremos si entra desde Chrome, Safari, un móvil Android...
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "fecha_acceso", nullable = false)
    private LocalDateTime fechaAcceso;

    // Por si en el futuro queremos registrar también los intentos fallidos de hackeo
    @Column(name = "exito", nullable = false)
    private boolean exito;
}