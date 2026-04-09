package com.nutripharma.api_nutripharma.security.repository;

import com.nutripharma.api_nutripharma.security.domain.RegistroAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Long> {
    // No hace falta escribir nada aquí dentro, JpaRepository ya tiene el método save()
}