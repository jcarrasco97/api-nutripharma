package com.nutripharma.api_nutripharma.security.repository;

import com.nutripharma.api_nutripharma.security.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Fundamental para Spring Security: buscar a la persona cuando intente hacer login
    Optional<Usuario> findByEmail(String email);

    // Muy útil para validar que no nos intenten registrar un email que ya existe
    boolean existsByEmail(String email);
}