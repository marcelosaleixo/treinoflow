package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AprendizadoAcoesAssistenteView;
import com.marceloaleixo.treinoflow.entity.AcaoAssistente;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import com.marceloaleixo.treinoflow.repository.AcaoAssistenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AprendizadoAcoesAssistenteService {
    private final AcaoAssistenteRepository repository;

    public AprendizadoAcoesAssistenteService(AcaoAssistenteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AprendizadoAcoesAssistenteView dashboard(Long personalId, int dias) {
        int periodo = dias <= 0 ? 90 : Math.min(dias, 365);
        List<AcaoAssistente> acoes = repository.buscarDesde(personalId, LocalDateTime.now().minusDays(periodo));
        List<AprendizadoAcoesAssistenteView.DesempenhoAcaoView> desempenhos = Arrays.stream(TipoAcaoAssistente.values())
                .map(tipo -> desempenho(acoes, tipo)).toList();

        AprendizadoAcoesAssistenteView.DesempenhoAcaoView melhor = desempenhos.stream()
                .filter(d -> d.resultadosFinais() > 0)
                .max(Comparator.comparingDouble(AprendizadoAcoesAssistenteView.DesempenhoAcaoView::taxaSucesso)
                        .thenComparingLong(AprendizadoAcoesAssistenteView.DesempenhoAcaoView::resultadosFinais))
                .orElse(null);

        String melhorNome = melhor == null ? "Ainda sem dados" : melhor.tipo();
        double melhorTaxa = melhor == null ? 0 : melhor.taxaSucesso();
        String recomendacao = gerarRecomendacao(melhor, desempenhos, acoes);
        List<AprendizadoAcoesAssistenteView.InsightRiscoView> insights = List.of(
                insight(acoes, "CRÍTICO", 75, 100),
                insight(acoes, "ALTO", 50, 74),
                insight(acoes, "MÉDIO", 25, 49)
        );
        long finais = acoes.stream().filter(a -> resultadoFinal(a.getResultado())).count();
        long acoesRadar = acoes.stream().filter(a -> "RADAR_DIARIO".equals(a.getRegraAutomacao())).count();
        return new AprendizadoAcoesAssistenteView("Últimos " + periodo + " dias", acoes.size(), finais, acoesRadar,
                melhorNome, melhorTaxa, recomendacao, desempenhos, insights);
    }

    private AprendizadoAcoesAssistenteView.DesempenhoAcaoView desempenho(List<AcaoAssistente> acoes, TipoAcaoAssistente tipo) {
        List<AcaoAssistente> grupo = acoes.stream().filter(a -> a.getTipoAcao() == tipo).toList();
        long recuperados = contar(grupo, ResultadoCrm.RECUPERADO);
        long renovados = contar(grupo, ResultadoCrm.RENOVADO);
        long cancelamentos = contar(grupo, ResultadoCrm.CANCELAMENTO);
        long finais = grupo.stream().filter(a -> resultadoFinal(a.getResultado())).count();
        double taxa = finais == 0 ? 0 : (recuperados + renovados) * 100.0 / finais;
        return new AprendizadoAcoesAssistenteView.DesempenhoAcaoView(tipo.getDescricao(), grupo.size(), finais,
                recuperados, renovados, cancelamentos, taxa);
    }

    private AprendizadoAcoesAssistenteView.InsightRiscoView insight(List<AcaoAssistente> acoes, String faixa, int min, int max) {
        List<AcaoAssistente> grupo = acoes.stream().filter(a -> a.getScoreRisco() >= min && a.getScoreRisco() <= max).toList();
        List<AprendizadoAcoesAssistenteView.DesempenhoAcaoView> dados = Arrays.stream(TipoAcaoAssistente.values())
                .map(tipo -> desempenho(grupo, tipo)).filter(d -> d.resultadosFinais() > 0).toList();
        AprendizadoAcoesAssistenteView.DesempenhoAcaoView melhor = dados.stream()
                .max(Comparator.comparingDouble(AprendizadoAcoesAssistenteView.DesempenhoAcaoView::taxaSucesso)
                        .thenComparingLong(AprendizadoAcoesAssistenteView.DesempenhoAcaoView::resultadosFinais)).orElse(null);
        String nome = melhor == null ? "Sem evidência suficiente" : melhor.tipo();
        double taxa = melhor == null ? 0 : melhor.taxaSucesso();
        String leitura = melhor == null ? "Registre mais resultados finais para gerar uma recomendação baseada no seu histórico." :
                "Nesta faixa de risco, " + nome + " apresentou a melhor taxa de recuperação/renovação observada.";
        return new AprendizadoAcoesAssistenteView.InsightRiscoView(faixa, grupo.size(), nome, taxa, leitura);
    }

    private String gerarRecomendacao(AprendizadoAcoesAssistenteView.DesempenhoAcaoView melhor,
                                     List<AprendizadoAcoesAssistenteView.DesempenhoAcaoView> dados,
                                     List<AcaoAssistente> acoes) {
        if (acoes.isEmpty()) return "Ainda não há ações executadas. Use o Assistente para começar a construir seu histórico.";
        if (melhor == null) return "Existem ações registradas, mas ainda não há resultados finais suficientes para comparar efetividade.";
        if (melhor.resultadosFinais() < 3) return "A melhor ação atual é " + melhor.tipo() + ", mas a amostra ainda é pequena. Registre mais resultados antes de mudar sua estratégia.";
        return "Para o seu histórico, priorize " + melhor.tipo() + " nos próximos casos semelhantes. A ação apresenta " + melhor.getTaxaSucessoLabel() + " de sucesso entre os resultados finais registrados.";
    }

    private boolean resultadoFinal(ResultadoCrm r) { return r == ResultadoCrm.RECUPERADO || r == ResultadoCrm.RENOVADO || r == ResultadoCrm.CANCELAMENTO || r == ResultadoCrm.SEM_RESPOSTA || r == ResultadoCrm.OUTRO; }
    private long contar(List<AcaoAssistente> acoes, ResultadoCrm resultado) { return acoes.stream().filter(a -> a.getResultado() == resultado).count(); }
}
