package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.LembreteAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LembreteAgendamentoRepository extends JpaRepository<LembreteAgendamento, Long> {
    boolean existsByAgendamentoIdAndTipo(Long agendamentoId, String tipo);
}
