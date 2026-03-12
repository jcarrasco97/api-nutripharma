package com.nutripharma.api_nutripharma.clinical.consultas.repository;

import com.nutripharma.api_nutripharma.clinical.consultas.domain.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Spring crea la consulta SQL automáticamente solo con leer el nombre del método
    List<Consulta> findByPacienteIdPacienteOrderByFechaConsultaDesc(Long pacienteId);
}