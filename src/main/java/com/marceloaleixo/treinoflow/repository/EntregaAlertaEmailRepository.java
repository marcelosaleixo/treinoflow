package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntregaAlertaEmailRepository extends JpaRepository<EntregaAlertaEmail, String> {
    @Query("select e from EntregaAlertaEmail e where (:status = 'TODOS' or e.status = :status) " +
           "and (:inicio is null or e.criadaEm >= :inicio) and (:fim is null or e.criadaEm < :fim) " +
           "order by e.criadaEm desc")
    Page<EntregaAlertaEmail> pesquisar(@Param("status") String status,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fim") LocalDateTime fim,
                                      Pageable pageable);

    List<EntregaAlertaEmail> findTop20ByStatusAndProximaTentativaEmLessThanEqualOrderByCriadaEmAsc(String status, LocalDateTime agora);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EntregaAlertaEmail e where e.id = :id")
    Optional<EntregaAlertaEmail> buscarParaAtualizacao(@Param("id") String id);


    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update EntregaAlertaEmail e set e.status = 'PROCESSANDO', e.reservadoAte = :prazo, e.ultimaTentativaEm = :agora, e.tentativas = e.tentativas + 1 where e.id = :id and e.status = 'PENDENTE' and e.proximaTentativaEm <= :agora")
    int reivindicarAtomico(@Param("id") String id, @Param("agora") LocalDateTime agora, @Param("prazo") LocalDateTime prazo);

    List<EntregaAlertaEmail> findTop100ByStatusAndReservadoAteLessThanEqualOrderByReservadoAteAsc(String status, LocalDateTime agora);

    boolean existsByIncidenteId(String incidenteId);

    long countByStatus(String status);
}
