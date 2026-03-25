package com.nutripharma.api_nutripharma.organization.nutricionistas.repository;

import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutricionistaRepository extends JpaRepository<Nutricionista, Long> {

    // --- 1. ANIDAMOS LA INTERFAZ DE PROYECCIÓN AQUÍ DENTRO ---
    interface NutriInactivoProjection {
        Long getId();

        String getNombre();

        String getApellidos();

        String getDni();

        String getEmail();
    }

    // --- 2. MÉTODOS DEL REPOSITORIO ---
    boolean existsByDni(String dni);

    Optional<Nutricionista> findByUsuarioEmail(String email);

    @Query(value = "SELECT n.id as id, n.nombre as nombre, n.apellidos as apellidos, n.dni as dni, u.email as email " +
            "FROM nutricionistas n JOIN usuarios u ON n.usuario_id = u.id " +
            "WHERE n.activo = false", nativeQuery = true)
    List<NutriInactivoProjection> findHistorialBajas();

    // 1. Resucita el "Alma" (Credenciales)
    @Modifying
    @Query(value = "UPDATE usuarios SET activo = true WHERE id = (SELECT usuario_id FROM nutricionistas WHERE id = ?1)", nativeQuery = true)
    void reactivarUsuario(Long id);

    // 2. Resucita el "Cuerpo" (Nutricionista)
    @Modifying
    @Query(value = "UPDATE nutricionistas SET activo = true WHERE id = ?1", nativeQuery = true)
    void reactivarNutricionista(Long id);

    @Query(value = "SELECT * FROM nutricionistas WHERE dni = :dni", nativeQuery = true)
    java.util.Optional<com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista> findByDniIgnorandoBajas(@org.springframework.data.repository.query.Param("dni") String dni);
}