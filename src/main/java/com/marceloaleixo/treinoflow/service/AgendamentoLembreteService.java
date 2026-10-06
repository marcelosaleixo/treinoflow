package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Agendamento;
import com.marceloaleixo.treinoflow.entity.LembreteAgendamento;
import com.marceloaleixo.treinoflow.repository.AgendamentoRepository;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.LembreteAgendamentoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AgendamentoLembreteService {
    private final AgendamentoRepository agendamentos;
    private final LembreteAgendamentoRepository lembretes;
    private final AlunoRepository alunos;
    private final WhatsAppNotificacaoService whatsapp;
    private final boolean whatsappEnabled;
    private final String baseUrl;

    public AgendamentoLembreteService(AgendamentoRepository agendamentos,
                                      LembreteAgendamentoRepository lembretes,
                                      AlunoRepository alunos,
                                      WhatsAppNotificacaoService whatsapp,
                                      @Value("${treinoflow.notificacoes.whatsapp.enabled:false}") boolean whatsappEnabled,
                                      @Value("${treinoflow.app.base-url:}") String baseUrl) {
        this.agendamentos = agendamentos;
        this.lembretes = lembretes;
        this.alunos = alunos;
        this.whatsapp = whatsapp;
        this.whatsappEnabled = whatsappEnabled;
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
    }

    @Scheduled(cron = "${treinoflow.agenda.lembretes-cron:0 */15 * * * *}")
    @Transactional
    public void processar() {
        if (!whatsappEnabled) return;
        LocalDateTime agora = LocalDateTime.now();
        for (Agendamento agendamento : agendamentos.buscarProximosParaLembrete(agora, agora.plusHours(25))) {
            long minutos = Duration.between(agora, agendamento.getInicio()).toMinutes();
            if (minutos >= 23 * 60 + 45 && minutos <= 24 * 60 + 15) {
                enviarSeNecessario(agendamento, "24H", "Seu treino é amanhã");
            } else if (minutos >= 105 && minutos <= 135) {
                enviarSeNecessario(agendamento, "2H", "Seu treino começa em breve");
            }
        }
    }

    private void enviarSeNecessario(Agendamento a, String tipo, String titulo) {
        if (lembretes.existsByAgendamentoIdAndTipo(a.getId(), tipo)) return;
        String telefone = a.getAluno().getTelefone();
        if (telefone == null || telefone.isBlank()) return;

        String token = a.getAluno().getTokenPortal();
        if (token == null || token.isBlank()) {
            token = UUID.randomUUID().toString().replace("-", "");
            a.getAluno().setTokenPortal(token);
            alunos.save(a.getAluno());
        }
        String portal = portalUrl(token);
        String mensagem = "Olá, " + a.getAluno().getNome() + "! 👋\n\n" +
                titulo + ": " + a.getTipo().toLowerCase() + " com seu Personal " +
                a.getPersonal().getNome() + ".\n\n" +
                "📅 " + a.getInicio().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " às " + a.getInicio().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) +
                "\n\n" +
                "Confirme ou cancele pelo seu Portal do Aluno:" +
                (portal.isBlank() ? "\n/portal/" + token : "\n" + portal);

        whatsapp.enviar(telefone, mensagem);
        lembretes.save(new LembreteAgendamento()
                .setAgendamento(a)
                .setTipo(tipo)
                .setDataEnvio(LocalDateTime.now()));
    }

    private String portalUrl(String token) {
        if (token == null || token.isBlank()) return "";
        if (baseUrl.isBlank()) return "";
        return baseUrl.replaceAll("/$", "") + "/portal/" + token;
    }
}
