// Archivo 2: PeticionSuministroRepository.java
package com.nutripharma.api_nutripharma.sales.suministros.repository;
import com.nutripharma.api_nutripharma.sales.suministros.domain.PeticionSuministro;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PeticionSuministroRepository extends JpaRepository<PeticionSuministro, Long> {}