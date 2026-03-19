package com.nutripharma.api_nutripharma.sales.suministros.repository;

import com.nutripharma.api_nutripharma.sales.suministros.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {}