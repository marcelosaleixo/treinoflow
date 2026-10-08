package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.entity.*;
import com.marceloaleixo.treinoflow.enums.*;
import com.marceloaleixo.treinoflow.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.math.BigDecimal;
import java.util.List;

/** Etapa 75: execução automática, controlada e auditável das recomendações de retenção. */
@Service
public class AutomacaoRetencaoInteligenteService {
    private static final String MARCADOR = "ETAPA75";
    private final AutomacaoRetencaoConfigRepository configs;
    private final ScoreRiscoAlunoService scoreService;
    private final AcaoAssistenteRepository acoes;
    private final AlunoRepository alunos;
    private final UsuarioPersonalRepository personais;
    private final InteracaoCrmRepository interacoes;
    private final WhatsAppNotificacaoService whatsapp;
    private final RecomendacaoAdaptativaService recomendacao;
    private final ResultadoAcoesAssistenteService resultados;
    private final JornadaRetencaoService jornada;
    private final MensagemInteligenteService mensagens;
    private final OtimizacaoRetencaoService otimizacao;

    public AutomacaoRetencaoInteligenteService(AutomacaoRetencaoConfigRepository configs,
                                                ScoreRiscoAlunoService scoreService,
                                                AcaoAssistenteRepository acoes,
                                                AlunoRepository alunos,
                                                UsuarioPersonalRepository personais,
                                                InteracaoCrmRepository interacoes,
                                                WhatsAppNotificacaoService whatsapp,
                                                RecomendacaoAdaptativaService recomendacao,
                                                ResultadoAcoesAssistenteService resultados,
                                                JornadaRetencaoService jornada,
                                                MensagemInteligenteService mensagens,
                                                OtimizacaoRetencaoService otimizacao) {
        this.configs=configs; this.scoreService=scoreService; this.acoes=acoes; this.alunos=alunos;
        this.personais=personais; this.interacoes=interacoes; this.whatsapp=whatsapp;
        this.recomendacao=recomendacao; this.resultados=resultados; this.jornada=jornada;
        this.mensagens=mensagens; this.otimizacao=otimizacao;
    }

    @Transactional(readOnly = true)
    public AutomacaoRetencaoConfig obterOuCriar(Long personalId) {
        return configs.findByPersonalId(personalId).orElseGet(() -> {
            AutomacaoRetencaoConfig c = new AutomacaoRetencaoConfig();
            c.setPersonal(personais.findById(personalId).orElseThrow());
            return configs.save(c);
        });
    }

    @Transactional
    public void salvar(Long personalId, boolean ativa, boolean whatsappCritico, boolean followUpAlto,
                       int scoreMinimo, int maxAcoesDia, int cooldownDias, String horaInicio, String horaFim, BigDecimal valorMensalAlunoEstimado, BigDecimal custoAutomacaoMensal) {
        if (scoreMinimo < 25 || scoreMinimo > 100) throw new IllegalArgumentException("Score mínimo deve ficar entre 25 e 100.");
        if (maxAcoesDia < 1 || maxAcoesDia > 100) throw new IllegalArgumentException("Limite diário deve ficar entre 1 e 100.");
        if (cooldownDias < 1 || cooldownDias > 30) throw new IllegalArgumentException("Cooldown deve ficar entre 1 e 30 dias.");
        validarHora(horaInicio); validarHora(horaFim);
        if (valorMensalAlunoEstimado == null || valorMensalAlunoEstimado.signum() < 0) throw new IllegalArgumentException("Valor mensal estimado do aluno não pode ser negativo.");
        if (custoAutomacaoMensal == null || custoAutomacaoMensal.signum() < 0) throw new IllegalArgumentException("Custo mensal da automação não pode ser negativo.");
        AutomacaoRetencaoConfig c = configs.findByPersonalId(personalId).orElseGet(() -> {
            AutomacaoRetencaoConfig n = new AutomacaoRetencaoConfig();
            n.setPersonal(personais.findById(personalId).orElseThrow()); return n;
        });
        c.setAtiva(ativa); c.setWhatsappCritico(whatsappCritico); c.setFollowUpAlto(followUpAlto);
        c.setScoreMinimo(scoreMinimo); c.setMaxAcoesDia(maxAcoesDia); c.setCooldownDias(cooldownDias);
        c.setHoraInicio(horaInicio); c.setHoraFim(horaFim); c.setValorMensalAlunoEstimado(valorMensalAlunoEstimado); c.setCustoAutomacaoMensal(custoAutomacaoMensal); configs.save(c);
    }

