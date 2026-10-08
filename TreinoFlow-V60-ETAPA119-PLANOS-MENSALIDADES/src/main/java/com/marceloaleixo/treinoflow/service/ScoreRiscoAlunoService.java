package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.entity.Agendamento;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.ExecucaoExercicio;
import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import com.marceloaleixo.treinoflow.repository.AgendamentoRepository;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.ExecucaoExercicioRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calcula um score operacional de risco de abandono de 0 a 100.
 * A regra é explicável: cada ponto do score nasce de um sinal observável.
 */
@Service
public class ScoreRiscoAlunoService {
    private static final Pattern NUMERO = Pattern.compile("(\\d+(?:[.,]\\d+)?)");

    private final AlunoRepository alunos;
    private final AgendamentoRepository agendamentos;
    private final RegistroTreinoAlunoRepository registros;
    private final ExecucaoExercicioRepository execucoes;
    private final InteracaoCrmRepository interacoes;

    public ScoreRiscoAlunoService(AlunoRepository alunos,
                                  AgendamentoRepository agendamentos,
                                  RegistroTreinoAlunoRepository registros,
                                  ExecucaoExercicioRepository execucoes,
                                  InteracaoCrmRepository interacoes) {
        this.alunos = alunos;
        this.agendamentos = agendamentos;
        this.registros = registros;
        this.execucoes = execucoes;
        this.interacoes = interacoes;
    }

