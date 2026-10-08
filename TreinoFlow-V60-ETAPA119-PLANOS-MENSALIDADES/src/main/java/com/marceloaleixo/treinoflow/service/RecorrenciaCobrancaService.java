package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.ContaReceber;
import com.marceloaleixo.treinoflow.entity.PlanoMensalidade;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import com.marceloaleixo.treinoflow.repository.PlanoMensalidadeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Etapa 122 - gera automaticamente as cobranças das mensalidades ativas.
 *
 * A rotina é idempotente no nível da aplicação: antes de criar uma cobrança
 * verifica se já existe uma conta para o mesmo plano e vencimento.
 * Não realiza cobrança automática nem envia mensagens/pagamentos externos.
 */
@Service
public class RecorrenciaCobrancaService {
    private final PlanoMensalidadeRepository planos;
    private final ContaReceberRepository contas;

    public RecorrenciaCobrancaService(PlanoMensalidadeRepository planos,
                                      ContaReceberRepository contas) {
        this.planos = planos;
        this.contas = contas;
    }

    /** Executa diariamente pela manhã; o horário pode ser alterado por variável de ambiente. */
    @Scheduled(cron = "${treinoflow.financeiro.recorrencia-cron:0 0 7 * * *}")
    @Transactional
    public void processarAutomaticamente() {
        processar(LocalDate.now());
    }

    /** Processa somente os planos de um Personal, útil para ação manual/teste. */
    @Transactional
    public int processarParaPersonal(Long personalId, LocalDate hoje) {
        return processarPlanos(planos.findByAtivoTrueAndPersonalIdOrderByIdAsc(personalId),
                hoje == null ? LocalDate.now() : hoje);
    }

    private int processar(LocalDate hoje) {
        return processarPlanos(planos.findByAtivoTrueOrderByIdAsc(), hoje);
    }

    private int processarPlanos(List<PlanoMensalidade> lista, LocalDate hoje) {
        int criadas = 0;
        YearMonth mesAtual = YearMonth.from(hoje);

        for (PlanoMensalidade plano : lista) {
            if (plano.getDataInicio() == null || plano.getDataInicio().isAfter(hoje)) continue;

            YearMonth mesInicio = YearMonth.from(plano.getDataInicio());
            if (mesInicio.isAfter(mesAtual)) continue;

            LocalDate vencimento = mesAtual.atDay(Math.min(plano.getDiaVencimento(), mesAtual.lengthOfMonth()));

            // Se o plano começou depois do vencimento deste mês, a primeira cobrança será no próximo mês.
            if (vencimento.isBefore(plano.getDataInicio())) continue;

            // A cobrança do mês só nasce quando o vencimento chegou. Se o servidor ficou desligado,
            // ela ainda é criada no próximo processamento e permanece como atrasada.
            if (vencimento.isAfter(hoje)) continue;

            if (contas.existsByPlanoMensalidadeIdAndDataVencimento(plano.getId(), vencimento)) continue;

            ContaReceber conta = new ContaReceber();
            conta.setPersonal(plano.getPersonal());
            conta.setAluno(plano.getAluno());
            conta.setPlanoMensalidade(plano);
            conta.setDescricao(plano.getNome());
            conta.setValor(plano.getValor());
            conta.setDataVencimento(vencimento);
            conta.setStatus(vencimento.isBefore(hoje)
                    ? StatusContaReceber.ATRASADA
                    : StatusContaReceber.PENDENTE);
            contas.save(conta);
            criadas++;
        }
        return criadas;
    }
}
