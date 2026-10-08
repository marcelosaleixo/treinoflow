package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.Treino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TreinoRepository extends JpaRepository<Treino, Long> {

    List<Treino> findByAlunoIdOrderByDataCriacaoDesc(Long alunoId);
    Page<Treino> findByAlunoIdOrderByDataCriacaoDesc(Long alunoId, Pageable pageable);

    Page<Treino> findByAlunoIdAndNomeContainingIgnoreCaseOrderByDataCriacaoDesc(Long alunoId, String nome, Pageable pageable);
    Page<Treino> findByAlunoIdAndStatusOrderByDataCriacaoDesc(Long alunoId, String status, Pageable pageable);
    long countByAlunoId(Long alunoId);

    long countByAlunoIdAndStatus(Long alunoId, String status);
    Page<Treino> findByAlunoIdAndNomeContainingIgnoreCaseAndStatusOrderByDataCriacaoDesc(Long alunoId, String nome, String status, Pageable pageable);

    Optional<Treino> findByIdAndAlunoPersonalId(Long id, Long personalId);

    Optional<Treino> findByTokenAcesso(String tokenAcesso);

    @Query("""
        SELECT t FROM Treino t
        JOIN FETCH t.aluno a
        WHERE a.id = :alunoId
          AND t.status = 'LIBERADO'
          AND (t.acessoExpiraEm IS NULL OR t.acessoExpiraEm > :agora)
        ORDER BY t.dataLiberacao DESC, t.dataCriacao DESC
    """)
    List<Treino> buscarLiberadosDoPortal(@Param("alunoId") Long alunoId, @Param("agora") java.time.LocalDateTime agora);

    @Query("""
        SELECT t FROM Treino t
        JOIN FETCH t.aluno a
        WHERE t.id = :treinoId AND a.id = :alunoId AND t.status = 'LIBERADO'
          AND (t.acessoExpiraEm IS NULL OR t.acessoExpiraEm > :agora)
    """)
    Optional<Treino> buscarTreinoDoPortal(@Param("treinoId") Long treinoId, @Param("alunoId") Long alunoId, @Param("agora") java.time.LocalDateTime agora);

    @Query("""
        SELECT t
        FROM Treino t
        JOIN FETCH t.aluno a
        WHERE a.personal.id = :personalId
        ORDER BY t.dataCriacao DESC
    """)
    List<Treino> buscarRecentesDoPersonal(
            @Param("personalId") Long personalId,
            Pageable pageable);

    @Query("""
        SELECT t
        FROM Treino t
        JOIN FETCH t.aluno a
        WHERE a.personal.id = :personalId AND t.status = 'LIBERADO'
        ORDER BY t.dataUltimaVisualizacao DESC NULLS LAST, t.dataLiberacao DESC
    """)
    List<Treino> buscarTreinosLiberadosComVisualizacoes(
            @Param("personalId") Long personalId,
            Pageable pageable);

    @Query("""
        SELECT t
        FROM Treino t
        JOIN FETCH t.aluno a
        WHERE a.personal.id = :personalId
          AND t.status = 'LIBERADO'
          AND t.acessoExpiraEm IS NOT NULL
          AND t.acessoExpiraEm > :agora
          AND t.acessoExpiraEm <= :limite
        ORDER BY t.acessoExpiraEm ASC
    """)
    List<Treino> buscarLinksExpirando(
            @Param("personalId") Long personalId,
            @Param("agora") java.time.LocalDateTime agora,
            @Param("limite") java.time.LocalDateTime limite);

    @Query("SELECT COUNT(t) FROM Treino t WHERE t.aluno.personal.id = :personalId")
    long contarTodosDoPersonal(@Param("personalId") Long personalId);

    @Query("SELECT COUNT(t) FROM Treino t WHERE t.aluno.personal.id = :personalId AND t.status = :status")
    long contarPorPersonalEStatus(@Param("personalId") Long personalId, @Param("status") String status);

    @Query("""
        SELECT t
        FROM Treino t
        JOIN FETCH t.aluno a
        WHERE t.id = :id
          AND a.personal.id = :personalId
    """)
    Optional<Treino> buscarPorIdComAlunoEPersonal(
            @Param("id") Long id,
            @Param("personalId") Long personalId);
}
