package com.nutripharma.api_nutripharma.clinical.pacientes.repository;

import com.nutripharma.api_nutripharma.clinical.pacientes.domain.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    // El método que faltaba y causaba el error
    Optional<Paciente> findByDniOrEmail(String dni, String email);

    Optional<Paciente> findByDni(String dni);

    Optional<Paciente> findByEmail(String email);

    // Para nuestro nuevo buscador predictivo
    List<Paciente> findByDniStartingWith(String dni);
}