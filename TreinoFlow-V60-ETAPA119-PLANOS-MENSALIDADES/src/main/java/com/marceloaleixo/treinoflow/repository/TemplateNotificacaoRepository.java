package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.TemplateNotificacao;
import com.marceloaleixo.treinoflow.enums.CanalNotificacao;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateNotificacaoRepository extends JpaRepository<TemplateNotificacao, Long> {
    Optional<TemplateNotificacao> findByTipoAndCanal(TipoNotificacao tipo, CanalNotificacao canal);
    List<TemplateNotificacao> findAllByOrderByTipoAscCanalAsc();
}
