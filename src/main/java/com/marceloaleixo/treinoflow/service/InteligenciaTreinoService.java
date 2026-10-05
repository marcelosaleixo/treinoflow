package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AlunoPerformanceView;
import com.marceloaleixo.treinoflow.dto.RecomendacaoPerformanceView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InteligenciaTreinoService {

    public List<RecomendacaoPerformanceView> gerar(
            List<AlunoPerformanceView> alunos,
            long sessoes7Dias,
            long sessoes30Dias,
            double mediaNotas,
            long alunosSemTreino30Dias) {

        List<RecomendacaoPerformanceView> recomendacoes = new ArrayList<>();

        long semSemana = alunos.stream().filter(a -> a.sessoes7Dias() == 0).count();
        long baixaFrequencia = alunos.stream()
                .filter(a -> a.sessoes30Dias() > 0 && a.sessoes30Dias() < 5)
                .count();
        long notasBaixas = alunos.stream()
                .filter(a -> a.mediaNota() > 0 && a.mediaNota() < 3.5)
                .count();

        if (alunosSemTreino30Dias > 0) {
            recomendacoes.add(new RecomendacaoPerformanceView(
                    "RETENCAO",
                    "Recupere alunos inativos",
                    alunosSemTreino30Dias + " aluno(s) não registrou(aram) treino nos últimos 30 dias.",
                    "Entre em contato pelo CRM e ofereça uma retomada do plano de treino.",
                    "ALTA"));
        }

        if (semSemana > 0) {
            recomendacoes.add(new RecomendacaoPerformanceView(
                    "FREQUENCIA",
                    "Acompanhe quem não treinou esta semana",
                    semSemana + " aluno(s) ativo(s) ainda não possui(em) sessão concluída nesta semana.",
                    "Envie uma mensagem de acompanhamento e confirme se existe alguma barreira para treinar.",
                    "ALTA"));
        }

        if (baixaFrequencia > 0) {
            recomendacoes.add(new RecomendacaoPerformanceView(
                    "ADESAO",
                    "Revise a frequência de treino",
                    baixaFrequencia + " aluno(s) treinou(aram) menos de 5 vezes nos últimos 30 dias.",
                    "Verifique agenda, disponibilidade e ajuste a rotina de treino se necessário.",
                    "MEDIA"));
        }

        if (notasBaixas > 0) {
            recomendacoes.add(new RecomendacaoPerformanceView(
                    "SATISFACAO",
                    "Investigue notas baixas",
                    notasBaixas + " aluno(s) apresenta(m) média de avaliação abaixo de 3,5.",
                    "Leia os feedbacks no histórico e avalie ajustes de volume, dificuldade ou exercícios.",
                    "MEDIA"));
        }

        if (sessoes7Dias == 0 && sessoes30Dias > 0) {
            recomendacoes.add(new RecomendacaoPerformanceView(
                    "CARTEIRA",
                    "Sua carteira está sem atividade nesta semana",
                    "Existem registros recentes no período de 30 dias, mas nenhuma sessão concluída nos últimos 7 dias.",
                    "Confira a agenda e faça um contato ativo com os alunos.",
                    "MEDIA"));
        }

        if (mediaNotas > 0 && mediaNotas >= 4.5) {
            recomendacoes.add(new RecomendacaoPerformanceView(
                    "OPORTUNIDADE",
                    "Use a satisfação como prova social",
                    "A média geral das avaliações está em " + String.format("%.1f", mediaNotas) + ".",
                    "Peça depoimentos aos alunos satisfeitos e use-os nas ações comerciais do Personal.",
                    "BAIXA"));
        }

        if (recomendacoes.isEmpty()) {
            recomendacoes.add(new RecomendacaoPerformanceView(
                    "MOMENTO",
                    "Carteira saudável",
                    "Não foram identificados alertas relevantes com os dados disponíveis.",
                    "Continue acompanhando frequência e avaliações semanalmente.",
                    "BAIXA"));
        }

        return recomendacoes;
    }
}
