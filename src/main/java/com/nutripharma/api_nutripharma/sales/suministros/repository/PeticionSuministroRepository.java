package com.nutripharma.api_nutripharma.sales.suministros.repository;

import com.nutripharma.api_nutripharma.sales.suministros.domain.PeticionSuministro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeticionSuministroRepository extends JpaRepository<PeticionSuministro, Long> {

    // AÑADIDO: Privacidad -> Obtener solo las peticiones de la nutricionista que inicia sesión
    List<PeticionSuministro> findByNutricionistaUsuarioEmailOrderByFechaPeticionDesc(String email);

    // AÑADIDO: Regla Anti-Spam -> Sacar los IDs de los materiales que ya tiene en estado "SOLICITADO"
    @Query("SELECT m.id FROM PeticionSuministro p JOIN p.materialesSolicitados m WHERE p.nutricionista.usuario.email = :email AND p.estado = 'SOLICITADO'")
    List<Long> findMaterialesBloqueadosParaNutricionista(@Param("email") String email);
}