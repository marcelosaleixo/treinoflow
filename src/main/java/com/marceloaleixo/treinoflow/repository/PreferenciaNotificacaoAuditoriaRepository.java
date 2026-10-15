package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.PreferenciaNotificacaoAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PreferenciaNotificacaoAuditoriaRepository extends JpaRepository<PreferenciaNotificacaoAuditoria, Long> {
    Page<PreferenciaNotificacaoAuditoria> findByPersonalIdOrderByAlteradoEmDescIdDesc(Long personalId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PreferenciaNotificacaoAuditoria a where a.alteradoEm < :limite")
    int excluirAnterioresA(@Param("limite") LocalDateTime limite);
}
