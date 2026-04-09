package com.nutripharma.api_nutripharma.core.audit;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditoriaRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        AuditoriaRevisionEntity auditoria = (AuditoriaRevisionEntity) revisionEntity;

        // Leemos la identidad del usuario desde el hilo de seguridad de Spring (JWT)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            // Es un Admin, Nutricionista o Farmacia real
            auditoria.setUsuarioEmail(auth.getName());
        } else {
            // Son acciones internas de Spring Boot (ej. el DataSeeder al arrancar)
            auditoria.setUsuarioEmail("SISTEMA_INTERNO");
        }
    }
}