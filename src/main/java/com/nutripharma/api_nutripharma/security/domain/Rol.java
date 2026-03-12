package com.nutripharma.api_nutripharma.security.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad de catálogo para el Control de Acceso Basado en Roles (RBAC).
 * Define qué permisos a nivel de sistema tiene un usuario autenticado.
 * Por convención de Spring Security, los nombres deben incluir el prefijo 'ROLE_'
 * (ej. ROLE_ADMIN, ROLE_NUTRICIONISTA).
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;
}