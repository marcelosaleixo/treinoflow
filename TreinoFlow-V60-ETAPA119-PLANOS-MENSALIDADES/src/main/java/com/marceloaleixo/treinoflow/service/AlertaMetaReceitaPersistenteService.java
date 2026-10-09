package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.InteracaoRecebimentoVinculo;
import com.marceloaleixo.treinoflow.entity.MetaReceitaAcoes;
import com.marceloaleixo.treinoflow.entity.Notificacao;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import com.marceloaleixo.treinoflow.repository.InteracaoRecebimentoVinculoRepository;
import com.marceloaleixo.treinoflow.repository.MetaReceitaAcoesRepository;
import com.marceloaleixo.treinoflow.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Mantém alertas de metas de receita sincronizados com a situação financeira atual, preservando o histórico. */
@Service
public class AlertaMetaReceitaPersistenteService {
    private static final List<TipoNotificacao> TIPOS_META = List.of(
            TipoNotificacao.META_RECEITA_ATINGIDA,
            TipoNotificacao.META_RECEITA_ABAIXO_RITMO,
            TipoNotificacao.META_RECEITA_NAO_DEFINIDA,
            TipoNotificacao.META_RECEITA_MES_ENCERRADO);

    private final NotificacaoRepository notificacoes;
    private final MetaReceitaAcoesRepository metas;
    private final InteracaoRecebimentoVinculoRepository vinculos;

    public AlertaMetaReceitaPersistenteService(NotificacaoRepository notificacoes,
            MetaReceitaAcoesRepository metas, InteracaoRecebimentoVinculoRepository vinculos) {
        this.notificacoes = notificacoes;
        this.metas = metas;
        this.vinculos = vinculos;
    }

    @Transactional
    public void sincronizar(UsuarioPersonal personal, YearMonth periodo) {
        if (personal == null || personal.getId() == null || periodo == null) return;

        MetaReceitaAcoes meta = metas.findByPersonalIdAndMesReferencia(personal.getId(), periodo.atDay(1)).orElse(null);
        List<InteracaoRecebimentoVinculo> pagamentos = vinculos
                .findByInteracaoPersonalIdOrderByDataVinculoDesc(personal.getId()).stream()
                .filter(v -> v.getPagamento() != null && v.getPagamento().getDataPagamento() != null
                        && YearMonth.from(v.getPagamento().getDataPagamento()).equals(periodo))
                .toList();
        BigDecimal realizado = pagamentos.stream()
                .map(v -> v.getPagamento().getValor() == null ? BigDecimal.ZERO : v.getPagamento().getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal alvo = meta == null || meta.getValorMeta() == null ? BigDecimal.ZERO : meta.getValorMeta();
        YearMonth atual = YearMonth.now();
        Map<TipoNotificacao, TextoAlerta> desejados = new EnumMap<>(TipoNotificacao.class);
        boolean definida = alvo.signum() > 0;

        if (!definida && periodo.equals(atual)) {
            desejados.put(TipoNotificacao.META_RECEITA_NAO_DEFINIDA, new TextoAlerta(
                    "Defina sua meta de receita", "Ainda não existe uma meta de receita atribuída definida para "
                    + periodo + ". Configure a meta para acompanhar o progresso."));
        } else if (definida && realizado.compareTo(alvo) >= 0) {
            desejados.put(TipoNotificacao.META_RECEITA_ATINGIDA, new TextoAlerta(
                    "Meta de receita atingida", "A receita atribuída atual de R$ " + moeda(realizado)
                    + " alcançou a meta de R$ " + moeda(alvo) + " em " + periodo + "."));
        } else if (definida && periodo.isBefore(atual)) {
            desejados.put(TipoNotificacao.META_RECEITA_MES_ENCERRADO, new TextoAlerta(
                    "Meta mensal não atingida", "O período " + periodo + " terminou com R$ " + moeda(realizado)
                    + " atribuídos, de uma meta de R$ " + moeda(alvo) + "."));
        } else if (definida && periodo.equals(atual)) {
            int dia = LocalDate.now().getDayOfMonth();
            BigDecimal esperado = alvo.multiply(BigDecimal.valueOf(dia))
                    .divide(BigDecimal.valueOf(periodo.lengthOfMonth()), 2, RoundingMode.HALF_UP);
            if (realizado.compareTo(esperado) < 0) {
                desejados.put(TipoNotificacao.META_RECEITA_ABAIXO_RITMO, new TextoAlerta(
                        "Receita abaixo do ritmo esperado", "Até hoje, a referência proporcional é R$ "
                        + moeda(esperado) + ", mas foram atribuídos R$ " + moeda(realizado)
                        + ". Revise as ações de acompanhamento em aberto."));
            }
        }

        // Atualiza alertas existentes para que não continuem exibindo valores antigos quando os recebimentos mudam.
        for (TipoNotificacao tipo : TIPOS_META) {
            String chave = chave(personal.getId(), periodo, tipo);
            Notificacao existente = notificacoes.findByChaveUnica(chave).orElse(null);
            TextoAlerta textoAtual = desejados.get(tipo);
            if (textoAtual != null) {
                if (existente == null) {
                    existente = new Notificacao();
                    existente.setPersonal(personal);
                    existente.setTipo(tipo);
                    existente.setChaveUnica(chave);
                }
                existente.setTitulo(textoAtual.titulo());
                existente.setMensagem(limitar(textoAtual.mensagem()));
                notificacoes.save(existente);
            } else if (existente != null && !existente.getTitulo().startsWith("Condição atualizada")) {
                existente.setTitulo("Condição atualizada: " + tipo.getDescricao());
                existente.setMensagem("Este alerta foi atualizado em " + LocalDate.now()
                        + " porque os dados atuais de meta e receita já não correspondem à condição original. "
                        + "Consulte o painel de metas para ver a situação vigente.");
                notificacoes.save(existente);
            }
        }
    }

    private String chave(Long personalId, YearMonth periodo, TipoNotificacao tipo) {
        return "META_RECEITA:" + personalId + ":" + periodo + ":" + tipo.name();
    }

    private String limitar(String mensagem) {
        return mensagem.length() > 495 ? mensagem.substring(0, 492) + "..." : mensagem;
    }

    private String moeda(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private record TextoAlerta(String titulo, String mensagem) { }
}
