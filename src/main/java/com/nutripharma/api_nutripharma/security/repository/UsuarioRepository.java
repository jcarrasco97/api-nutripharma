package com.nutripharma.api_nutripharma.security.repository;

import com.nutripharma.api_nutripharma.documents.documentacion.controller.dto.DocumentoDTO.UsuarioDestinatarioProjection;
import com.nutripharma.api_nutripharma.security.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // Consulta nativa que cruza las 3 tablas para sacar Email + Nombre Real
    @Query(value = "SELECT u.email as email, " +
            "COALESCE(CONCAT(n.nombre, ' ', n.apellidos), f.nombre, 'Administrador / Sin Nombre') as nombreCompleto " +
            "FROM usuarios u " +
            "LEFT JOIN nutricionistas n ON n.usuario_id = u.id " +
            "LEFT JOIN farmacias f ON f.usuario_id = u.id " +
            "WHERE u.activo = true", nativeQuery = true)
    List<UsuarioDestinatarioProjection> findUsuariosParaDesplegable();

    Optional<Usuario> findByResetPasswordToken(String token);
}