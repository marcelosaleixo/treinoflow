package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.RetencaoDashboardView;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RetencaoService {
    private final InteracaoCrmRepository interacoes;

    public RetencaoService(InteracaoCrmRepository interacoes) {
        this.interacoes = interacoes;
    }

    public RetencaoDashboardView dashboard(Long personalId) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(7);
        LocalDateTime inicio30 = LocalDateTime.now().minusDays(30);

        List<InteracaoCrm> vencidas = interacoes.buscarAcoesVencidas(
                personalId, hoje, ResultadoCrm.EM_ACOMPANHAMENTO);
        List<InteracaoCrm> agenda = interacoes.buscarAcoesAtePorResultado(
                personalId, limite, ResultadoCrm.EM_ACOMPANHAMENTO);

        List<InteracaoCrm> hojeLista = agenda.stream()
                .filter(i -> hoje.equals(i.getDataProximaAcao()))
                .toList();

        List<InteracaoCrm> proximos7 = agenda.stream()
                .filter(i -> i.getDataProximaAcao() != null
                        && i.getDataProximaAcao().isAfter(hoje)
                        && !i.getDataProximaAcao().isAfter(limite))
                .toList();

        List<InteracaoCrm> recuperados = interacoes.buscarResultadosDesde(
                personalId, ResultadoCrm.RECUPERADO, inicio30);
        List<InteracaoCrm> renovados = interacoes.buscarResultadosDesde(
                personalId, ResultadoCrm.RENOVADO, inicio30);
        List<InteracaoCrm> cancelamentos = interacoes.buscarResultadosDesde(
                personalId, ResultadoCrm.CANCELAMENTO, inicio30);

        return new RetencaoDashboardView(
                vencidas.size(),
                hojeLista.size(),
                proximos7.size(),
                recuperados.size(),
                renovados.size(),
                cancelamentos.size(),
                vencidas,
                hojeLista,
                proximos7,
                recuperados,
                renovados,
                cancelamentos
        );
    }

    @Transactional
    public void atualizarResultado(Long personalId, Long interacaoId, ResultadoCrm resultado, LocalDate proximaAcao) {
        if (resultado == null) {
            throw new IllegalArgumentException("Informe o resultado do contato.");
        }
        InteracaoCrm interacao = interacoes.findByIdAndPersonalId(interacaoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Interação não encontrada para este personal."));

        if (resultado == ResultadoCrm.EM_ACOMPANHAMENTO) {
            if (proximaAcao == null) {
                throw new IllegalArgumentException("Informe a data da próxima ação.");
            }
            interacao.setDataProximaAcao(proximaAcao);
        } else {
            interacao.setDataProximaAcao(null);
        }
        interacao.setResultado(resultado);
        interacoes.save(interacao);
    }

    public String linkWhatsApp(Long personalId, Long interacaoId) {
        InteracaoCrm interacao = interacoes.findByIdAndPersonalId(interacaoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Interação não encontrada para este personal."));
        String telefone = interacao.getAluno() == null ? "" : interacao.getAluno().getTelefone();
        String numero = telefone == null ? "" : telefone.replaceAll("[^0-9]", "");
        if (numero.isBlank()) throw new IllegalArgumentException("Este aluno não possui telefone cadastrado.");
        if (numero.length() == 11) numero = "55" + numero;
        String mensagem = mensagemSugerida(interacao);
        return "https://wa.me/" + numero + "?text=" + URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
    }

    private String mensagemSugerida(InteracaoCrm interacao) {
        String nome = interacao.getAluno() == null || interacao.getAluno().getNome() == null
                ? "" : interacao.getAluno().getNome().trim();
        String assunto = interacao.getAssunto() == null ? "" : interacao.getAssunto().toUpperCase();
        if (assunto.contains("RETENCAO")) {
            return "Olá, " + nome + "! Tudo bem? Percebi que você está há alguns dias sem treinar e queria saber se está tudo certo. Se aconteceu alguma dificuldade com horários, rotina ou treino, me chama que posso te ajudar a ajustar.";
        }
        if (assunto.contains("FREQUENCIA")) {
            return "Olá, " + nome + "! Como você está? Notei que esta semana não conseguimos manter sua rotina de treinos. Aconteceu alguma coisa? Se precisar, podemos ajustar os horários ou o treino para facilitar sua semana.";
        }
        if (assunto.contains("ADESAO")) {
            return "Olá, " + nome + "! Queria acompanhar melhor sua evolução. Percebi que sua frequência de treinos ficou abaixo do planejado. Vamos conversar para encontrar uma rotina que funcione melhor para você?";
        }
        if (assunto.contains("SATISFACAO")) {
            return "Olá, " + nome + "! Queria saber como você está se sentindo com seus treinos. Sua opinião é importante para mim. Tem algo que você gostaria de mudar, melhorar ou ajustar no seu treino?";
        }
        return "Olá, " + nome + "! Tudo bem? Estou passando para acompanhar sua evolução. Como posso te ajudar nesta semana?";
    }
}
