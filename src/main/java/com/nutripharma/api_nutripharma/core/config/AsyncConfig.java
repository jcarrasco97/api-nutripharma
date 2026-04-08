package com.nutripharma.api_nutripharma.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Magia pura: Esta anotación enciende el motor multi-hilo de Spring.
}