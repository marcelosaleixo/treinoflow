package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.PlanoAcaoAlunoView;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Gera follow-ups operacionais a partir dos alertas do Plano de Ação.
 * A automação não envia mensagens sozinha: apenas prepara o contato no CRM.
 */
@Service
public class AutomacaoRetencaoService {
    private final PlanoAcaoAlunoService planoAcao;
    private final InteracaoCrmRepository interacoes;
    private final com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository personais;
    private final AlunoRepository alunos;
    private final RetencaoFaltasService retencaoFaltas;

    public AutomacaoRetencaoService(PlanoAcaoAlunoService planoAcao,
                                    InteracaoCrmRepository interacoes,
                                    com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository personais,
                                    AlunoRepository alunos,
                                    RetencaoFaltasService retencaoFaltas) {
        this.planoAcao = planoAcao;
        this.interacoes = interacoes;
        this.personais = personais;
        this.alunos = alunos;
        this.retencaoFaltas = retencaoFaltas;
    }

    @Transactional
    public int gerarParaPersonal(Long personalId) {
        if (personalId == null) return 0;
        int gerados = 0;
        UsuarioPersonal personal = personais.findById(personalId)
                .orElseThrow(() -> new IllegalArgumentException("Personal não encontrado."));
        LocalDate hoje = LocalDate.now();
        LocalDateTime contatoDesde = LocalDateTime.now().minusDays(7);

        List<PlanoAcaoAlunoView> acoes = planoAcao.listar(personalId);
        for (PlanoAcaoAlunoView acao : acoes) {
            if (interacoes.existeFollowUpPendente(personalId, acao.alunoId(), ResultadoCrm.EM_ACOMPANHAMENTO)) {
                continue;
            }
            if (interacoes.existeContatoDesde(personalId, acao.alunoId(), contatoDesde)) {
                continue;
            }

            Aluno aluno = alunos.findByIdAndPersonalId(acao.alunoId(), personalId)
                    .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal."));

            InteracaoCrm interacao = new InteracaoCrm();
            interacao.setCanal(aluno.getTelefone() == null || aluno.getTelefone().isBlank()
                    ? CanalCrm.INTERNO : CanalCrm.WHATSAPP);
            interacao.setTipo(TipoInteracaoCrm.RETENCAO);
            interacao.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO);
            interacao.setAssunto("Ação automática · " + acao.tipo());
            interacao.setDescricao(descricaoAutomatica(acao));
            interacao.setDataContato(LocalDateTime.now());
            interacao.setDataProximaAcao(hoje);
            interacao.setPersonal(personal);
            interacao.setAluno(aluno);
            interacoes.save(interacao);
            gerados++;
        }
        return gerados;
    }

    @Scheduled(cron = "${treinoflow.retencao.automacao-cron:0 15 8 * * *}")
    public void executarRotinaDiaria() {
        personais.findByPerfilAndAtivoTrue("PERSONAL").forEach(personal -> {
            try {
                gerarParaPersonal(personal.getId());
                retencaoFaltas.gerarAcoes(personal.getId());
            } catch (RuntimeException ignored) {
                // Uma falha em um personal não deve interromper a rotina dos demais.
            }
        });
    }

    private String descricaoAutomatica(PlanoAcaoAlunoView acao) {
        return "Follow-up criado automaticamente pelo Plano de Ação.\n\n"
                + "Motivo: " + acao.motivo() + "\n"
                + "Ação recomendada: " + acao.acao() + "\n\n"
                + "Mensagem sugerida para WhatsApp:\n"
                + mensagemSugerida(acao);
    }

    public String mensagemSugerida(PlanoAcaoAlunoView acao) {
        String nome = acao.nome() == null ? "" : acao.nome().trim();
        return switch (acao.tipo()) {
            case "RETENCAO" -> "Olá, " + nome + "! Tudo bem? Percebi que você está há alguns dias sem treinar e queria saber se está tudo certo. Se aconteceu alguma dificuldade com horários, rotina ou treino, me chama que posso te ajudar a ajustar.";
            case "FREQUENCIA" -> "Olá, " + nome + "! Como você está? Notei que esta semana não conseguimos manter sua rotina de treinos. Aconteceu alguma coisa? Se precisar, podemos ajustar os horários ou o treino para facilitar sua semana.";
            case "ADESAO" -> "Olá, " + nome + "! Queria acompanhar melhor sua evolução. Percebi que sua frequência de treinos ficou abaixo do planejado. Vamos conversar para encontrar uma rotina que funcione melhor para você?";
            case "SATISFACAO" -> "Olá, " + nome + "! Queria saber como você está se sentindo com seus treinos. Sua opinião é importante para mim. Tem algo que você gostaria de mudar, melhorar ou ajustar no seu treino?";
            default -> "Olá, " + nome + "! Tudo bem? Estou passando para acompanhar sua evolução. Como posso te ajudar nesta semana?";
        };
    }
}
