package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.ConciliacaoFinanceira;
import com.marceloaleixo.treinoflow.entity.PagamentoContaReceber;
import com.marceloaleixo.treinoflow.repository.ConciliacaoFinanceiraRepository;
import com.marceloaleixo.treinoflow.repository.PagamentoContaReceberRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@Transactional
public class ConciliacaoFinanceiraService {
    private final PagamentoContaReceberRepository pagamentos;
    private final ConciliacaoFinanceiraRepository conciliacoes;
    private final UsuarioPersonalService personals;

    public ConciliacaoFinanceiraService(PagamentoContaReceberRepository pagamentos,
            ConciliacaoFinanceiraRepository conciliacoes, UsuarioPersonalService personals) {
        this.pagamentos = pagamentos; this.conciliacoes = conciliacoes; this.personals = personals;
    }

    @Transactional(readOnly = true)
    public List<LinhaConciliacao> listar(Long personalId, YearMonth mes) {
        YearMonth periodo = mes == null ? YearMonth.now() : mes;
        List<PagamentoContaReceber> lista = pagamentos.listarPeriodo(personalId, periodo.atDay(1), periodo.atEndOfMonth());
        if (lista.isEmpty()) return List.of();
        List<Long> ids = lista.stream().map(PagamentoContaReceber::getId).toList();
        Set<Long> conciliados = new HashSet<>();
        conciliacoes.findByPersonalIdAndPagamentoIdIn(personalId, ids)
                .forEach(c -> conciliados.add(c.getPagamento().getId()));
        return lista.stream().map(p -> new LinhaConciliacao(p, conciliados.contains(p.getId()))).toList();
    }

    public void conciliar(Long personalId, Long pagamentoId, String observacao) {
        PagamentoContaReceber pagamento = pagamentos.buscarDoPersonal(pagamentoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado para este Personal."));
        if (conciliacoes.findByPagamentoIdAndPersonalId(pagamentoId, personalId).isPresent())
            throw new IllegalArgumentException("Este pagamento já foi conciliado.");
        ConciliacaoFinanceira conciliacao = new ConciliacaoFinanceira();
        conciliacao.setPersonal(personals.buscarPorId(personalId));
        conciliacao.setPagamento(pagamento);
        conciliacao.setObservacao(observacao == null || observacao.isBlank() ? null : observacao.trim());
        conciliacoes.save(conciliacao);
    }

    public record LinhaConciliacao(PagamentoContaReceber pagamento, boolean conciliado) {}
}
