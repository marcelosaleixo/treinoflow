package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.RetencaoFaltaAlunoView;
import com.marceloaleixo.treinoflow.entity.Agendamento;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.repository.AgendamentoRepository;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RetencaoFaltasService {
    private final AgendamentoRepository agendamentos;
    private final AlunoRepository alunos;
    private final InteracaoCrmRepository interacoes;
    private final UsuarioPersonalRepository personais;

    public RetencaoFaltasService(AgendamentoRepository agendamentos,
                                  AlunoRepository alunos,
                                  InteracaoCrmRepository interacoes,
                                  UsuarioPersonalRepository personais) {
        this.agendamentos = agendamentos;
        this.alunos = alunos;
        this.interacoes = interacoes;
        this.personais = personais;
    }

    @Transactional(readOnly = true)
    public List<RetencaoFaltaAlunoView> listarRiscos(Long personalId) {
        if (personalId == null || personalId <= 0) {
            throw new IllegalArgumentException("Personal inválido.");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicio = agora.minusDays(30);
        List<Agendamento> periodo = agendamentos.buscarDoPeriodoComAluno(personalId, inicio, agora.plusNanos(1));

        Map<Long, List<Agendamento>> porAluno = periodo.stream()
                .filter(a -> a.getAluno() != null && a.getAluno().getId() != null)
                .filter(a -> !"CANCELADO".equals(a.getStatus()))
                .collect(Collectors.groupingBy(a -> a.getAluno().getId(), LinkedHashMap::new, Collectors.toList()));

        return alunos.findByPersonalIdAndStatusOrderByNomeAsc(personalId, "ATIVO").stream()
                .map(aluno -> montar(aluno, porAluno.getOrDefault(aluno.getId(), List.of())))
                .filter(v -> v != null)
                .sorted(Comparator.comparingInt(this::pesoPrioridade)
                        .thenComparing(RetencaoFaltaAlunoView::faltas30Dias, Comparator.reverseOrder())
                        .thenComparing(RetencaoFaltaAlunoView::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public int gerarAcoes(Long personalId) {
        UsuarioPersonal personal = personais.findById(personalId)
                .orElseThrow(() -> new IllegalArgumentException("Personal não encontrado."));
        int gerados = 0;
        LocalDateTime contatoDesde = LocalDateTime.now().minusDays(7);

        for (RetencaoFaltaAlunoView risco : listarRiscos(personalId)) {
            if (interacoes.existeFollowUpPendente(personalId, risco.alunoId(), ResultadoCrm.EM_ACOMPANHAMENTO)) {
                continue;
            }
            if (interacoes.existeContatoDesde(personalId, risco.alunoId(), contatoDesde)) {
                continue;
            }

            Aluno aluno = alunos.findByIdAndPersonalId(risco.alunoId(), personalId)
                    .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal."));

            InteracaoCrm interacao = new InteracaoCrm();
            interacao.setPersonal(personal);
            interacao.setAluno(aluno);
            interacao.setCanal(aluno.getTelefone() == null || aluno.getTelefone().isBlank()
                    ? CanalCrm.INTERNO : CanalCrm.WHATSAPP);
            interacao.setTipo(TipoInteracaoCrm.RETENCAO);
            interacao.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO);
            interacao.setAssunto("Retenção automática · faltas");
            interacao.setDescricao(descricao(risco));
            interacao.setDataContato(LocalDateTime.now());
            interacao.setDataProximaAcao(LocalDate.now());
            interacoes.save(interacao);
            gerados++;
        }
        return gerados;
    }

    private RetencaoFaltaAlunoView montar(Aluno aluno, List<Agendamento> agenda) {
        long faltas = agenda.stream().filter(a -> "FALTOU".equals(a.getStatus())).count();
        long realizados = agenda.stream().filter(a -> "REALIZADO".equals(a.getStatus())).count();
        long base = faltas + realizados;
        double taxa = base == 0 ? 0D : realizados * 100D / base;

        boolean risco = faltas >= 2 || (base >= 3 && taxa < 75D);
        if (!risco) return null;

        String prioridade = faltas >= 3 || (base >= 4 && taxa < 60D) ? "ALTA" : "MEDIA";
        String motivo = faltas >= 2
                ? faltas + " faltas nos últimos 30 dias"
                : "taxa de comparecimento de " + String.format("%.1f", taxa) + "%";
        return new RetencaoFaltaAlunoView(
                aluno.getId(), aluno.getNome(), aluno.getTelefone(), faltas, realizados,
                taxa, agenda.stream().map(Agendamento::getInicio).max(LocalDateTime::compareTo).orElse(null),
                prioridade, motivo, mensagem(aluno.getNome(), faltas, taxa));
    }

    private int pesoPrioridade(RetencaoFaltaAlunoView view) {
        return "ALTA".equals(view.prioridade()) ? 0 : 1;
    }

    private String descricao(RetencaoFaltaAlunoView risco) {
        return "Aluno identificado automaticamente pela análise de presença.\n\n"
                + "Motivo: " + risco.motivo() + ".\n"
                + "Faltas nos últimos 30 dias: " + risco.faltas30Dias() + ".\n"
                + "Treinos realizados: " + risco.realizados30Dias() + ".\n"
                + "Taxa de comparecimento: " + String.format("%.1f", risco.taxaComparecimento()) + "%.\n\n"
                + "Mensagem sugerida para WhatsApp:\n" + risco.mensagemWhatsApp();
    }

    public String mensagem(String nome, long faltas, double taxa) {
        String primeiroNome = nome == null ? "" : nome.trim();
        int espaco = primeiroNome.indexOf(' ');
        if (espaco > 0) primeiroNome = primeiroNome.substring(0, espaco);
        return "Olá, " + primeiroNome + "! Tudo bem? Percebi que tivemos " + faltas
                + " falta(s) nos últimos dias e queria saber se está tudo certo. "
                + "Sua rotina mudou ou posso ajustar seus horários/treino para facilitar sua frequência? "
                + "Me chama para organizarmos isso juntos.";
    }
}
