package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Cobranca;
import com.marceloaleixo.treinoflow.entity.Notificacao;
import com.marceloaleixo.treinoflow.enums.StatusCobranca;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import com.marceloaleixo.treinoflow.repository.CobrancaRepository;
import com.marceloaleixo.treinoflow.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class NotificacaoService {
    private final NotificacaoRepository notificacoes;
    private final CobrancaRepository cobrancas;
    private final NotificacaoEntregaService entregas;
    private final TemplateNotificacaoService templates;

    public NotificacaoService(NotificacaoRepository notificacoes, CobrancaRepository cobrancas,
                               NotificacaoEntregaService entregas, TemplateNotificacaoService templates) {
        this.notificacoes = notificacoes;
        this.cobrancas = cobrancas;
        this.entregas = entregas;
        this.templates = templates;
    }

    @Transactional
    public int gerarAutomaticas() {
        int total = 0;
        LocalDate hoje = LocalDate.now();
        total += gerarPara(cobrancas.findByStatusOrderByDataVencimentoAsc(StatusCobranca.PENDENTE),
                hoje.plusDays(7), TipoNotificacao.VENCIMENTO_7_DIAS);
        total += gerarPara(cobrancas.findByStatusOrderByDataVencimentoAsc(StatusCobranca.PENDENTE),
                hoje, TipoNotificacao.VENCIMENTO_HOJE);
        List<Cobranca> atrasadas = cobrancas.findByStatusOrderByDataVencimentoAsc(StatusCobranca.ATRASADA);
        total += gerarAtrasadas(atrasadas);
        total += gerarRetencao(atrasadas, hoje);
        total += gerarAssinaturasVencendo(hoje);
        return total;
    }

    private int gerarPara(List<Cobranca> lista, LocalDate data, TipoNotificacao tipo) {
        int total = 0;
        for (Cobranca cobranca : lista) {
            if (!data.equals(cobranca.getDataVencimento())) continue;
            if (criarSeNaoExistir(cobranca, tipo, data.toString())) total++;
        }
        return total;
    }

    private int gerarAtrasadas(List<Cobranca> lista) {
        int total = 0;
        for (Cobranca cobranca : lista) {
            if (criarSeNaoExistir(cobranca, TipoNotificacao.COBRANCA_ATRASADA, cobranca.getDataVencimento().toString())) total++;
        }
        return total;
    }

    private int gerarRetencao(List<Cobranca> lista, LocalDate hoje) {
        int total = 0;
        for (Cobranca cobranca : lista) {
            if (cobranca.getDataVencimento() == null) continue;
            long dias = java.time.temporal.ChronoUnit.DAYS.between(cobranca.getDataVencimento(), hoje);
            if (dias == 3 && criarSeNaoExistir(cobranca, TipoNotificacao.INADIMPLENCIA_3_DIAS, cobranca.getDataVencimento() + ":3")) total++;
            if (dias == 7 && criarSeNaoExistir(cobranca, TipoNotificacao.INADIMPLENCIA_7_DIAS, cobranca.getDataVencimento() + ":7")) total++;
        }
        return total;
    }

    private int gerarAssinaturasVencendo(LocalDate hoje) {
        int total = 0;
        List<Cobranca> pendentes = cobrancas.findByStatusOrderByDataVencimentoAsc(StatusCobranca.PENDENTE);
        LocalDate alvo = hoje.plusDays(7);
        for (Cobranca cobranca : pendentes) {
            if (cobranca.getDataVencimento() != null && alvo.equals(cobranca.getDataVencimento())
                    && criarSeNaoExistir(cobranca, TipoNotificacao.ASSINATURA_VENCENDO, cobranca.getDataVencimento() + ":assinatura")) total++;
        }
        return total;
    }

    @Transactional
    public void registrarPagamentoConfirmado(Cobranca cobranca) {
        criarSeNaoExistir(cobranca, TipoNotificacao.PAGAMENTO_CONFIRMADO,
                cobranca.getDataPagamento() == null ? LocalDate.now().toString() : cobranca.getDataPagamento().toString());
    }

    private boolean criarSeNaoExistir(Cobranca cobranca, TipoNotificacao tipo, String referencia) {
        String chave = cobranca.getId() + ":" + tipo.name() + ":" + referencia;
        if (notificacoes.existsByChaveUnica(chave)) return false;

        Notificacao n = new Notificacao();
        n.setPersonal(cobranca.getAssinatura().getPersonal());
        n.setCobranca(cobranca);
        n.setTipo(tipo);
        n.setChaveUnica(chave);
        preencherTexto(n, cobranca);
        templates.aplicarInterna(n, variaveis(cobranca));
        notificacoes.save(n);
        entregas.criarEntregas(n);
        return true;
    }

    private void preencherTexto(Notificacao n, Cobranca c) {
        String valor = "R$ " + c.getValor().toPlainString().replace('.', ',');
        switch (n.getTipo()) {
            case VENCIMENTO_7_DIAS -> {
                n.setTitulo("Sua cobrança vence em 7 dias");
                n.setMensagem("A cobrança de " + valor + " do plano " + c.getAssinatura().getPlano().getNome() + " vence em " + c.getDataVencimento() + ".");
            }
            case VENCIMENTO_HOJE -> {
                n.setTitulo("Sua cobrança vence hoje");
                n.setMensagem("A cobrança de " + valor + " vence hoje. Acesse Cobranças para realizar o pagamento.");
            }
            case COBRANCA_ATRASADA -> {
                n.setTitulo("Existe uma cobrança em atraso");
                n.setMensagem("A cobrança de " + valor + " venceu em " + c.getDataVencimento() + ". Regularize sua assinatura para evitar bloqueios.");
            }
            case PAGAMENTO_CONFIRMADO -> {
                n.setTitulo("Pagamento confirmado");
                n.setMensagem("Recebemos o pagamento de " + valor + ". Sua assinatura foi atualizada.");
            }
            case ASSINATURA_VENCENDO -> {
                n.setTitulo("Sua assinatura está próxima do vencimento");
                n.setMensagem("Seu plano " + c.getAssinatura().getPlano().getNome() + " está próximo do vencimento.");
            }
            case ASSINATURA_BLOQUEADA -> {
                n.setTitulo("Sua assinatura foi bloqueada");
                n.setMensagem("Regularize sua assinatura para voltar a utilizar todos os recursos do TreinoFlow.");
            }
            case INADIMPLENCIA_3_DIAS -> {
                n.setTitulo("Sua cobrança está em atraso há 3 dias");
                n.setMensagem("A cobrança de " + valor + " venceu em " + c.getDataVencimento() + ". Regularize agora para evitar a suspensão da assinatura.");
            }
            case INADIMPLENCIA_7_DIAS -> {
                n.setTitulo("Último aviso: cobrança em atraso há 7 dias");
                n.setMensagem("A cobrança de " + valor + " está em atraso há 7 dias. Regularize sua assinatura para evitar a suspensão dos serviços.");
            }
        }
    }

    private Map<String, String> variaveis(Cobranca c) {
        return Map.of(
                "nome", valor(c.getAssinatura().getPersonal().getNome()),
                "plano", valor(c.getAssinatura().getPlano().getNome()),
                "valor", "R$ " + c.getValor().toPlainString().replace('.', ','),
                "vencimento", c.getDataVencimento() == null ? "" : c.getDataVencimento().toString(),
                "link_pagamento", valor(c.getPaymentUrl())
        );
    }

    private String valor(String valor) {
        return valor == null ? "" : valor;
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listar(Long personalId) {
        return notificacoes.findByPersonalIdOrderByDataCriacaoDesc(personalId);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> ultimas(Long personalId) {
        return notificacoes.findTop10ByPersonalIdOrderByDataCriacaoDesc(personalId);
    }

    public long contarNaoLidas(Long personalId) {
        return notificacoes.countByPersonalIdAndLidaFalse(personalId);
    }

    @Transactional
    public void marcarLida(Long personalId, Long id) {
        notificacoes.findById(id).ifPresent(n -> {
            if (n.getPersonal().getId().equals(personalId)) {
                n.marcarComoLida();
                notificacoes.save(n);
            }
        });
    }

    @Transactional
    public void marcarTodasLidas(Long personalId) {
        notificacoes.findByPersonalIdOrderByDataCriacaoDesc(personalId).stream()
                .filter(n -> !n.isLida())
                .forEach(n -> n.marcarComoLida());
    }
}