    @Transactional
    public Execucao executar(Long personalId) {
        return executar(personalId, false);
    }

    /** Execução usada pelo scheduler: aplica também o melhor horário aprendido. */
    @Transactional
    public Execucao executarNoHorarioAprendido(Long personalId) {
        return executar(personalId, true);
    }

    private Execucao executar(Long personalId, boolean respeitarHorarioAprendido) {
        AutomacaoRetencaoConfig config = configs.findByPersonalId(personalId).orElse(null);
        if (config == null || !config.isAtiva()) return new Execucao(0,0,0,"Automação desativada.");
        if (!dentroDoHorario(config)) return new Execucao(0,0,0,"Fora do horário permitido.");
        LocalDateTime agora=LocalDateTime.now();
        LocalDateTime inicioDia=LocalDate.now().atStartOfDay();
        long usadas=acoes.countAutomaticasDesde(personalId,inicioDia);
        int limite=Math.max(0, config.getMaxAcoesDia() - (int)usadas);
        if(limite==0) return new Execucao(0,0,0,"Limite diário atingido.");

        int avaliadas=0,enviadas=0,ignoradas=0;
        for(ScoreRiscoAlunoView risco: scoreService.listar(personalId)) {
            if(enviadas>=limite) break;
            if(risco.score()<config.getScoreMinimo()) { ignoradas++; continue; }
            if(risco.diasSemContato()<config.getCooldownDias()) { ignoradas++; continue; }
            if(interacoes.existeFollowUpPendente(personalId,risco.alunoId(),ResultadoCrm.EM_ACOMPANHAMENTO)) { ignoradas++; continue; }
            if(acoes.existeAutomaticaDesde(personalId,risco.alunoId(),agora.minusDays(config.getCooldownDias()))) { ignoradas++; continue; }
            var rec=recomendacao.recomendar(personalId,risco);
            TipoAcaoAssistente tipo=tipoPermitido(risco,rec.acao(),config);
            if(tipo==null) { ignoradas++; continue; }

            // Quando o histórico já possui amostra suficiente, a execução horária
            // só acontece no horário vencedor daquela faixa de risco. Sem amostra,
            // o motor continua usando a janela configurada pelo Personal.
            var horario=otimizacao.melhorHorarioPara(risco.score(),personalId);
            if(respeitarHorarioAprendido && horario!=null && LocalTime.now().getHour()!=horario.hora()) {
                ignoradas++; continue;
            }

            avaliadas++;
            try {
                if(tipo==TipoAcaoAssistente.WHATSAPP) executarWhatsApp(personalId,risco,rec.justificativa());
                else executarFollowUp(personalId,risco,rec.justificativa());
                enviadas++;
            } catch(RuntimeException ex) { ignoradas++; }
        }
        return new Execucao(avaliadas,enviadas,ignoradas,"Rotina concluída.");
    }

    @Scheduled(cron="${treinoflow.assistente.automacao-cron:0 5 * * * *}")
    public void rotinaHoraria() {
        configs.findByAtivaTrue().forEach(c -> { try { executarNoHorarioAprendido(c.getPersonal().getId()); } catch(RuntimeException ignored) {} });
    }

    private TipoAcaoAssistente tipoPermitido(ScoreRiscoAlunoView risco,String recomendada,AutomacaoRetencaoConfig c){
        if(recomendada==null) return null;
        if(recomendada.equalsIgnoreCase("WhatsApp") && c.isWhatsappCritico() && risco.score()>=75 && risco.telefone()!=null && !risco.telefone().isBlank()) return TipoAcaoAssistente.WHATSAPP;
        if(recomendada.equalsIgnoreCase("Follow-up") && c.isFollowUpAlto() && risco.score()>=50) return TipoAcaoAssistente.FOLLOW_UP;
        return null;
    }

