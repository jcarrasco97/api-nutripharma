package com.nutripharma.api_nutripharma.organization.farmacias.repository;

import com.nutripharma.api_nutripharma.organization.farmacias.domain.Farmacia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FarmaciaRepository extends JpaRepository<Farmacia, Long> {
    boolean existsByCif(String cif);
}