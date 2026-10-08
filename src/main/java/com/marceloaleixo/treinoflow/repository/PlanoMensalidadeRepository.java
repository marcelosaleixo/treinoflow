package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.PlanoMensalidade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlanoMensalidadeRepository extends JpaRepository<PlanoMensalidade, Long> {
    @EntityGraph(attributePaths = "aluno")
    List<PlanoMensalidade> findByPersonalIdOrderByAtivoDescAlunoNomeAsc(Long personalId);

    @EntityGraph(attributePaths = {"aluno", "personal"})
    List<PlanoMensalidade> findByAtivoTrueOrderByIdAsc();

    @EntityGraph(attributePaths = {"aluno", "personal"})
    List<PlanoMensalidade> findByAtivoTrueAndPersonalIdOrderByIdAsc(Long personalId);

    Optional<PlanoMensalidade> findByIdAndPersonalId(Long id, Long personalId);
    boolean existsByIdAndPersonalId(Long id, Long personalId);
}
