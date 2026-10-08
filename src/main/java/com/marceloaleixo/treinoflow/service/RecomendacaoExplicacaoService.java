package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.RecomendacaoAcaoPerfilView;
import com.marceloaleixo.treinoflow.dto.RecomendacaoExplicacaoView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;

/** Etapa 115: torna a recomendação auditável e compreensível para o Personal. */
@Service
public class RecomendacaoExplicacaoService {
    private static final int DIAS_HISTORICO = 180;

    private final AlunoRepository alunos;

    public RecomendacaoExplicacaoService(AlunoRepository alunos) {
        this.alunos = alunos;
    }

    @Transactional(readOnly = true)
    public RecomendacaoExplicacaoView explicar(Long personalId, Long alunoId, int scoreRisco,
                                               RecomendacaoAcaoPerfilView recomendacao) {
        Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este Personal."));

        String objetivo = aluno.getObjetivo() == null || aluno.getObjetivo().isBlank()
                ? "Não informado" : aluno.getObjetivo().trim();
        String idade = faixaIdade(aluno.getDataNascimento());
        String risco = faixaRisco(scoreRisco);
        String base = recomendacao == null ? "histórico geral do Personal" : recomendacao.getBaseLabel();
        String acao = recomendacao == null ? "Sem indicação" : recomendacao.acao();
        double taxa = recomendacao == null ? 0 : recomendacao.taxaSucesso();
        long finais = recomendacao == null ? 0 : recomendacao.resultadosFinais();
        long total = recomendacao == null ? 0 : recomendacao.amostraTotal();

        String forca = forcaEvidencia(finais);
        String resumo;
        if (recomendacao == null || finais == 0) {
            resumo = "Ainda não há resultados finais suficientes para sustentar uma recomendação específica.";
        } else {
            resumo = "A indicação foi escolhida porque " + acao + " ficou à frente entre as ações comparadas na base "
                    + base + ", com " + formatar(taxa) + " de recuperação/renovação.";
        }

        String comoPodeMudar = "A recomendação pode mudar quando novos resultados finais forem registrados. "
                + "Cada novo resultado altera a amostra e pode mudar a taxa relativa entre as ações. "
                + "O histórico considerado é limitado aos últimos " + DIAS_HISTORICO + " dias.";

        return new RecomendacaoExplicacaoView(
                objetivo, idade, risco, base, DIAS_HISTORICO, acao, taxa, finais, total,
                forca, resumo, comoPodeMudar
        );
    }

    private String faixaRisco(int score) {
        if (score >= 75) return "Crítico (75–100)";
        if (score >= 50) return "Alto (50–74)";
        if (score >= 25) return "Médio (25–49)";
        return "Baixo (0–24)";
    }

    private String faixaIdade(LocalDate nascimento) {
        if (nascimento == null) return "Não informada";
        int idade = Period.between(nascimento, LocalDate.now()).getYears();
        if (idade < 25) return "Até 24 anos";
        if (idade < 35) return "25–34 anos";
        if (idade < 45) return "35–44 anos";
        if (idade < 60) return "45–59 anos";
        return "60+ anos";
    }

    private String forcaEvidencia(long resultadosFinais) {
        if (resultadosFinais >= 10) return "Forte — 10+ resultados finais";
        if (resultadosFinais >= 5) return "Moderada — 5 a 9 resultados finais";
        if (resultadosFinais >= 3) return "Inicial — 3 a 4 resultados finais";
        if (resultadosFinais > 0) return "Insuficiente — menos de 3 resultados finais";
        return "Sem evidência final";
    }

    private String formatar(double valor) {
        return String.format(Locale.US, "%.1f%%", valor);
    }
}
