package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.PrevisaoFinanceiraMesView;
import com.marceloaleixo.treinoflow.entity.ContaReceber;
import com.marceloaleixo.treinoflow.entity.DespesaPersonal;
import com.marceloaleixo.treinoflow.entity.PlanoMensalidade;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import com.marceloaleixo.treinoflow.repository.DespesaPersonalRepository;
import com.marceloaleixo.treinoflow.repository.PlanoMensalidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrevisaoFinanceiraService {
    private final ContaReceberRepository contas;
    private final DespesaPersonalRepository despesas;
    private final PlanoMensalidadeRepository planos;

    public PrevisaoFinanceiraService(ContaReceberRepository contas, DespesaPersonalRepository despesas, PlanoMensalidadeRepository planos) {
        this.contas = contas; this.despesas = despesas; this.planos = planos;
    }

    @Transactional(readOnly = true)
    public List<PrevisaoFinanceiraMesView> projetar(Long personalId, int quantidadeMeses) {
        int horizonte = Math.max(1, Math.min(12, quantidadeMeses));
        List<ContaReceber> todasContas = contas.findByPersonalIdOrderByDataVencimentoAsc(personalId);
        List<PlanoMensalidade> planosAtivos = planos.findByAtivoTrueAndPersonalIdOrderByIdAsc(personalId);
        List<PrevisaoFinanceiraMesView> resultado = new ArrayList<>();
        YearMonth atual = YearMonth.now();
        for (int i = 0; i < horizonte; i++) {
            YearMonth mes = atual.plusMonths(i);
            LocalDate inicio = mes.atDay(1), fim = mes.atEndOfMonth();
            BigDecimal receita = BigDecimal.ZERO;
            for (ContaReceber c : todasContas) {
                if (c.getDataVencimento() == null || c.getDataVencimento().isBefore(inicio) || c.getDataVencimento().isAfter(fim)) continue;
                if (c.getStatus() == StatusContaReceber.CANCELADA || c.getStatus() == StatusContaReceber.PAGA) continue;
                receita = receita.add(c.getValor() == null ? BigDecimal.ZERO : c.getValor());
            }
            // Acrescenta mensalidades ativas futuras somente quando ainda não existe conta para aquele plano/vencimento.
            for (PlanoMensalidade plano : planosAtivos) {
                if (plano.getDataInicio() == null || plano.getDataInicio().isAfter(fim)) continue;
                int dia = Math.max(1, Math.min(plano.getDiaVencimento() == null ? 1 : plano.getDiaVencimento(), mes.lengthOfMonth()));
                LocalDate vencimento = mes.atDay(dia);
                if (vencimento.isBefore(plano.getDataInicio())) continue;
                boolean jaExisteConta = todasContas.stream().anyMatch(c -> c.getPlanoMensalidade() != null
                        && c.getPlanoMensalidade().getId().equals(plano.getId())
                        && vencimento.equals(c.getDataVencimento())
                        && c.getStatus() != StatusContaReceber.CANCELADA);
                if (!jaExisteConta) receita = receita.add(plano.getValor() == null ? BigDecimal.ZERO : plano.getValor());
            }
            BigDecimal despesasCadastradas = despesas.somarPendentes(personalId, inicio, fim);
            BigDecimal saldo = receita.subtract(despesasCadastradas);
            String observacao = i == 0 ? "Contas pendentes e mensalidades ativas, menos despesas pendentes cadastradas."
                    : "Estimativa de mensalidades ativas e contas pendentes; considera apenas despesas pendentes já cadastradas.";
            resultado.add(new PrevisaoFinanceiraMesView(mes, receita, despesasCadastradas, saldo, observacao));
        }
        return resultado;
    }
}
