package com.nutripharma.api_nutripharma.core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de la documentación OpenAPI (Swagger).
 * Aquí definimos los metadatos de la API y, vitalmente, instruimos a Swagger
 * para que inyecte el token JWT (Bearer Auth) en las cabeceras HTTP de sus peticiones.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Nutripharma API REST",
                version = "1.0",
                description = "Documentación interactiva del ERP/CRM Clínico y Farmacéutico. Arquitectura Greenfield."
        ),
        security = {
                // Aplica este esquema de seguridad por defecto a todos los endpoints
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZG1pbkBudXRyaXBoYXJtYS5jb20iLCJpYXQiOjE3NzMzMDg4OTMsImV4cCI6MTc3MzM5NTI5M30.2_39C1mtmfZIqwIEnWjU55tyLJjFyejJGftT-jJLQfLeBohG5ORom_jOe9Fc4K_Q",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}