package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.HistoricoImportacaoAlunos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricoImportacaoAlunosRepository extends JpaRepository<HistoricoImportacaoAlunos, Long> {
    List<HistoricoImportacaoAlunos> findByPersonalIdOrderByDataImportacaoDesc(Long personalId);
    Optional<HistoricoImportacaoAlunos> findByIdAndPersonalId(Long id, Long personalId);
}
