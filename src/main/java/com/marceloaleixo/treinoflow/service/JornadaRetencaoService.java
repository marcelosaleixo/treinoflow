package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.AcaoAssistente;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.repository.AcaoAssistenteRepository;
import com.marceloaleixo.treinoflow.repository.AutomacaoRetencaoConfigRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/** Etapa 76: jornada automática de retenção com passos, encerramento e auditoria. */
@Service
public class JornadaRetencaoService {
    private static final String AGUARDANDO = "AGUARDANDO_RESPOSTA";
    private static final String RECUPERADA = "RECUPERADA";
    private static final String RENOVADA = "RENOVADA";
    private static final String CANCELADA = "CANCELADA";
    private static final String ENCERRADA = "ENCERRADA";

    private final AcaoAssistenteRepository acoes;
    private final InteracaoCrmRepository interacoes;
    private final AutomacaoRetencaoConfigRepository configs;
    private final ScoreRiscoAlunoService scoreService;
    private final WhatsAppNotificacaoService whatsapp;
    private final UsuarioPersonalRepository personais;

    public JornadaRetencaoService(AcaoAssistenteRepository acoes,
                                  InteracaoCrmRepository interacoes,
                                  AutomacaoRetencaoConfigRepository configs,
                                  ScoreRiscoAlunoService scoreService,
                                  WhatsAppNotificacaoService whatsapp,
                                  UsuarioPersonalRepository personais) {
        this.acoes = acoes;
        this.interacoes = interacoes;
        this.configs = configs;
        this.scoreService = scoreService;
        this.whatsapp = whatsapp;
        this.personais = personais;
    }

    /** Agenda o primeiro passo da jornada na própria ação automática criada pela Etapa 75. */
    @Transactional
    public void iniciar(AcaoAssistente acao) {
        if (!acao.isAutomatica()) return;
        if (acao.getJornadaId() == null || acao.getJornadaId().isBlank()) {
            acao.setJornadaId(UUID.randomUUID().toString());
        }
        acao.setEtapaJornada(1);
        acao.setJornadaStatus(AGUARDANDO);
        acao.setProximaAcaoEm(acao.getTipoAcao() == TipoAcaoAssistente.WHATSAPP
                ? LocalDateTime.now().plusDays(3)
                : LocalDateTime.now().plusDays(4));
        acoes.save(acao);
    }

    @Scheduled(cron = "${treinoflow.assistente.jornada-cron:0 15 * * * *}")
    @Transactional
    public void processarTodas() {
        // A própria lista de personais é usada para manter isolamento por tenant.
        for (UsuarioPersonal p : personais.findAll()) {
            try { processar(p.getId()); } catch (RuntimeException ignored) { }
        }
    }

    @Transactional
    public Execucao processar(Long personalId) {
        var config = configs.findByPersonalId(personalId).orElse(null);
        if (config == null || !config.isAtiva() || !dentroDoHorario(config.getHoraInicio(), config.getHoraFim())) {
            return new Execucao(0, 0, 0, "Jornada fora das condições de execução.");
        }
        LocalDateTime agora = LocalDateTime.now();
        List<AcaoAssistente> pendentes = acoes.buscarJornadasPendentes(personalId, agora);
        int avaliadas = 0, executadas = 0, encerradas = 0;
        long usadasHoje = acoes.countAutomaticasDesde(personalId, LocalDate.now().atStartOfDay());
        int limite = Math.max(0, config.getMaxAcoesDia() - (int) usadasHoje);
        for (AcaoAssistente acao : pendentes) {
            if (executadas >= limite) break;
            avaliadas++;
            if (acao.getResultado() == ResultadoCrm.RECUPERADO || acao.getResultado() == ResultadoCrm.RENOVADO || acao.getResultado() == ResultadoCrm.CANCELAMENTO) {
                encerrarPorResposta(acao, null);
                encerradas++;
                continue;
            }
            InteracaoCrm resposta = encontrarResposta(acao);
            if (resposta != null) {
                encerrarPorResposta(acao, resposta);
                encerradas++;
                continue;
            }
            ScoreRiscoAlunoView risco = localizarRisco(personalId, acao.getAluno().getId());
            if (risco == null || risco.score() < config.getScoreMinimo()) {
                acao.setJornadaStatus(ENCERRADA);
                acao.setProximaAcaoEm(null);
                acoes.save(acao);
                encerradas++;
                continue;
            }
            if (acao.getEtapaJornada() == 1) {
                executarSegundoContato(personalId, acao, risco);
                executadas++;
            } else if (acao.getEtapaJornada() == 2) {
                executarUltimoContato(personalId, acao, risco);
                executadas++;
            } else {
                acao.setJornadaStatus(ENCERRADA);
                acao.setProximaAcaoEm(null);
                acoes.save(acao);
                encerradas++;
            }
        }
        return new Execucao(avaliadas, executadas, encerradas, "Jornada processada.");
    }

