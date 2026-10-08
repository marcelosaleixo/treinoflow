package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.AcaoAssistente;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import com.marceloaleixo.treinoflow.repository.AcaoAssistenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Etapa 80: aprendizado contínuo da retenção.
 *
 * O motor transforma o histórico real do personal em uma pontuação adaptativa
 * por modelo de mensagem. A seleção considera taxa de sucesso, amostra e
 * um pequeno bônus de exploração para não eternizar uma mensagem apenas porque
 * ela acumulou mais envios no passado.
 *
 * Não depende de IA externa e mantém o isolamento por personalId.
 */
@Service
public class AprendizadoRetencaoService {

    private static final int DIAS_HISTORICO = 180;
    private static final int MIN_AMOSTRA = 3;

    private final AcaoAssistenteRepository repository;

    public AprendizadoRetencaoService(AcaoAssistenteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ModeloVencedor> modelosElegiveis(Long personalId, int score, TipoAcaoAssistente tipo) {
        List<AcaoAssistente> historico = base(personalId, score, tipo);
        if (historico.isEmpty()) return List.of();
        Map<String, List<AcaoAssistente>> grupos = historico.stream()
                .collect(Collectors.groupingBy(a -> normalizar(a.getMensagem(), a.getAluno() == null ? null : a.getAluno().getNome())));
        int totalElegivel = grupos.values().stream().filter(g -> g.size() >= MIN_AMOSTRA).mapToInt(List::size).sum();
        return grupos.entrySet().stream()
                .filter(e -> e.getValue().size() >= MIN_AMOSTRA)
                .map(e -> modelo(e.getKey(), e.getValue(), totalElegivel))
                .sorted(Comparator.comparingDouble(ModeloVencedor::pontuacao).reversed()
                        .thenComparing(Comparator.comparingInt(ModeloVencedor::amostra).reversed()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ModeloVencedor melhorModelo(Long personalId, int score, TipoAcaoAssistente tipo) {
        List<AcaoAssistente> historico = base(personalId, score, tipo);
        if (historico.isEmpty()) return null;

        Map<String, List<AcaoAssistente>> grupos = historico.stream()
                .collect(Collectors.groupingBy(a -> normalizar(
                        a.getMensagem(),
                        a.getAluno() == null ? null : a.getAluno().getNome())));

        int totalElegivel = grupos.values().stream()
                .filter(g -> g.size() >= MIN_AMOSTRA)
                .mapToInt(List::size)
                .sum();

        return grupos.entrySet().stream()
                .filter(e -> e.getValue().size() >= MIN_AMOSTRA)
                .map(e -> modelo(e.getKey(), e.getValue(), totalElegivel))
                .max(Comparator.comparingDouble(ModeloVencedor::pontuacao)
                        .thenComparingInt(ModeloVencedor::amostra)
                        .thenComparing(ModeloVencedor::ultimoUso))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Dashboard dashboard(Long personalId) {
        List<AcaoAssistente> historico = repository.buscarDesde(
                personalId, LocalDateTime.now().minusDays(DIAS_HISTORICO));

        List<FaixaDashboard> faixas = List.of("CRÍTICO", "ALTO", "MÉDIO").stream()
                .map(faixa -> dashboardFaixa(historico, faixa))
                .toList();

        return new Dashboard("Últimos " + DIAS_HISTORICO + " dias", faixas);
    }

    private FaixaDashboard dashboardFaixa(List<AcaoAssistente> historico, String faixa) {
        List<AcaoAssistente> base = historico.stream()
                .filter(a -> faixa(a.getScoreRisco()).equals(faixa))
                .filter(a -> a.getTipoAcao() != null)
                .filter(a -> sucesso(a.getResultado()))
                .filter(a -> a.getMensagem() != null && !a.getMensagem().isBlank())
                .toList();

        // Para o painel, consolidamos por modelo independentemente do canal.
        Map<String, List<AcaoAssistente>> grupos = base.stream()
                .collect(Collectors.groupingBy(a -> normalizar(
                        a.getMensagem(),
                        a.getAluno() == null ? null : a.getAluno().getNome())));

        int total = grupos.values().stream()
                .filter(g -> g.size() >= MIN_AMOSTRA)
                .mapToInt(List::size)
                .sum();

        ModeloVencedor melhor = grupos.entrySet().stream()
                .filter(e -> e.getValue().size() >= MIN_AMOSTRA)
                .map(e -> modelo(e.getKey(), e.getValue(), total))
                .max(Comparator.comparingDouble(ModeloVencedor::pontuacao)
                        .thenComparingInt(ModeloVencedor::amostra)
                        .thenComparing(ModeloVencedor::ultimoUso))
                .orElse(null);

        if (melhor == null) {
            return new FaixaDashboard(faixa, "Sem amostra suficiente.", 0, 0, 0, "BAIXA");
        }

        return new FaixaDashboard(
                faixa,
                melhor.modelo(),
                melhor.amostra(),
                melhor.sucessos(),
                melhor.taxaSucesso(),
                melhor.confianca());
    }

    private ModeloVencedor modelo(String modelo, List<AcaoAssistente> grupo, int totalElegivel) {
        long sucessos = grupo.stream().filter(a -> sucesso(a.getResultado())).count();
        int n = grupo.size();

        // Suavização de Laplace evita 100% em amostras muito pequenas.
        double taxaSuavizada = (sucessos + 1D) / (n + 2D);

        // Bônus UCB1: favorece modelos com menos observações sem permitir
        // que uma amostra pequena vença indefinidamente uma estratégia forte.
        double exploracao = totalElegivel <= 0
                ? 0
                : Math.sqrt((2D * Math.log(totalElegivel + 1D)) / n);

        double pontuacao = taxaSuavizada * 100D + exploracao * 5D;
        String confianca = n >= 10 ? "ALTA" : "MÉDIA";
        LocalDateTime ultimoUso = grupo.stream()
                .map(AcaoAssistente::getExecutadaEm)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);

        return new ModeloVencedor(
                modelo, n, sucessos, sucessos * 100D / n,
                taxaSuavizada * 100D, exploracao, pontuacao, confianca, ultimoUso);
    }

    private List<AcaoAssistente> base(Long personalId, int score, TipoAcaoAssistente tipo) {
        String faixa = faixa(score);
        return repository.buscarDesde(personalId, LocalDateTime.now().minusDays(DIAS_HISTORICO)).stream()
                .filter(a -> a.getTipoAcao() == tipo)
                .filter(a -> faixa(a.getScoreRisco()).equals(faixa))
                .filter(a -> sucesso(a.getResultado()))
                .filter(a -> a.getMensagem() != null && !a.getMensagem().isBlank())
                .toList();
    }

    private String normalizar(String mensagem, String nomeAluno) {
        String texto = mensagem.trim().replaceAll("\\s+", " ");
        String primeiroNome = primeiroNome(nomeAluno);
        if (!primeiroNome.isBlank()) {
            texto = texto.replaceAll("(?i)\\b" + Pattern.quote(primeiroNome) + "\\b", "{nome}");
        }
        return texto;
    }

    private String primeiroNome(String nome) {
        if (nome == null || nome.isBlank()) return "";
        return nome.trim().split("\\s+")[0];
    }

    private boolean sucesso(ResultadoCrm resultado) {
        return resultado == ResultadoCrm.RECUPERADO || resultado == ResultadoCrm.RENOVADO;
    }

    private String faixa(int score) {
        if (score >= 75) return "CRÍTICO";
        if (score >= 50) return "ALTO";
        return "MÉDIO";
    }

    public record ModeloVencedor(
            String modelo,
            int amostra,
            long sucessos,
            double taxaSucesso,
            double taxaSuavizada,
            double exploracao,
            double pontuacao,
            String confianca,
            LocalDateTime ultimoUso) {}

    public record Dashboard(String periodoLabel, List<FaixaDashboard> faixas) {}

    public record FaixaDashboard(
            String faixa,
            String modelo,
            int amostra,
            long sucessos,
            double taxaSucesso,
            String confianca) {}
}
