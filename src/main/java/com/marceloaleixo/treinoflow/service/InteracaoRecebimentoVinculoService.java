package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.InteracaoRecebimentoVinculo;
import com.marceloaleixo.treinoflow.entity.PagamentoContaReceber;
import com.marceloaleixo.treinoflow.event.SincronizarMetaReceitaEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.time.YearMonth;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoRecebimentoVinculoRepository;
import com.marceloaleixo.treinoflow.repository.PagamentoContaReceberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteracaoRecebimentoVinculoService {
    private final InteracaoCrmRepository interacoes;
    private final PagamentoContaReceberRepository pagamentos;
    private final InteracaoRecebimentoVinculoRepository vinculos;
    private final ApplicationEventPublisher eventos;

    public InteracaoRecebimentoVinculoService(InteracaoCrmRepository interacoes, PagamentoContaReceberRepository pagamentos, InteracaoRecebimentoVinculoRepository vinculos, ApplicationEventPublisher eventos) {
        this.interacoes = interacoes; this.pagamentos = pagamentos; this.vinculos = vinculos; this.eventos = eventos;
    }

    @Transactional
    public void vincular(Long interacaoId, Long pagamentoId, Long personalId, String observacao) {
        InteracaoCrm interacao = interacoes.findByIdAndPersonalId(interacaoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Ação de CRM não encontrada para sua conta."));
        PagamentoContaReceber pagamento = pagamentos.buscarDoPersonal(pagamentoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Recebimento não encontrado para sua conta."));
        if (interacao.getAluno() == null || interacao.getAluno().getId() == null || pagamento.getContaReceber() == null || pagamento.getContaReceber().getAluno() == null || !interacao.getAluno().getId().equals(pagamento.getContaReceber().getAluno().getId())) {
            throw new IllegalArgumentException("A ação e o recebimento precisam pertencer ao mesmo aluno.");
        }
        if (vinculos.existsByInteracaoIdAndPagamentoId(interacaoId, pagamentoId)) {
            throw new IllegalArgumentException("Este recebimento já está vinculado a essa ação.");
        }
        if (vinculos.existsByPagamentoId(pagamentoId)) {
            throw new IllegalArgumentException("Este recebimento já foi atribuído a outra ação. Cada pagamento pode ser atribuído a apenas uma ação para evitar receita duplicada.");
        }
        InteracaoRecebimentoVinculo vinculo = new InteracaoRecebimentoVinculo();
        vinculo.setInteracao(interacao); vinculo.setPagamento(pagamento);
        vinculo.setObservacao(observacao == null ? null : observacao.trim().substring(0, Math.min(observacao.trim().length(), 300)));
        vinculos.save(vinculo);
        if (pagamento.getDataPagamento() != null) {
            eventos.publishEvent(new SincronizarMetaReceitaEvent(personalId, YearMonth.from(pagamento.getDataPagamento())));
        }
    }

    @Transactional
    public void excluir(Long vinculoId, Long personalId) {
        InteracaoRecebimentoVinculo vinculo = vinculos.findByIdAndInteracaoPersonalId(vinculoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Vínculo não encontrado para sua conta."));
        YearMonth periodo = vinculo.getPagamento() == null || vinculo.getPagamento().getDataPagamento() == null
                ? null : YearMonth.from(vinculo.getPagamento().getDataPagamento());
        vinculos.delete(vinculo);
        if (periodo != null) eventos.publishEvent(new SincronizarMetaReceitaEvent(personalId, periodo));
    }
}
