package com.nutripharma.api_nutripharma.organization.farmacias.saldo.repository;

import com.nutripharma.api_nutripharma.organization.farmacias.saldo.domain.SaldoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaldoMovimientoRepository extends JpaRepository<SaldoMovimiento, Long> {

    List<SaldoMovimiento> findByFarmaciaIdOrderByFechaDesc(Long farmaciaId);
}
