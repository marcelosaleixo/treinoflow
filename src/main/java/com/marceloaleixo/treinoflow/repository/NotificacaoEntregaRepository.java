package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.NotificacaoEntrega;
import com.marceloaleixo.treinoflow.enums.CanalNotificacao;
import com.marceloaleixo.treinoflow.enums.StatusEntregaNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacaoEntregaRepository extends JpaRepository<NotificacaoEntrega, Long> {
    boolean existsByNotificacaoIdAndCanal(Long notificacaoId, CanalNotificacao canal);
    List<NotificacaoEntrega> findTop100ByStatusInAndProximaTentativaLessThanEqualOrderByProximaTentativaAsc(
            List<StatusEntregaNotificacao> statuses, LocalDateTime limite);
}
