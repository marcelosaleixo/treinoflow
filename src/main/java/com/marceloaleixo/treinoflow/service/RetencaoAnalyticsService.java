package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AlunoResultadoView;
import com.marceloaleixo.treinoflow.dto.MotivoRiscoView;
import com.marceloaleixo.treinoflow.dto.PlanoAcaoAlunoView;
import com.marceloaleixo.treinoflow.dto.RetencaoAnalyticsView;
import com.marceloaleixo.treinoflow.dto.RetencaoMesView;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.service.PlanoAcaoAlunoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RetencaoAnalyticsService {
    private static final DateTimeFormatter MES_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    private final AlunoRepository alunos;
    private final InteracaoCrmRepository interacoes;
    private final PlanoAcaoAlunoService planoAcaoAlunoService;

    public RetencaoAnalyticsService(AlunoRepository alunos,
                                    InteracaoCrmRepository interacoes,
                                    PlanoAcaoAlunoService planoAcaoAlunoService) {
        this.alunos = alunos;
        this.interacoes = interacoes;
        this.planoAcaoAlunoService = planoAcaoAlunoService;
    }

    @Transactional(readOnly = true)
    public RetencaoAnalyticsView dashboard(Long personalId) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio30 = hoje.minusDays(29).atStartOfDay();
        LocalDateTime inicio6Meses = YearMonth.from(hoje).minusMonths(5).atDay(1).atStartOfDay();

        List<InteracaoCrm> historico = interacoes.findByPersonalIdOrderByDataContatoDesc(personalId);
        List<InteracaoCrm> ultimos6Meses = historico.stream()
                .filter(i -> i.getDataContato() != null && !i.getDataContato().isBefore(inicio6Meses))
                .toList();
        List<InteracaoCrm> ultimos30 = historico.stream()
                .filter(i -> i.getDataContato() != null && !i.getDataContato().isBefore(inicio30))
                .toList();

        long ativos = alunos.countByPersonalIdAndStatus(personalId, "ATIVO");
        List<PlanoAcaoAlunoView> riscos = planoAcaoAlunoService.listar(personalId);

        long recuperados = contar(ultimos30, ResultadoCrm.RECUPERADO);
        long renovados = contar(ultimos30, ResultadoCrm.RENOVADO);
        long cancelamentos = contar(ultimos30, ResultadoCrm.CANCELAMENTO);
        long contatos = ultimos30.size();
        long resultados = recuperados + renovados + cancelamentos;

        double taxaSucesso = resultados == 0 ? 0D : (recuperados + renovados) * 100D / resultados;
        double taxaCancelamento = resultados == 0 ? 0D : cancelamentos * 100D / resultados;

        return new RetencaoAnalyticsView(
                ativos,
                riscos.size(),
                contatos,
                recuperados,
                renovados,
                cancelamentos,
                taxaSucesso,
                taxaCancelamento,
                calcularTempoMedioRecuperacao(historico),
                montarMeses(ultimos6Meses, hoje),
                montarMotivosRisco(riscos),
                resultadosRecentes(ultimos30, ResultadoCrm.RECUPERADO),
                resultadosRecentes(ultimos30, ResultadoCrm.RENOVADO)
        );
    }

    private long contar(List<InteracaoCrm> itens, ResultadoCrm resultado) {
        return itens.stream().filter(i -> i.getResultado() == resultado).count();
    }

    private List<RetencaoMesView> montarMeses(List<InteracaoCrm> itens, LocalDate hoje) {
        List<RetencaoMesView> resultado = new ArrayList<>();
        YearMonth atual = YearMonth.from(hoje);
        for (int offset = 5; offset >= 0; offset--) {
            YearMonth mes = atual.minusMonths(offset);
            List<InteracaoCrm> doMes = itens.stream()
                    .filter(i -> i.getDataContato() != null && YearMonth.from(i.getDataContato()).equals(mes))
                    .toList();
            long recuperados = contar(doMes, ResultadoCrm.RECUPERADO);
            long renovados = contar(doMes, ResultadoCrm.RENOVADO);
            long cancelamentos = contar(doMes, ResultadoCrm.CANCELAMENTO);
            long resultados = recuperados + renovados + cancelamentos;
            double taxa = resultados == 0 ? 0D : (recuperados + renovados) * 100D / resultados;
            resultado.add(new RetencaoMesView(
                    mes.format(MES_FORMATTER),
                    doMes.size(),
                    recuperados,
                    renovados,
                    cancelamentos,
                    taxa
            ));
        }
        return resultado;
    }

    private List<MotivoRiscoView> montarMotivosRisco(List<PlanoAcaoAlunoView> riscos) {
        Map<String, Long> contagem = new HashMap<>();
        Map<String, String> descricoes = Map.of(
                "RETENCAO", "Sem treino nos últimos 30 dias",
                "FREQUENCIA", "Sem treino nesta semana",
                "ADESAO", "Baixa frequência em 30 dias",
                "SATISFACAO", "Avaliações abaixo de 3,5"
        );
        riscos.forEach(r -> contagem.merge(r.tipo(), 1L, Long::sum));
        return contagem.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new MotivoRiscoView(e.getKey(), descricoes.getOrDefault(e.getKey(), e.getKey()), e.getValue()))
                .toList();
    }

    private List<AlunoResultadoView> resultadosRecentes(List<InteracaoCrm> itens, ResultadoCrm resultado) {
        return itens.stream()
                .filter(i -> i.getResultado() == resultado && i.getAluno() != null)
                .sorted(Comparator.comparing(InteracaoCrm::getDataContato, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(i -> new AlunoResultadoView(i.getAluno().getId(), i.getAluno().getNome(), i.getAssunto(), i.getDataContato()))
                .toList();
    }

    private double calcularTempoMedioRecuperacao(List<InteracaoCrm> historico) {
        Map<Long, List<InteracaoCrm>> porAluno = new HashMap<>();
        for (InteracaoCrm i : historico) {
            if (i.getAluno() != null && i.getDataContato() != null) {
                porAluno.computeIfAbsent(i.getAluno().getId(), ignored -> new ArrayList<>()).add(i);
            }
        }

        List<Long> tempos = new ArrayList<>();
        for (List<InteracaoCrm> alunoInteracoes : porAluno.values()) {
            alunoInteracoes.sort(Comparator.comparing(InteracaoCrm::getDataContato));
            for (int i = 0; i < alunoInteracoes.size(); i++) {
                InteracaoCrm atual = alunoInteracoes.get(i);
                if (atual.getResultado() != ResultadoCrm.RECUPERADO) continue;
                InteracaoCrm primeiro = alunoInteracoes.get(0);
                long dias = ChronoUnit.DAYS.between(primeiro.getDataContato().toLocalDate(), atual.getDataContato().toLocalDate());
                tempos.add(Math.max(0, dias));
                break;
            }
        }
        return tempos.stream().mapToLong(Long::longValue).average().orElse(0D);
    }
}
