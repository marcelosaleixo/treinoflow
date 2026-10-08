package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AcaoInteligenteAlunoView;
import com.marceloaleixo.treinoflow.dto.CarteiraAlunoView;
import com.marceloaleixo.treinoflow.dto.CarteiraDashboardView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class AcoesInteligentesService {
    private final SegmentacaoCarteiraService segmentacao;

    public AcoesInteligentesService(SegmentacaoCarteiraService segmentacao) {
        this.segmentacao = segmentacao;
    }

    @Transactional(readOnly = true)
    public List<AcaoInteligenteAlunoView> listar(Long personalId) {
        CarteiraDashboardView dashboard = segmentacao.dashboard(personalId);
        return dashboard.alunos().stream()
                .map(this::montar)
                .sorted(Comparator.comparingInt(this::prioridadeOrdem)
                        .thenComparing(Comparator.comparingInt(AcaoInteligenteAlunoView::risco).reversed())
                        .thenComparing(AcaoInteligenteAlunoView::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private AcaoInteligenteAlunoView montar(CarteiraAlunoView aluno) {
        return switch (aluno.segmento()) {
            case "CRITICO" -> new AcaoInteligenteAlunoView(
                    aluno.alunoId(), aluno.nome(), aluno.telefone(), aluno.segmento(), aluno.risco(), aluno.motivo(),
                    "Entrar em contato hoje", "URGENTE",
                    mensagem(aluno, "Percebi que sua rotina de treinos ficou mais distante nos últimos dias. Está tudo bem? Quero te ajudar a retomar sem pressão e ajustar o que for necessário."));
            case "RISCO" -> new AcaoInteligenteAlunoView(
                    aluno.alunoId(), aluno.nome(), aluno.telefone(), aluno.segmento(), aluno.risco(), aluno.motivo(),
                    "Fazer contato de recuperação", "ALTA",
                    mensagem(aluno, "Olá! Notei que sua frequência de treinos diminuiu. Aconteceu alguma coisa? Se precisar, posso ajustar seu treino ou horário para facilitar sua rotina."));
            case "ATENCAO" -> new AcaoInteligenteAlunoView(
                    aluno.alunoId(), aluno.nome(), aluno.telefone(), aluno.segmento(), aluno.risco(), aluno.motivo(),
                    "Acompanhar nesta semana", "MEDIA",
                    mensagem(aluno, "Olá! Passando para acompanhar sua rotina de treinos. Como você está? Se houver alguma dificuldade com treino, horário ou exercícios, me avise que ajustamos."));
            case "RECUPERADO" -> new AcaoInteligenteAlunoView(
                    aluno.alunoId(), aluno.nome(), aluno.telefone(), aluno.segmento(), aluno.risco(), aluno.motivo(),
                    "Fidelizar retorno", "MEDIA",
                    mensagem(aluno, "Que bom ter você de volta aos treinos! Vamos manter esse ritmo. Se precisar ajustar qualquer coisa para continuar evoluindo, estou à disposição."));
            default -> new AcaoInteligenteAlunoView(
                    aluno.alunoId(), aluno.nome(), aluno.telefone(), aluno.segmento(), aluno.risco(), aluno.motivo(),
                    "Manter acompanhamento", "BAIXA",
                    mensagem(aluno, "Olá! Tudo certo com seus treinos? Continue mantendo sua rotina. Se precisar de qualquer ajuste, pode falar comigo."));
        };
    }

    private String mensagem(CarteiraAlunoView aluno, String texto) {
        return "Olá, " + primeiroNome(aluno.nome()) + "! " + texto;
    }

    private String primeiroNome(String nome) {
        if (nome == null || nome.isBlank()) return "tudo bem";
        String valor = nome.trim();
        int espaco = valor.indexOf(' ');
        return espaco > 0 ? valor.substring(0, espaco) : valor;
    }

    private int prioridadeOrdem(AcaoInteligenteAlunoView acao) {
        return switch (acao.prioridade()) {
            case "URGENTE" -> 1;
            case "ALTA" -> 2;
            case "MEDIA" -> 3;
            default -> 4;
        };
    }
}
