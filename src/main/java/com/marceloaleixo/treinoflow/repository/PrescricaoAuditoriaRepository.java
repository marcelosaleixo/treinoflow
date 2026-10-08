package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.PrescricaoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PrescricaoAuditoriaRepository extends JpaRepository<PrescricaoAuditoria, Long> {
    @Query("""
        SELECT a FROM PrescricaoAuditoria a
        JOIN FETCH a.treinoExercicio item
        JOIN FETCH a.aluno aluno
        WHERE a.aluno.id = :alunoId AND a.personal.id = :personalId
        ORDER BY a.dataDecisao DESC
    """)
    List<PrescricaoAuditoria> buscarRecentesDoAluno(@Param("alunoId") Long alunoId, @Param("personalId") Long personalId);

    @Query("""
        SELECT a FROM PrescricaoAuditoria a
        JOIN FETCH a.aluno aluno
        WHERE a.id = :id AND a.aluno.id = :alunoId AND a.personal.id = :personalId
    """)
    java.util.Optional<PrescricaoAuditoria> buscarPorIdDoPersonal(@Param("id") Long id,
                                                                  @Param("alunoId") Long alunoId,
                                                                  @Param("personalId") Long personalId);
}
