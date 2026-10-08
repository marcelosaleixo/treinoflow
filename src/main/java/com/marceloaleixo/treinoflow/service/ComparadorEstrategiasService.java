package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.ComparadorEstrategiaView;
import com.marceloaleixo.treinoflow.entity.AcaoAssistente;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import com.marceloaleixo.treinoflow.repository.AcaoAssistenteRepository;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Etapa 116: compara estratégias sem executar nenhuma ação automaticamente. */
@Service
public class ComparadorEstrategiasService {
    private static final int DIAS_HISTORICO = 180;

    private final AcaoAssistenteRepository acoes;
    private final AlunoRepository alunos;

    public ComparadorEstrategiasService(AcaoAssistenteRepository acoes, AlunoRepository alunos) {
        this.acoes = acoes;
        this.alunos = alunos;
    }

    @Transactional(readOnly = true)
    public List<ComparadorEstrategiaView> comparar(Long personalId, Long alunoId, int scoreRisco) {
        Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este Personal."));

        List<AcaoAssistente> historico = acoes.buscarDesde(personalId,
                LocalDateTime.now().minusDays(DIAS_HISTORICO));
        String objetivo = normalizar(aluno.getObjetivo());
        String idade = faixaIdade(aluno.getDataNascimento());
        int min = faixaMin(scoreRisco);
        int max = faixaMax(scoreRisco);

        GrupoContexto contexto = escolherContexto(historico, objetivo, idade, min, max);
        List<ComparadorEstrategiaView> resultado = new ArrayList<>();
        for (TipoAcaoAssistente tipo : TipoAcaoAssistente.values()) {
            resultado.add(montar(contexto.acoes(), tipo));
        }

        double melhorTaxa = resultado.stream()
                .filter(v -> v.resultadosFinais() > 0)
                .mapToDouble(ComparadorEstrategiaView::taxaSucesso)
                .max().orElse(-1);

        if (melhorTaxa >= 0) {
            resultado = resultado.stream().map(v -> {
                boolean melhor = v.resultadosFinais() > 0 && Math.abs(v.taxaSucesso() - melhorTaxa) < 0.0001;
                String leitura = leitura(v, melhor, contexto.base());
                return new ComparadorEstrategiaView(v.tipo(), v.totalAcoes(), v.resultadosFinais(),
                        v.positivos(), v.taxaSucesso(), v.forcaEvidencia(), melhor, leitura);
            }).toList();
        }

        return resultado;
    }

    private GrupoContexto escolherContexto(List<AcaoAssistente> historico, String objetivo, String idade,
                                           int min, int max) {
        List<AcaoAssistente> exato = filtrar(historico, objetivo, idade, min, max, true, true);
        if (temResultado(exato)) return new GrupoContexto(exato, "PERFIL_EXATO");

        List<AcaoAssistente> objetivoRisco = filtrar(historico, objetivo, null, min, max, true, false);
        if (temResultado(objetivoRisco)) return new GrupoContexto(objetivoRisco, "OBJETIVO_RISCO");

        List<AcaoAssistente> risco = filtrar(historico, null, null, min, max, false, false);
        if (temResultado(risco)) return new GrupoContexto(risco, "RISCO");

        return new GrupoContexto(filtrar(historico, null, null, 0, 100, false, false), "GERAL");
    }

    private List<AcaoAssistente> filtrar(List<AcaoAssistente> historico, String objetivo, String idade,
                                         int min, int max, boolean exigirObjetivo, boolean exigirIdade) {
        return historico.stream()
                .filter(a -> a.getScoreRisco() >= min && a.getScoreRisco() <= max)
                .filter(a -> !exigirObjetivo || normalizar(a.getAluno() == null ? null : a.getAluno().getObjetivo()).equals(objetivo))
                .filter(a -> !exigirIdade || faixaIdade(a.getAluno() == null ? null : a.getAluno().getDataNascimento()).equals(idade))
                .toList();
    }

    private boolean temResultado(List<AcaoAssistente> lista) {
        return lista.stream().anyMatch(this::resultadoFinal);
    }

    private ComparadorEstrategiaView montar(List<AcaoAssistente> lista, TipoAcaoAssistente tipo) {
        List<AcaoAssistente> grupo = lista.stream().filter(a -> a.getTipoAcao() == tipo).toList();
        long positivos = grupo.stream().filter(a -> a.getResultado() == ResultadoCrm.RECUPERADO
                || a.getResultado() == ResultadoCrm.RENOVADO).count();
        long finais = grupo.stream().filter(this::resultadoFinal).count();
        double taxa = finais == 0 ? 0 : positivos * 100.0 / finais;
        String forca = forcaEvidencia(finais);
        return new ComparadorEstrategiaView(tipo, grupo.size(), finais, positivos, taxa, forca, false,
                "Ainda não há resultados finais suficientes para comparar esta estratégia.");
    }

    private String leitura(ComparadorEstrategiaView v, boolean melhor, String base) {
        if (v.resultadosFinais() == 0) {
            return "Sem resultado final na base " + baseLabel(base) + ".";
        }
        if (v.resultadosFinais() < 3) {
            return "Há um sinal inicial, mas a amostra é pequena; não trate como padrão.";
        }
        if (melhor) {
            return "Maior taxa observada nesta comparação. A evidência depende da amostra e do contexto.";
        }
        return "Resultado abaixo da melhor taxa desta comparação, sem significar que a estratégia seja inadequada.";
    }

    private String forcaEvidencia(long finais) {
        if (finais >= 10) return "Forte";
        if (finais >= 5) return "Moderada";
        if (finais >= 3) return "Inicial";
        return "Insuficiente";
    }

    private boolean resultadoFinal(AcaoAssistente a) {
        ResultadoCrm r = a.getResultado();
        return r == ResultadoCrm.RECUPERADO || r == ResultadoCrm.RENOVADO
                || r == ResultadoCrm.CANCELAMENTO || r == ResultadoCrm.SEM_RESPOSTA
                || r == ResultadoCrm.OUTRO;
    }

    private int faixaMin(int score) {
        if (score >= 75) return 75;
        if (score >= 50) return 50;
        if (score >= 25) return 25;
        return 0;
    }

    private int faixaMax(int score) {
        if (score >= 75) return 100;
        if (score >= 50) return 74;
        if (score >= 25) return 49;
        return 24;
    }

    private String faixaIdade(LocalDate nascimento) {
        if (nascimento == null) return "SEM_IDADE";
        int idade = Period.between(nascimento, LocalDate.now()).getYears();
        if (idade < 25) return "ATE_24";
        if (idade < 35) return "25_34";
        if (idade < 45) return "35_44";
        if (idade < 60) return "45_59";
        return "60_MAIS";
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    private String baseLabel(String base) {
        return switch (base) {
            case "PERFIL_EXATO" -> "mesmo perfil + faixa de risco";
            case "OBJETIVO_RISCO" -> "mesmo objetivo + faixa de risco";
            case "RISCO" -> "mesma faixa de risco";
            default -> "histórico geral do Personal";
        };
    }

    private record GrupoContexto(List<AcaoAssistente> acoes, String base) {}
}
