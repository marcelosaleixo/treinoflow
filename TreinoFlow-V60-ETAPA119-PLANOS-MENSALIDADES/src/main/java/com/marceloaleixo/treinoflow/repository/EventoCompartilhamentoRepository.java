package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.EventoCompartilhamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoCompartilhamentoRepository extends JpaRepository<EventoCompartilhamento, Long> {
    List<EventoCompartilhamento> findByTreinoIdOrderByDataEventoDesc(Long treinoId);
    boolean existsByTreinoId(Long treinoId);
}