    @Transactional(readOnly = true)
    public Resumo resumo(Long personalId) {
        long ativas = acoes.countJornadasPorStatus(personalId, AGUARDANDO);
        long recuperadas = acoes.countJornadasPorStatus(personalId, RECUPERADA);
        long renovadas = acoes.countJornadasPorStatus(personalId, RENOVADA);
        long canceladas = acoes.countJornadasPorStatus(personalId, CANCELADA);
        long encerradas = acoes.countJornadasPorStatus(personalId, ENCERRADA);
        long sucesso = recuperadas + renovadas;
        long finalizadas = sucesso + canceladas;
        double taxa = finalizadas == 0 ? 0 : sucesso * 100.0 / finalizadas;
        return new Resumo(ativas, recuperadas, renovadas, canceladas, encerradas, taxa);
    }

    private void executarSegundoContato(Long personalId, AcaoAssistente original, ScoreRiscoAlunoView risco) {
        String mensagem = mensagem(risco, "Só passando para reforçar que podemos ajustar seu treino e sua rotina para você não perder o ritmo. 💪");
        if (risco.telefone() != null && !risco.telefone().isBlank()) {
            whatsapp.enviar(risco.telefone(), mensagem);
            registrarContato(personalId, original, mensagem, "Jornada · segundo contato WhatsApp");
        }
        original.setEtapaJornada(2);
        original.setProximaAcaoEm(LocalDateTime.now().plusDays(4));
        acoes.save(original);
    }

    private void executarUltimoContato(Long personalId, AcaoAssistente original, ScoreRiscoAlunoView risco) {
        String mensagem = mensagem(risco, "Esta é minha última mensagem desta sequência. Se quiser retomar ou ajustar seu plano, me chama por aqui e eu te ajudo. 🤝");
        if (risco.telefone() != null && !risco.telefone().isBlank()) {
            whatsapp.enviar(risco.telefone(), mensagem);
            registrarContato(personalId, original, mensagem, "Jornada · último contato WhatsApp");
        }
        original.setEtapaJornada(3);
        original.setJornadaStatus(ENCERRADA);
        original.setProximaAcaoEm(null);
        acoes.save(original);
    }

    private void registrarContato(Long personalId, AcaoAssistente original, String mensagem, String assunto) {
        InteracaoCrm i = new InteracaoCrm();
        i.setPersonal(personais.findById(personalId).orElseThrow());
        i.setAluno(original.getAluno());
        i.setCanal(CanalCrm.WHATSAPP);
        i.setTipo(TipoInteracaoCrm.RETENCAO);
        i.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO);
        i.setAssunto(assunto);
        i.setDescricao("ETAPA76 · " + mensagem);
        i.setDataProximaAcao(LocalDate.now().plusDays(3));
        interacoes.save(i);
    }

    private InteracaoCrm encontrarResposta(AcaoAssistente acao) {
        if (acao.getResultado() == ResultadoCrm.RECUPERADO || acao.getResultado() == ResultadoCrm.RENOVADO || acao.getResultado() == ResultadoCrm.CANCELAMENTO) {
            return null;
        }
        List<InteracaoCrm> novas = interacoes.buscarDepois(acao.getPersonal().getId(), acao.getAluno().getId(), acao.getExecutadaEm());
        return novas.stream().filter(i -> i.getDescricao() == null || !i.getDescricao().startsWith("ETAPA76 ·")).findFirst().orElse(null);
    }

    private void encerrarPorResposta(AcaoAssistente acao, InteracaoCrm resposta) {
        ResultadoCrm resultado = resposta == null ? acao.getResultado() : resposta.getResultado();
        if (resultado == ResultadoCrm.RECUPERADO) { acao.setResultado(resultado); acao.setJornadaStatus(RECUPERADA); }
        else if (resultado == ResultadoCrm.RENOVADO) { acao.setResultado(resultado); acao.setJornadaStatus(RENOVADA); }
        else if (resultado == ResultadoCrm.CANCELAMENTO) { acao.setResultado(resultado); acao.setJornadaStatus(CANCELADA); }
        else { acao.setJornadaStatus(ENCERRADA); }
        acao.setResultadoEm(LocalDateTime.now());
        acao.setProximaAcaoEm(null);
        acoes.save(acao);
    }

    private ScoreRiscoAlunoView localizarRisco(Long personalId, Long alunoId) {
        return scoreService.listar(personalId).stream().filter(r -> r.alunoId().equals(alunoId)).findFirst().orElse(null);
    }

    private String mensagem(ScoreRiscoAlunoView r, String texto) {
        String nome = r.nome() == null ? "tudo bem" : r.nome().trim().split("\\s+")[0];
        return "Olá, " + nome + "! 👋\n\n" + texto;
    }

    private boolean dentroDoHorario(String inicio, String fim) {
        LocalTime agora = LocalTime.now();
        return !agora.isBefore(LocalTime.parse(inicio)) && !agora.isAfter(LocalTime.parse(fim));
    }

    public record Execucao(int avaliadas, int executadas, int encerradas, String mensagem) {}
    public record Resumo(long ativas, long recuperadas, long renovadas, long canceladas, long encerradas, double taxaRecuperacao) {}

}