    private void executarWhatsApp(Long personalId,ScoreRiscoAlunoView risco,String justificativa){
        Aluno aluno=alunos.findByIdAndPersonalId(risco.alunoId(),personalId).orElseThrow();
        var escolha=mensagens.escolher(personalId,risco,TipoAcaoAssistente.WHATSAPP);
        String mensagem=escolha.mensagem();
        whatsapp.enviar(aluno.getTelefone(),mensagem);
        InteracaoCrm i=new InteracaoCrm(); i.setPersonal(personais.findById(personalId).orElseThrow()); i.setAluno(aluno);
        i.setCanal(CanalCrm.WHATSAPP); i.setTipo(TipoInteracaoCrm.RETENCAO); i.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO);
        i.setAssunto("Automação de retenção · WhatsApp"); i.setDescricao(MARCADOR+" · WhatsApp automático. Score "+risco.score()+"/100. Confiança "+escolha.confianca()+", amostra "+escolha.amostra()+". "+justificativa);
        i.setDataProximaAcao(LocalDate.now().plusDays(2)); interacoes.save(i);
        AcaoAssistente acao=resultados.registrarAcaoExecutada(personalId,risco.alunoId(),risco.score(),MARCADOR+" · WhatsApp automático · "+escolha.justificativa(),mensagem,TipoAcaoAssistente.WHATSAPP);
        marcarComoAutomaticaEIniciarJornada(acao,"CRITICO_WHATSAPP", escolha.experimentoId(), escolha.variante());
    }

    private void executarFollowUp(Long personalId,ScoreRiscoAlunoView risco,String justificativa){
        Aluno aluno=alunos.findByIdAndPersonalId(risco.alunoId(),personalId).orElseThrow();
        UsuarioPersonal p=personais.findById(personalId).orElseThrow();
        var escolha=mensagens.escolher(personalId,risco,TipoAcaoAssistente.FOLLOW_UP);
        InteracaoCrm i=new InteracaoCrm(); i.setPersonal(p); i.setAluno(aluno); i.setCanal(CanalCrm.INTERNO); i.setTipo(TipoInteracaoCrm.RETENCAO);
        i.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO); i.setAssunto("Automação de retenção · Follow-up");
        i.setDescricao(MARCADOR+" · Follow-up automático. Score "+risco.score()+"/100. Confiança "+escolha.confianca()+", amostra "+escolha.amostra()+". "+justificativa+" Mensagem sugerida: "+escolha.mensagem());
        i.setDataProximaAcao(LocalDate.now()); interacoes.save(i);
        AcaoAssistente acao=resultados.registrarAcaoExecutada(personalId,risco.alunoId(),risco.score(),MARCADOR+" · Follow-up automático · "+escolha.justificativa(),escolha.mensagem(),TipoAcaoAssistente.FOLLOW_UP);
        marcarComoAutomaticaEIniciarJornada(acao,"ALTO_FOLLOW_UP", escolha.experimentoId(), escolha.variante());
    }

    private void marcarComoAutomaticaEIniciarJornada(AcaoAssistente acao,String regra, Long experimentoId, String variante){
        acao.setAutomatica(true);
        acao.setExperimentoId(experimentoId);
        acao.setExperimentoVariante(variante);
        acao.setRegraAutomacao(regra);
        acoes.save(acao);
        jornada.iniciar(acao);
    }
    private boolean dentroDoHorario(AutomacaoRetencaoConfig c){ LocalTime agora=LocalTime.now(); LocalTime ini=LocalTime.parse(c.getHoraInicio()); LocalTime fim=LocalTime.parse(c.getHoraFim()); return !agora.isBefore(ini)&&!agora.isAfter(fim); }
    private void validarHora(String h){ try{LocalTime.parse(h);}catch(Exception e){throw new IllegalArgumentException("Horário inválido. Use HH:mm.");} }
    public record Execucao(int avaliadas,int executadas,int ignoradas,String mensagem){}
}
