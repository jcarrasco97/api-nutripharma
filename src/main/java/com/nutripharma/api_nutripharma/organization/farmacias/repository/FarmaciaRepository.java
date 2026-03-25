package com.nutripharma.api_nutripharma.organization.farmacias.repository;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmaciaRepository extends JpaRepository<Farmacia, Long> {

    // --- 1. ANIDAMOS LA INTERFAZ DE PROYECCIÓN AQUÍ DENTRO ---
    interface FarmaciaInactivaProjection {
        Long getId();

        String getNombre();

        String getCif();

        String getDireccion();

        String getEmail();

        java.time.LocalDateTime getFechaBaja();

        String getBorradoPor();
    }

    // --- 2. MÉTODOS DEL REPOSITORIO ---
    boolean existsByCif(String cif);

    Optional<Farmacia> findByUsuarioEmail(String email);

    @Query(value = "SELECT f.id as id, f.nombre as nombre, f.cif as cif, f.direccion as direccion, u.email as email, f.fecha_baja as fechaBaja, f.borrado_por as borradoPor " +
            "FROM farmacias f JOIN usuarios u ON f.usuario_id = u.id " +
            "WHERE f.activo = false", nativeQuery = true)
    List<FarmaciaInactivaProjection> findHistorialBajas();

    // 1. Resucita el "Alma" (Credenciales)
    @Modifying
    @Query(value = "UPDATE usuarios SET activo = true WHERE id = (SELECT usuario_id FROM farmacias WHERE id = ?1)", nativeQuery = true)
    void reactivarUsuario(Long id);

    // 2. Resucita el "Cuerpo" (Farmacia)
    @Modifying
    @Query(value = "UPDATE farmacias SET activo = true, fecha_baja = NULL, borrado_por = NULL WHERE id = ?1", nativeQuery = true)
    void reactivarFarmacia(Long id);

    @Query(value = "SELECT * FROM farmacias WHERE cif = :cif", nativeQuery = true)
    java.util.Optional<com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia> findByCifIgnorandoBajas(@org.springframework.data.repository.query.Param("cif") String cif);
}