package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AcaoRetencaoView;
import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RetencaoInteligenteService {
    private final ScoreRiscoAlunoService scoreService;
    private final AlunoRepository alunos;
    private final InteracaoCrmRepository interacoes;
    private final UsuarioPersonalService usuarioPersonalService;
    private final WhatsAppNotificacaoService whatsapp;
    private final CrmService crm;
    private final ResultadoAcoesAssistenteService resultados;

    public RetencaoInteligenteService(ScoreRiscoAlunoService scoreService,
                                      AlunoRepository alunos,
                                      InteracaoCrmRepository interacoes,
                                      UsuarioPersonalService usuarioPersonalService,
                                      WhatsAppNotificacaoService whatsapp,
                                      CrmService crm,
                                      ResultadoAcoesAssistenteService resultados) {
        this.scoreService = scoreService;
        this.alunos = alunos;
        this.interacoes = interacoes;
        this.usuarioPersonalService = usuarioPersonalService;
        this.whatsapp = whatsapp;
        this.crm = crm;
        this.resultados = resultados;
    }

    @Transactional(readOnly = true)
    public List<AcaoRetencaoView> listar(Long personalId) {
        return scoreService.listar(personalId).stream()
                .filter(r -> r.score() >= 25)
                .map(r -> new AcaoRetencaoView(r, gerarMensagem(r), r.telefone() != null && !r.telefone().isBlank()))
                .toList();
    }

    @Transactional
    public void enviarWhatsApp(Long personalId, Long alunoId) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorId(personalId);
        Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal."));

        ScoreRiscoAlunoView risco = scoreService.listar(personalId).stream()
                .filter(r -> r.alunoId().equals(alunoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível calcular o risco deste aluno."));

        if (risco.score() < 25) {
            throw new IllegalArgumentException("Este aluno não está classificado em risco de retenção.");
        }
        if (aluno.getTelefone() == null || aluno.getTelefone().isBlank()) {
            throw new IllegalArgumentException("O aluno não possui telefone cadastrado.");
        }

        String mensagem = gerarMensagem(risco);
        whatsapp.enviar(aluno.getTelefone(), mensagem);

        InteracaoCrm interacao = new InteracaoCrm();
        interacao.setPersonal(personal);
        interacao.setAluno(aluno);
        interacao.setCanal(CanalCrm.WHATSAPP);
        interacao.setTipo(TipoInteracaoCrm.RETENCAO);
        interacao.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO);
        interacao.setAssunto("Ação de retenção por risco de abandono");
        interacao.setDescricao("Mensagem de retenção enviada pelo TreinoFlow. Score no momento do envio: " + risco.score() + "/100. Mensagem: " + mensagem);
        interacao.setDataProximaAcao(LocalDate.now().plusDays(2));
        interacoes.save(interacao);
        resultados.registrarAcaoExecutada(personalId, alunoId, risco.score(),
                "Mensagem de retenção enviada pelo Assistente", mensagem,
                com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente.WHATSAPP);
    }

    @Transactional
    public void criarFollowUp(Long personalId, Long alunoId) {
        Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal."));
        ScoreRiscoAlunoView risco = scoreService.listar(personalId).stream()
                .filter(r -> r.alunoId().equals(alunoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível calcular o risco deste aluno."));

        if (interacoes.existeFollowUpPendente(personalId, alunoId, ResultadoCrm.EM_ACOMPANHAMENTO)) {
            throw new IllegalArgumentException("Já existe um follow-up pendente para este aluno.");
        }

        InteracaoCrm interacao = new InteracaoCrm();
        interacao.setPersonal(usuarioPersonalService.buscarPorId(personalId));
        interacao.setAluno(aluno);
        interacao.setCanal(CanalCrm.INTERNO);
        interacao.setTipo(TipoInteracaoCrm.RETENCAO);
        interacao.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO);
        interacao.setAssunto("Follow-up de retenção · risco " + risco.score() + "/100");
        interacao.setDescricao("Aluno identificado pelo Motor de Risco. Ação recomendada: " + risco.acaoRecomendada());
        interacao.setDataProximaAcao(LocalDate.now());
        crm.salvar(personalId, alunoId, interacao);
        resultados.registrarAcaoExecutada(personalId, alunoId, risco.score(),
                "Follow-up de retenção criado pelo Assistente",
                null, com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente.FOLLOW_UP);
    }

    public String gerarMensagem(ScoreRiscoAlunoView risco) {
        String nome = primeiroNome(risco.nome());
        String motivo = risco.nivel().equals("CRÍTICO") || risco.nivel().equals("ALTO")
                ? "Percebi que sua frequência nos treinos mudou um pouco e queria saber se está tudo bem."
                : "Queria acompanhar como você está se sentindo com os treinos e sua rotina.";
        String acao = risco.acaoRecomendada();
        return "Olá, " + nome + "! 👋\n\n" + motivo + "\n\n" + acao + "\n\n" +
                "Se precisar, podemos ajustar o treino ou o horário para ficar mais fácil manter sua rotina. 💪\n\n" +
                "Me responde por aqui e vamos resolver juntos!";
    }

    private String primeiroNome(String nome) {
        if (nome == null || nome.isBlank()) return "tudo bem";
        return nome.trim().split("\\s+")[0];
    }
}
