package com.nutripharma.api_nutripharma.security.repository;

import com.nutripharma.api_nutripharma.security.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    // Lo usaremos al registrar un nuevo usuario para asignarle su rol buscando por el nombre
    Optional<Rol> findByNombre(String nombre);
}