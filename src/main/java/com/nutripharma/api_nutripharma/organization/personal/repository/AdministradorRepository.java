package com.nutripharma.api_nutripharma.organization.personal.repository;

import com.nutripharma.api_nutripharma.organization.personal.domain.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    interface AdminInactivoProjection {
        Long getId();

        String getNombre();

        String getApellidos();

        String getEmail();
    }

    Optional<Administrador> findByUsuarioEmail(String email);

    @Query(value = "SELECT a.id as id, a.nombre as nombre, a.apellidos as apellidos, u.email as email " +
            "FROM administradores a JOIN usuarios u ON a.usuario_id = u.id " +
            "WHERE a.activo = false", nativeQuery = true)
    List<AdminInactivoProjection> findHistorialBajas();
}