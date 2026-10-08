package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.CarteiraAlunoView;
import com.marceloaleixo.treinoflow.dto.CarteiraDashboardView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SegmentacaoCarteiraService {
    private final AlunoRepository alunos;
    private final RegistroTreinoAlunoRepository registros;
    private final InteracaoCrmRepository interacoes;

    public SegmentacaoCarteiraService(AlunoRepository alunos,
                                      RegistroTreinoAlunoRepository registros,
                                      InteracaoCrmRepository interacoes) {
        this.alunos = alunos;
        this.registros = registros;
        this.interacoes = interacoes;
    }

    @Transactional(readOnly = true)
    public CarteiraDashboardView dashboard(Long personalId) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio7 = hoje.minusDays(6);
        LocalDate inicio30 = hoje.minusDays(29);
        LocalDateTime limiteRecuperado = hoje.minusDays(29).atStartOfDay();

        List<Aluno> ativos = alunos.findByPersonalIdAndStatusOrderByNomeAsc(personalId, "ATIVO");
        List<RegistroTreinoAluno> historico = registros.findAll().stream()
                .filter(r -> r.getAluno() != null && r.getAluno().getPersonal() != null)
                .filter(r -> personalId.equals(r.getAluno().getPersonal().getId()))
                .filter(RegistroTreinoAluno::isConcluido)
                .toList();
        Map<Long, List<RegistroTreinoAluno>> registrosPorAluno = historico.stream()
                .collect(Collectors.groupingBy(r -> r.getAluno().getId()));

        List<InteracaoCrm> historicoCrm = interacoes.findByPersonalIdOrderByDataContatoDesc(personalId);
        Map<Long, List<InteracaoCrm>> crmPorAluno = historicoCrm.stream()
                .filter(i -> i.getAluno() != null)
                .collect(Collectors.groupingBy(i -> i.getAluno().getId()));

        List<CarteiraAlunoView> carteira = new ArrayList<>();
        for (Aluno aluno : ativos) {
            carteira.add(montar(aluno,
                    registrosPorAluno.getOrDefault(aluno.getId(), List.of()),
                    crmPorAluno.getOrDefault(aluno.getId(), List.of()),
                    inicio7, inicio30, limiteRecuperado));
        }

        carteira.sort(Comparator.comparingInt(CarteiraAlunoView::risco).reversed()
                .thenComparing(CarteiraAlunoView::nome, String.CASE_INSENSITIVE_ORDER));

        long saudaveis = carteira.stream().filter(a -> "SAUDAVEL".equals(a.segmento())).count();
        long atencao = carteira.stream().filter(a -> "ATENCAO".equals(a.segmento())).count();
        long risco = carteira.stream().filter(a -> "RISCO".equals(a.segmento())).count();
        long criticos = carteira.stream().filter(a -> "CRITICO".equals(a.segmento())).count();
        long recuperados = carteira.stream().filter(a -> "RECUPERADO".equals(a.segmento())).count();
        double riscoMedio = carteira.stream().mapToInt(CarteiraAlunoView::risco).average().orElse(0D);

        return new CarteiraDashboardView(
                carteira.size(), saudaveis, atencao, risco, criticos, recuperados,
                riscoMedio, carteira
        );
    }

    private CarteiraAlunoView montar(Aluno aluno,
                                     List<RegistroTreinoAluno> registrosAluno,
                                     List<InteracaoCrm> interacoesAluno,
                                     LocalDate inicio7,
                                     LocalDate inicio30,
                                     LocalDateTime limiteRecuperado) {
        long sete = registrosAluno.stream().filter(r -> r.getDataExecucao() != null && !r.getDataExecucao().isBefore(inicio7)).count();
        long trinta = registrosAluno.stream().filter(r -> r.getDataExecucao() != null && !r.getDataExecucao().isBefore(inicio30)).count();
        LocalDate ultimoTreino = registrosAluno.stream()
                .map(RegistroTreinoAluno::getDataExecucao)
                .filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
        double mediaNota = registrosAluno.stream()
                .filter(r -> r.getNota() != null)
                .mapToInt(RegistroTreinoAluno::getNota)
                .average().orElse(0D);
        LocalDate ultimoContato = interacoesAluno.stream()
                .map(InteracaoCrm::getDataContato)
                .filter(java.util.Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        boolean recuperado = interacoesAluno.stream().anyMatch(i ->
                i.getResultado() == ResultadoCrm.RECUPERADO
                        && i.getDataContato() != null
                        && !i.getDataContato().isBefore(limiteRecuperado));

        int risco = 0;
        List<String> motivos = new ArrayList<>();
        if (ultimoTreino == null || ultimoTreino.isBefore(inicio30)) {
            risco += 50;
            motivos.add("sem treino há 30+ dias");
        } else if (sete == 0) {
            risco += 20;
            motivos.add("sem treino nesta semana");
        }
        if (trinta < 5) {
            risco += 15;
            motivos.add("baixa frequência");
        }
        if (mediaNota > 0 && mediaNota < 3.5) {
            risco += 15;
            motivos.add("satisfação baixa");
        }
        if (ultimoContato == null || ultimoContato.isBefore(inicio30)) {
            risco += 10;
            motivos.add("sem contato recente");
        }
        risco = Math.min(100, risco);

        String segmento;
        if (recuperado) segmento = "RECUPERADO";
        else if (risco >= 75) segmento = "CRITICO";
        else if (risco >= 50) segmento = "RISCO";
        else if (risco >= 25) segmento = "ATENCAO";
        else segmento = "SAUDAVEL";

        String motivo = motivos.isEmpty()
                ? "Frequência e acompanhamento dentro do esperado."
                : String.join(" · ", motivos);

        return new CarteiraAlunoView(
                aluno.getId(), aluno.getNome(), aluno.getTelefone(), segmento, risco,
                motivo, ultimoTreino, trinta, sete, mediaNota, ultimoContato
        );
    }
}
