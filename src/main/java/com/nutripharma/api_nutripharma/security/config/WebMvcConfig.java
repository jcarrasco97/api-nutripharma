package com.nutripharma.api_nutripharma.security.config;

import com.nutripharma.api_nutripharma.core.audit.ApiAuditInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiAuditInterceptor apiAuditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registramos el interceptor y le decimos que vigile todo lo que entre por la
        // API
        registry.addInterceptor(apiAuditInterceptor)
                .addPathPatterns("/api/**");
    }
}