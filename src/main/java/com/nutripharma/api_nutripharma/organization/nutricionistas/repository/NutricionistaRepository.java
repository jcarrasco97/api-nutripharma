package com.nutripharma.api_nutripharma.organization.nutricionistas.repository;

import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NutricionistaRepository extends JpaRepository<Nutricionista, Long> {

    // Spring Data crea la consulta SQL automáticamente solo con leer el nombre del método
    boolean existsByDni(String dni);

    Optional<Nutricionista> findByUsuarioEmail(String email);
}