package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.Notificacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    boolean existsByChaveUnica(String chaveUnica);

    @EntityGraph(attributePaths = {"cobranca", "cobranca.assinatura", "cobranca.assinatura.plano"})
    List<Notificacao> findByPersonalIdOrderByDataCriacaoDesc(Long personalId);

    @EntityGraph(attributePaths = {"cobranca", "cobranca.assinatura", "cobranca.assinatura.plano"})
    List<Notificacao> findTop10ByPersonalIdOrderByDataCriacaoDesc(Long personalId);

    long countByPersonalIdAndLidaFalse(Long personalId);
}
