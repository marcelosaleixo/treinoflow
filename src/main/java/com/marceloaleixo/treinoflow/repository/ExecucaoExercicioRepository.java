package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.ExecucaoExercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExecucaoExercicioRepository extends JpaRepository<ExecucaoExercicio, Long> {
    Optional<ExecucaoExercicio> findByRegistroIdAndTreinoExercicioId(Long registroId, Long treinoExercicioId);

    @Query("""
        SELECT e FROM ExecucaoExercicio e
        JOIN FETCH e.registro r
        JOIN FETCH e.treinoExercicio te
        JOIN FETCH te.exercicio ex
        WHERE r.aluno.id = :alunoId
        ORDER BY r.dataExecucao DESC, te.ordem ASC
    """)
    List<ExecucaoExercicio> buscarHistoricoDoAluno(@Param("alunoId") Long alunoId);
}
