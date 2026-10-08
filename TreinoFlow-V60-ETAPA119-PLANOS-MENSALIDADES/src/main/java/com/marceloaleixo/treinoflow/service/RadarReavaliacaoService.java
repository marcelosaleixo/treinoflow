package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.RadarReavaliacaoView;
import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

/** Etapa 111: transforma o novo score em uma leitura simples pós-ação. */
@Service
public class RadarReavaliacaoService {

    public RadarReavaliacaoView montar(ScoreRiscoAlunoView antes,
                                       ScoreRiscoAlunoView depois,
                                       ResultadoCrm resultado,
                                       LocalDate proximaAcao,
                                       String observacao) {
        if (antes == null || depois == null) {
            throw new IllegalArgumentException("Não foi possível reavaliar o risco do aluno.");
        }

        int variacao = depois.score() - antes.score();
        String tendencia;
        if (variacao < 0) {
            tendencia = "Risco reduzido após a ação.";
        } else if (variacao > 0) {
            tendencia = "Risco aumentou após a reavaliação.";
        } else {
            tendencia = "Risco permanece estável após a ação.";
        }

        String resultadoDescricao = resultado == null ? "Não informado" : resultado.getDescricao();
        String proxima = proximaAcao == null
                ? "Nenhuma próxima ação definida."
                : proximaAcao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String obs = observacao == null || observacao.isBlank()
                ? "Nenhuma observação adicional registrada."
                : observacao.trim();

        return new RadarReavaliacaoView(
                depois.alunoId(),
                depois.nome(),
                antes.score(),
                antes.nivel(),
                prioridade(antes.score()),
                depois.score(),
                depois.nivel(),
                prioridade(depois.score()),
                variacao,
                tendencia,
                resultadoDescricao,
                proxima,
                obs,
                depois.acaoRecomendada()
        );
    }

    private String prioridade(int score) {
        if (score >= 75) return "ATENÇÃO";
        if (score >= 50) return "ALTA";
        if (score >= 25) return "MÉDIA";
        return "BAIXA";
    }
}
