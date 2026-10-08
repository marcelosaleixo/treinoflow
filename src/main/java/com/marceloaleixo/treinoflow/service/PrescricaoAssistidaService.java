package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AnaliseMultiSerieView;
import com.marceloaleixo.treinoflow.dto.AnaliseTendenciaProgressaoView;
import com.marceloaleixo.treinoflow.dto.PrescricaoAssistidaView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.TreinoExercicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Etapa 87 - IA de Prescrição Assistida.
 *
 * Converte as análises das etapas 83-85 em uma sugestão objetiva para a próxima
 * prescrição. Nunca persiste nem altera o treino automaticamente.
 */
@Service
public class PrescricaoAssistidaService {
    private static final Pattern NUMERO = Pattern.compile("(\\d+(?:[.,]\\d+)?)");

    private final TendenciaProgressaoService tendencia;
    private final AnaliseMultiSerieService multiSerie;

    public PrescricaoAssistidaService(TendenciaProgressaoService tendencia,
                                      AnaliseMultiSerieService multiSerie) {
        this.tendencia = tendencia;
        this.multiSerie = multiSerie;
    }

    @Transactional(readOnly = true)
    public PrescricaoAssistidaView sugerir(TreinoExercicio item, Aluno aluno) {
        AnaliseTendenciaProgressaoView t = tendencia.analisar(item, aluno);
        AnaliseMultiSerieView m = multiSerie.analisar(item, aluno);

        String series = item.getSeries() == null ? "Manter séries atuais" : String.valueOf(item.getSeries());
        String reps = item.getRepeticoes() == null || item.getRepeticoes().isBlank()
                ? "Manter faixa atual" : item.getRepeticoes();
        String cargaAtual = item.getCarga() == null || item.getCarga().isBlank() ? "não informada" : item.getCarga();

        if ("PROGRESSAO".equals(t.nivel()) && "ALTA".equals(t.confianca())
                && (m.rpeMedioUltimaSessao() == null || m.rpeMedioUltimaSessao() <= 8.5)) {
            String carga = incrementarCarga(cargaAtual);
            return new PrescricaoAssistidaView(
                    "PROGRESSAO", "ALTA", "Avaliar progressão na próxima prescrição",
                    "Há tendência positiva consistente, com esforço controlado. A sugestão é um pequeno incremento, sujeito à avaliação técnica do Personal.",
                    series, reps, carga,
                    "Revisar técnica e recuperação antes de aplicar. Se a execução estiver boa, considerar o incremento sugerido.");
        }

        if ("ATENCAO".equals(t.nivel()) || "ATENCAO".equals(m.nivel()) || "FADIGA".equals(m.nivel()) || "QUEDA".equals(m.nivel())) {
            return new PrescricaoAssistidaView(
                    "CAUTELA", t.confianca(), "Manter e reavaliar",
                    "Os dados mostram esforço elevado, fadiga ou queda de desempenho. Não há evidência segura para aumentar a carga agora.",
                    series, reps, cargaAtual,
                    "Manter a carga atual por enquanto e revisar recuperação, técnica e qualidade das últimas séries.");
        }

        if ("PROGRESSAO".equals(t.nivel()) && "MEDIA".equals(t.confianca())) {
            return new PrescricaoAssistidaView(
                    "AGUARDAR", "MEDIA", "Consolidar antes de progredir",
                    "Existe tendência positiva, mas ainda não há evidência suficiente para sugerir incremento de carga com alta confiança.",
                    series, reps, cargaAtual,
                    "Manter a prescrição e buscar mais uma ou duas sessões consistentes antes de progredir.");
        }

        if ("SEM_DADOS".equals(t.nivel()) || "AGUARDAR".equals(t.nivel())) {
            return new PrescricaoAssistidaView(
                    "SEM_DADOS", "BAIXA", "Coletar mais dados",
                    "O histórico ainda é insuficiente para uma recomendação de progressão responsável.",
                    series, reps, cargaAtual,
                    "Manter a prescrição atual e registrar carga, repetições e RPE em novas sessões.");
        }

        return new PrescricaoAssistidaView(
                "MANTER", "MEDIA", "Manter prescrição atual",
                "O histórico não apresenta evidência consistente para justificar uma alteração agora.",
                series, reps, cargaAtual,
                "Manter a prescrição e continuar acompanhando a tendência.");
    }

    private String incrementarCarga(String cargaAtual) {
        if (cargaAtual == null || cargaAtual.isBlank()) return "pequeno incremento (definir pelo Personal)";
        Matcher matcher = NUMERO.matcher(cargaAtual.replace(',', '.'));
        if (!matcher.find()) return "pequeno incremento (definir pelo Personal)";
        try {
            BigDecimal valor = new BigDecimal(matcher.group(1));
            // Incremento conservador de 2,5%, arredondado para uma casa decimal.
            BigDecimal novo = valor.multiply(new BigDecimal("1.025")).setScale(1, RoundingMode.HALF_UP);
            String original = matcher.group(1);
            String resultado = novo.stripTrailingZeros().toPlainString().replace('.', ',');
            if (original.contains(".")) resultado = resultado.replace(',', '.');
            int inicio = matcher.start(1), fim = matcher.end(1);
            return cargaAtual.substring(0, inicio) + resultado + cargaAtual.substring(fim);
        } catch (NumberFormatException ex) {
            return "pequeno incremento (definir pelo Personal)";
        }
    }
}
