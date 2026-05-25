package com.nutripharma.api_nutripharma.core.settings.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "configuracion_global")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionGlobal {

    @Id
    private Long id = 1L;

    private Double limiteMonedero = 80.0;
}
