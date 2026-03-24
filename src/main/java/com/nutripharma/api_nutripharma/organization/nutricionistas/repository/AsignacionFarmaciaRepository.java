package com.nutripharma.api_nutripharma.organization.nutricionistas.repository;

import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.AsignacionFarmacia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionFarmaciaRepository extends JpaRepository<AsignacionFarmacia, Long> {
    // Método para borrar todas las menciones a una farmacia específica
    void deleteByFarmaciaId(Long farmaciaId);
}