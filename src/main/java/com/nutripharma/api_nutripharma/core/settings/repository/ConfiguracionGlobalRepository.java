package com.nutripharma.api_nutripharma.core.settings.repository;

import com.nutripharma.api_nutripharma.core.settings.domain.ConfiguracionGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionGlobalRepository extends JpaRepository<ConfiguracionGlobal, Long> {
}
