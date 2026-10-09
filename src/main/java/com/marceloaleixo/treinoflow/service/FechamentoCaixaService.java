package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.PagamentoContaReceber;
import com.marceloaleixo.treinoflow.repository.PagamentoContaReceberRepository;
import com.marceloaleixo.treinoflow.repository.ConciliacaoFinanceiraRepository;
import com.marceloaleixo.treinoflow.repository.DespesaPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class FechamentoCaixaService {
    private final PagamentoContaReceberRepository pagamentos;
    private final ConciliacaoFinanceiraRepository conciliacoes;
    private final DespesaPersonalRepository despesas;

    public FechamentoCaixaService(PagamentoContaReceberRepository pagamentos,
            ConciliacaoFinanceiraRepository conciliacoes, DespesaPersonalRepository despesas) {
        this.pagamentos = pagamentos;
        this.conciliacoes = conciliacoes;
        this.despesas = despesas;
    }

    public ResumoFechamento montar(Long personalId, YearMonth periodo) {
        YearMonth mes = periodo == null ? YearMonth.now() : periodo;
        List<PagamentoContaReceber> lista = pagamentos.listarPeriodo(personalId, mes.atDay(1), mes.atEndOfMonth());
        Set<Long> ids = new HashSet<>();
        lista.forEach(p -> ids.add(p.getId()));
        Set<Long> conciliados = new HashSet<>();
        if (!ids.isEmpty()) conciliacoes.findByPersonalIdAndPagamentoIdIn(personalId, ids)
                .forEach(c -> conciliados.add(c.getPagamento().getId()));
        BigDecimal recebimentos = BigDecimal.ZERO, totalConciliado = BigDecimal.ZERO, totalPendenteConciliacao = BigDecimal.ZERO;
        for (PagamentoContaReceber p : lista) {
            BigDecimal valor = p.getValor() == null ? BigDecimal.ZERO : p.getValor();
            recebimentos = recebimentos.add(valor);
            if (conciliados.contains(p.getId())) totalConciliado = totalConciliado.add(valor);
            else totalPendenteConciliacao = totalPendenteConciliacao.add(valor);
        }
        BigDecimal despesasPagas = despesas.somarPagas(personalId, mes.atDay(1), mes.atEndOfMonth());
        BigDecimal despesasPendentes = despesas.somarPendentes(personalId, mes.atDay(1), mes.atEndOfMonth());
        return new ResumoFechamento(mes, lista.size(), conciliados.size(), lista.size() - conciliados.size(),
                recebimentos, totalConciliado, totalPendenteConciliacao, despesasPagas, despesasPendentes,
                recebimentos.subtract(despesasPagas), lista.size() == conciliados.size());
    }

    public record ResumoFechamento(YearMonth periodo, int quantidadePagamentos, int quantidadeConciliados,
            int quantidadePendentes, BigDecimal recebimentos, BigDecimal recebimentosConciliados,
            BigDecimal recebimentosPendentesConciliacao, BigDecimal despesasPagas, BigDecimal despesasPendentes,
            BigDecimal saldoOperacional, boolean todosPagamentosConciliados) {}
}
