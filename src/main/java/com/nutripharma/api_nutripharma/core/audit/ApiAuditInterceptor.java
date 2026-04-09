package com.nutripharma.api_nutripharma.core.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class ApiAuditInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String method = request.getMethod();

        // 1. FILTRO: Solo nos interesan las acciones de escritura, modificación o borrado
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) ||
                "DELETE".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {

            String uri = request.getRequestURI();

            // Evitamos loguear el propio login, ya que eso ya lo guarda el Evento de Login en BD
            if (uri.contains("/auth/login") || uri.contains("/auth/reset-password")) {
                return;
            }

            int status = response.getStatus();

            // 2. EXTRAER IP (Con soporte para servidores Proxy/VPS)
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }

            // 3. EXTRAER USUARIO (Del Contexto de Seguridad)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String usuario = (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser"))
                    ? auth.getName()
                    : "No Autenticado / Sistema";

            // 4. FORMATEAR FECHA Y LOGUEAR
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            // Si el status es de error (4xx o 5xx), lo pintamos como WARN o ERROR en la consola
            if (status >= 400) {
                log.warn("[{}] IP: {} | Usuario: {} | Acción: {} {} | Status: {}",
                        fecha, ipAddress, usuario, method, uri, status);
            } else {
                log.info("[{}] IP: {} | Usuario: {} | Acción: {} {} | Status: {}",
                        fecha, ipAddress, usuario, method, uri, status);
            }
        }
    }
}