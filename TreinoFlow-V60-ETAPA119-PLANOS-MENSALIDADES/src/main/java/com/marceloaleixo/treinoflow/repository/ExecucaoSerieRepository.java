package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.ExecucaoSerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExecucaoSerieRepository extends JpaRepository<ExecucaoSerie, Long> {
    Optional<ExecucaoSerie> findByRegistroIdAndTreinoExercicioIdAndNumeroSerie(Long registroId, Long treinoExercicioId, Integer numeroSerie);

    List<ExecucaoSerie> findByRegistroIdAndTreinoExercicioIdOrderByNumeroSerieAsc(Long registroId, Long treinoExercicioId);

    @Query("""
        SELECT s FROM ExecucaoSerie s
        JOIN FETCH s.registro r
        WHERE s.treinoExercicio.id = :itemId
          AND r.aluno.id = :alunoId
          AND r.dataExecucao < :data
        ORDER BY r.dataExecucao DESC, s.numeroSerie DESC
    """)
    List<ExecucaoSerie> buscarHistoricoAnterior(@Param("itemId") Long itemId,
                                                 @Param("alunoId") Long alunoId,
                                                 @Param("data") LocalDate data);

    @Query("SELECT s FROM ExecucaoSerie s JOIN FETCH s.treinoExercicio te WHERE s.registro.id = :registroId ORDER BY te.ordem ASC, s.numeroSerie ASC")
    List<ExecucaoSerie> buscarPorRegistro(@Param("registroId") Long registroId);
}
