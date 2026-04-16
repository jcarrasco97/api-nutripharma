package com.nutripharma.api_nutripharma.documents.facturas.repository;

import com.nutripharma.api_nutripharma.documents.facturas.domain.FacturaGasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaGastoRepository extends JpaRepository<FacturaGasto, Long> {
    List<FacturaGasto> findByNutricionistaUsuarioEmail(String email);
    List<FacturaGasto> findAllByOrderByFechaSubidaDesc();
}
