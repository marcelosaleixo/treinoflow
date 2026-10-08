package com.marceloaleixo.treinoflow.repository;
import com.marceloaleixo.treinoflow.entity.ModeloTreinoExercicio;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository public interface ModeloTreinoExercicioRepository extends JpaRepository<ModeloTreinoExercicio,Long>{
 @Query("SELECT m FROM ModeloTreinoExercicio m JOIN FETCH m.exercicio WHERE m.modelo.id=:modeloId ORDER BY m.ordem ASC")
 List<ModeloTreinoExercicio> listar(@Param("modeloId") Long modeloId);
 void deleteByModeloId(Long modeloId);
 boolean existsByModeloIdAndExercicioId(Long modeloId, Long exercicioId);
 java.util.Optional<ModeloTreinoExercicio> findByIdAndModeloId(Long id, Long modeloId);
 @Query("SELECT COALESCE(MAX(m.ordem),0)+1 FROM ModeloTreinoExercicio m WHERE m.modelo.id=:modeloId")
 int proximaOrdem(@Param("modeloId") Long modeloId);
}