    @Transactional(readOnly = true)
    public List<ScoreRiscoAlunoView> listar(Long personalId) {
        if (personalId == null || personalId <= 0) throw new IllegalArgumentException("Personal inválido.");

        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(29);
        LocalDateTime agora = LocalDateTime.now();

        List<Aluno> carteira = alunos.findByPersonalIdAndStatusOrderByNomeAsc(personalId, "ATIVO");
        List<Agendamento> agenda = agendamentos.buscarDoPeriodoComAluno(personalId, inicio.atStartOfDay(), agora.plusNanos(1));
        List<RegistroTreinoAluno> treinos = registros.buscarConcluidosDoPersonalDesde(personalId, inicio);
        List<ExecucaoExercicio> execucoes30 = execucoes.buscarDoPersonalDesde(personalId, inicio).stream()
                .filter(ExecucaoExercicio::isConcluido)
                .toList();

        Map<Long, List<Agendamento>> agendaPorAluno = agruparAgenda(agenda);
        Map<Long, List<RegistroTreinoAluno>> treinosPorAluno = agruparTreinos(treinos);
        Map<Long, List<ExecucaoExercicio>> execucoesPorAluno = agruparExecucoes(execucoes30);

        return carteira.stream()
                .map(aluno -> calcular(aluno,
                        agendaPorAluno.getOrDefault(aluno.getId(), List.of()),
                        treinosPorAluno.getOrDefault(aluno.getId(), List.of()),
                        execucoesPorAluno.getOrDefault(aluno.getId(), List.of()),
                        hoje))
                .sorted(Comparator.comparingInt(ScoreRiscoAlunoView::score).reversed()
                        .thenComparing(ScoreRiscoAlunoView::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public ScoreRiscoAlunoView buscar(Long personalId, Long alunoId) {
        if (alunoId == null) throw new IllegalArgumentException("Aluno inválido.");
        return listar(personalId).stream()
                .filter(item -> alunoId.equals(item.alunoId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado no radar de risco."));
    }

    private ScoreRiscoAlunoView calcular(Aluno aluno,
                                         List<Agendamento> agenda,
                                         List<RegistroTreinoAluno> treinos,
                                         List<ExecucaoExercicio> execucoes,
                                         LocalDate hoje) {
        long faltas = agenda.stream().filter(a -> "FALTOU".equals(a.getStatus())).count();
        long realizados = agenda.stream().filter(a -> "REALIZADO".equals(a.getStatus())).count();
        long basePresenca = faltas + realizados;
        double taxa = basePresenca == 0 ? 0D : realizados * 100D / basePresenca;

        LocalDate ultimoTreino = ultimoTreino(treinos, agenda);
        long diasSemTreinar = ultimoTreino == null ? 30 : Math.min(30, ChronoUnit.DAYS.between(ultimoTreino, hoje));
        long diasSemContato = calcularDiasSemContato(aluno, hoje);
        Double mediaNota = calcularMediaNota(treinos);
        double quedaFrequencia = calcularQuedaFrequencia(agenda);
        QuedaCarga quedaCarga = calcularQuedaCarga(execucoes);

        int score = 0;
        List<String> sinais = new ArrayList<>();

        int pontosFaltas = pontosFaltas(faltas);
        score += pontosFaltas;
        if (pontosFaltas > 0) sinais.add(faltas + " falta(s) nos últimos 30 dias (" + pontosFaltas + " pts)");

        int pontosFrequencia = pontosQuedaFrequencia(quedaFrequencia);
        score += pontosFrequencia;
        if (pontosFrequencia > 0) sinais.add("queda de frequência de " + formatar(quedaFrequencia) + "% (" + pontosFrequencia + " pts)");

        int pontosInatividade = pontosInatividade(diasSemTreinar);
        score += pontosInatividade;
        if (pontosInatividade > 0) sinais.add(diasSemTreinar + " dias sem treino (" + pontosInatividade + " pts)");

        int pontosSatisfacao = pontosSatisfacao(mediaNota);
        score += pontosSatisfacao;
        if (pontosSatisfacao > 0) sinais.add("satisfação média de " + formatar(mediaNota) + "/5 (" + pontosSatisfacao + " pts)");

        int pontosContato = pontosContato(diasSemContato);
        score += pontosContato;
        if (pontosContato > 0) sinais.add(diasSemContato >= 30 ? "sem contato registrado nos últimos 30 dias (" + pontosContato + " pts)" : "" + diasSemContato + " dias sem contato (" + pontosContato + " pts)");

        int pontosCarga = pontosQuedaCarga(quedaCarga.percentual());
        score += pontosCarga;
        if (pontosCarga > 0) sinais.add("queda de carga de " + formatar(quedaCarga.percentual()) + "% (" + pontosCarga + " pts)");

        score = Math.min(100, score);
        String nivel = nivel(score);
        if (sinais.isEmpty()) sinais.add("Nenhum sinal relevante de abandono identificado.");

        return new ScoreRiscoAlunoView(
                aluno.getId(), aluno.getNome(), aluno.getTelefone(), score, nivel,
                faltas, realizados, taxa, treinos.size(), mediaNota, ultimoTreino,
                diasSemTreinar, diasSemContato, quedaCarga.relevante(), quedaCarga.percentual(),
                quedaFrequencia, sinais, acao(nivel));
    }

    private Map<Long, List<Agendamento>> agruparAgenda(List<Agendamento> lista) {
        Map<Long, List<Agendamento>> map = new HashMap<>();
        for (Agendamento a : lista) {
            if (a.getAluno() != null && a.getAluno().getId() != null && !"CANCELADO".equals(a.getStatus())) {
                map.computeIfAbsent(a.getAluno().getId(), k -> new ArrayList<>()).add(a);
            }
        }
        return map;
    }

    private Map<Long, List<RegistroTreinoAluno>> agruparTreinos(List<RegistroTreinoAluno> lista) {
        Map<Long, List<RegistroTreinoAluno>> map = new HashMap<>();
        for (RegistroTreinoAluno r : lista) {
            if (r.getAluno() != null && r.getAluno().getId() != null) {
                map.computeIfAbsent(r.getAluno().getId(), k -> new ArrayList<>()).add(r);
            }
        }
        return map;
    }

    private Map<Long, List<ExecucaoExercicio>> agruparExecucoes(List<ExecucaoExercicio> lista) {
        Map<Long, List<ExecucaoExercicio>> map = new HashMap<>();
        for (ExecucaoExercicio e : lista) {
            if (e.getRegistro() != null && e.getRegistro().getAluno() != null && e.getRegistro().getAluno().getId() != null) {
                map.computeIfAbsent(e.getRegistro().getAluno().getId(), k -> new ArrayList<>()).add(e);
            }
        }
        return map;
    }

    private LocalDate ultimoTreino(List<RegistroTreinoAluno> treinos, List<Agendamento> agenda) {
        LocalDate ultimoRegistro = treinos.stream().map(RegistroTreinoAluno::getDataExecucao).filter(java.util.Objects::nonNull).max(LocalDate::compareTo).orElse(null);
        LocalDate ultimoAgendamento = agenda.stream().filter(a -> "REALIZADO".equals(a.getStatus()))
                .map(a -> a.getInicio() == null ? null : a.getInicio().toLocalDate()).filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo).orElse(null);
        if (ultimoRegistro == null) return ultimoAgendamento;
        if (ultimoAgendamento == null) return ultimoRegistro;
        return ultimoRegistro.isAfter(ultimoAgendamento) ? ultimoRegistro : ultimoAgendamento;
    }

    private long calcularDiasSemContato(Aluno aluno, LocalDate hoje) {
        LocalDateTime ultimo = interacoes.ultimaInteracao(aluno.getPersonal().getId(), aluno.getId());
        if (ultimo == null) return 30;
        return Math.max(0, Math.min(30, ChronoUnit.DAYS.between(ultimo.toLocalDate(), hoje)));
    }

    private Double calcularMediaNota(List<RegistroTreinoAluno> treinos) {
        return treinos.stream().map(RegistroTreinoAluno::getNota).filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).average().orElse(Double.NaN);
    }

    private double calcularQuedaFrequencia(List<Agendamento> agenda) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioAtual = hoje.minusDays(14);
        long atual = agenda.stream().filter(a -> "REALIZADO".equals(a.getStatus()) && a.getInicio() != null && !a.getInicio().toLocalDate().isBefore(inicioAtual)).count();
        long anterior = agenda.stream().filter(a -> "REALIZADO".equals(a.getStatus()) && a.getInicio() != null && a.getInicio().toLocalDate().isBefore(inicioAtual)).count();
        if (anterior <= 0) return 0D;
        return Math.max(0D, ((anterior - atual) * 100D) / anterior);
    }

    private QuedaCarga calcularQuedaCarga(List<ExecucaoExercicio> lista) {
        Map<String, List<ExecucaoExercicio>> porExercicio = new LinkedHashMap<>();
        lista.stream()
                .sorted(Comparator.comparing((ExecucaoExercicio e) -> e.getRegistro().getDataExecucao(), Comparator.reverseOrder())
                        .thenComparing(ExecucaoExercicio::getDataRegistro, Comparator.reverseOrder()))
                .forEach(e -> {
                    Long exId = e.getTreinoExercicio().getExercicio().getId();
                    porExercicio.computeIfAbsent(String.valueOf(exId), k -> new ArrayList<>());
                    List<ExecucaoExercicio> h = porExercicio.get(String.valueOf(exId));
                    if (h.size() < 2) h.add(e);
                });

        double maiorQueda = 0D;
        for (List<ExecucaoExercicio> h : porExercicio.values()) {
            if (h.size() < 2) continue;
            Double atual = numero(h.get(0).getCargaRealizada());
            Double anterior = numero(h.get(1).getCargaRealizada());
            if (atual == null || anterior == null || anterior <= 0D) continue;
            double variacao = ((atual - anterior) / anterior) * 100D;
            if (variacao < maiorQueda) maiorQueda = variacao;
        }
        return new QuedaCarga(maiorQueda < -4.9D, Math.abs(maiorQueda));
    }

    private Double numero(String valor) {
        if (valor == null || valor.isBlank()) return null;
        Matcher matcher = NUMERO.matcher(valor.replace(',', '.'));
        Double ultimo = null;
        while (matcher.find()) {
            try { ultimo = Double.parseDouble(matcher.group(1)); } catch (NumberFormatException ignored) { }
        }
        return ultimo;
    }

    private int pontosFaltas(long faltas) { return faltas >= 3 ? 25 : faltas == 2 ? 18 : faltas == 1 ? 10 : 0; }
    private int pontosQuedaFrequencia(double queda) { return queda >= 40D ? 15 : queda >= 20D ? 8 : 0; }
    private int pontosInatividade(long dias) { return dias > 14 ? 20 : dias > 7 ? 10 : 0; }
    private int pontosSatisfacao(Double media) {
        if (media == null || media.isNaN()) return 0;
        return media < 3D ? 15 : media < 3.5D ? 10 : 0;
    }
    private int pontosContato(long dias) { return dias > 14 ? 10 : 0; }
    private int pontosQuedaCarga(double queda) { return queda >= 10D ? 15 : queda >= 5D ? 8 : 0; }

    private String nivel(int score) {
        if (score >= 75) return "CRITICO";
        if (score >= 50) return "ALTO";
        if (score >= 25) return "MEDIO";
        return "BAIXO";
    }

    private String acao(String nivel) {
        return switch (nivel) {
            case "CRITICO" -> "Entrar em contato hoje, entender a causa e propor ajuste de horário ou treino.";
            case "ALTO" -> "Fazer contato nos próximos 1–2 dias e revisar frequência, rotina e evolução.";
            case "MEDIO" -> "Acompanhar a próxima semana e fazer um contato preventivo se o sinal persistir.";
            default -> "Manter acompanhamento e continuar registrando presença, satisfação e evolução.";
        };
    }

    private String formatar(double valor) { return String.format(java.util.Locale.US, "%.1f", valor); }
    private String formatar(Double valor) { return valor == null || valor.isNaN() ? "sem avaliação" : String.format(java.util.Locale.US, "%.1f", valor); }

    private record QuedaCarga(boolean relevante, double percentual) {}
}
