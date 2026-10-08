package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.TreinoExercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface TreinoExercicioRepository extends JpaRepository<TreinoExercicio, Long> {
    List<TreinoExercicio> findByTreinoIdOrderByOrdemAsc(Long treinoId);

    @Query("""
        SELECT item
        FROM TreinoExercicio item
        JOIN FETCH item.exercicio exercicio
        WHERE item.treino.id = :treinoId
        ORDER BY item.ordem ASC
    """)
    List<TreinoExercicio> listarComExercicioPorTreino(@Param("treinoId") Long treinoId);

    @Query("""
        SELECT item
        FROM TreinoExercicio item
        JOIN FETCH item.treino treino
        JOIN FETCH treino.aluno aluno
        JOIN FETCH item.exercicio exercicio
        WHERE aluno.personal.id = :personalId
    """)
    List<TreinoExercicio> listarDoPersonal(@Param("personalId") Long personalId);
}
