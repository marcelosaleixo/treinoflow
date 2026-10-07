package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Etapa 79 + 80: mensagem inteligente apoiada pelo aprendizado contínuo. */
@Service
public class MensagemInteligenteService {

    private final AprendizadoRetencaoService aprendizado;
    private final ExperimentoRetencaoService experimentos;

    public MensagemInteligenteService(AprendizadoRetencaoService aprendizado, ExperimentoRetencaoService experimentos) {
        this.aprendizado = aprendizado;
        this.experimentos = experimentos;
    }

    @Transactional
    public Escolha escolher(Long personalId, ScoreRiscoAlunoView risco, TipoAcaoAssistente tipo) {
        var experimento = experimentos.escolher(personalId, risco.alunoId(), risco.score(), tipo);
        if (experimento.experimentoId() != null) {
            String modelo = experimentos.mensagem(experimento.experimentoId(), experimento.variante());
            if (modelo != null) {
                var modelos = aprendizado.modelosElegiveis(personalId, risco.score(), tipo);
                int indice = "A".equals(experimento.variante()) ? 0 : 1;
                var estatistica = modelos.size() > indice ? modelos.get(indice) : null;
                return new Escolha(personalizar(modelo, risco),
                        estatistica == null ? "MÉDIA" : estatistica.confianca(),
                        estatistica == null ? 0 : estatistica.amostra(),
                        "Etapa 81 · experimento A/B #" + experimento.experimentoId() + " · variante " + experimento.variante() + ".",
                        experimento.experimentoId(), experimento.variante());
            }
        }
        AprendizadoRetencaoService.ModeloVencedor modelo =
                aprendizado.melhorModelo(personalId, risco.score(), tipo);

        if (modelo != null) {
            return new Escolha(
                    personalizar(modelo.modelo(), risco),
                    modelo.confianca(),
                    modelo.amostra(),
                    "Etapa 80 · modelo adaptativo. Taxa de sucesso observada: "
                            + String.format(java.util.Locale.US, "%.1f", modelo.taxaSucesso())
                            + "%. Pontuação adaptativa: "
                            + String.format(java.util.Locale.US, "%.2f", modelo.pontuacao()) + ".", null, null);
        }

        return new Escolha(
                fallback(risco),
                "BAIXA",
                0,
                "Etapa 80 · sem amostra mínima de 3 resultados vencedores; fallback seguro.", null, null);
    }

    @Transactional(readOnly = true)
    public ResumoDashboard resumo(Long personalId) {
        var dashboard = aprendizado.dashboard(personalId);
        var faixas = dashboard.faixas().stream()
                .map(f -> new ResumoFaixa(
                        f.faixa(), f.modelo(), f.amostra(), f.confianca()))
                .toList();
        return new ResumoDashboard(dashboard.periodoLabel(), faixas);
    }

    private String personalizar(String modelo, ScoreRiscoAlunoView risco) {
        String nome = primeiroNome(risco.nome());
        if (nome.isBlank()) nome = "tudo bem";
        return modelo.replace("{nome}", nome);
    }

    private String primeiroNome(String nome) {
        if (nome == null || nome.isBlank()) return "";
        return nome.trim().split("\\s+")[0];
    }

    private String fallback(ScoreRiscoAlunoView risco) {
        String nome = primeiroNome(risco.nome());
        if (nome.isBlank()) nome = "tudo bem";

        String texto = switch (faixa(risco.score())) {
            case "CRÍTICO" -> "Percebi alguns sinais de que sua rotina de treinos mudou e queria saber se está tudo bem.\n\nSe precisar, podemos ajustar o treino ou o horário para facilitar sua rotina. 💪\n\nMe responde por aqui e vamos resolver juntos!";
            case "ALTO" -> "Queria saber como está sua rotina de treinos. Se algo estiver dificultando sua frequência, podemos ajustar seu treino e encontrar uma forma mais fácil de manter o ritmo. 💪";
            default -> "Passando para acompanhar sua evolução. Se precisar de algum ajuste no treino ou na rotina, me chama e vamos adaptar juntos. 🤝";
        };
        return "Olá, " + nome + "! 👋\n\n" + texto;
    }

    private String faixa(int score) {
        if (score >= 75) return "CRÍTICO";
        if (score >= 50) return "ALTO";
        return "MÉDIO";
    }

    public record Escolha(String mensagem, String confianca, int amostra, String justificativa, Long experimentoId, String variante) {}
    public record ResumoDashboard(String periodoLabel, java.util.List<ResumoFaixa> faixas) {}
    public record ResumoFaixa(String faixa, String mensagem, int amostra, String confianca) {}
}
