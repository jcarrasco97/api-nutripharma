package com.nutripharma.api_nutripharma.core.settings.service;

import com.nutripharma.api_nutripharma.core.settings.domain.ConfiguracionGlobal;
import com.nutripharma.api_nutripharma.core.settings.repository.ConfiguracionGlobalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionGlobalService {

    private final ConfiguracionGlobalRepository repository;

    public ConfiguracionGlobalService(ConfiguracionGlobalRepository repository) {
        this.repository = repository;
    }

    public ConfiguracionGlobal obtenerConfiguracion() {
        return repository.findById(1L).orElseGet(() -> {
            ConfiguracionGlobal config = new ConfiguracionGlobal();
            config.setId(1L);
            config.setLimiteMonedero(80.0);
            return repository.save(config);
        });
    }

    @Transactional
    public ConfiguracionGlobal actualizarLimiteMonedero(Double nuevoLimite) {
        if (nuevoLimite == null || nuevoLimite < 0) {
            throw new IllegalArgumentException("El límite debe ser un número positivo");
        }
        ConfiguracionGlobal config = obtenerConfiguracion();
        config.setLimiteMonedero(nuevoLimite);
        return repository.save(config);
    }
}
