package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import com.marceloaleixo.treinoflow.repository.EntregaAlertaEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FilaAlertaEmailService {
    private static final Logger log = LoggerFactory.getLogger(FilaAlertaEmailService.class);
    private final EntregaAlertaEmailRepository repository;
    private final EmailNotificacaoService emailService;
    private final int maxTentativas;
    @Autowired @Lazy private FilaAlertaEmailService self;
    public FilaAlertaEmailService(EntregaAlertaEmailRepository repository, EmailNotificacaoService emailService,
            @Value("${treinoflow.notificacoes.alertas.email.max-tentativas:4}") int maxTentativas) {
        this.repository=repository; this.emailService=emailService; this.maxTentativas=Math.max(1, Math.min(10,maxTentativas));
    }
    @Transactional
    public void enfileirar(String incidenteId, String destino, String assunto, String mensagem) {
        if (destino == null || destino.isBlank()) return;
        if (incidenteId == null || incidenteId.isBlank()) {
            throw new IllegalArgumentException("O identificador do incidente é obrigatório para enfileirar um alerta.");
        }
        // Idempotência: um incidente só pode originar uma entrega; reenvios reutilizam a mesma linha.
        if (repository.existsByIncidenteId(incidenteId)) return;
        try {
            repository.saveAndFlush(new EntregaAlertaEmail(UUID.randomUUID().toString(), incidenteId, destino, assunto, mensagem, LocalDateTime.now()));
        } catch (org.springframework.dao.DataIntegrityViolationException concorrencia) {
            // Duas instâncias podem passar pela consulta exists ao mesmo tempo; a restrição única decide o vencedor.
            if (!repository.existsByIncidenteId(incidenteId)) throw concorrencia;
            log.debug("Entrega de alerta já criada para o incidente {}.", incidenteId);
        }
    }
    @Scheduled(fixedDelayString = "${treinoflow.notificacoes.alertas.email.intervalo-ms:30000}")
    public void processarFila() {
        LocalDateTime agora = LocalDateTime.now();
        List<String> ids = repository.findTop20ByStatusAndProximaTentativaEmLessThanEqualOrderByCriadaEmAsc("PENDENTE", agora)
                .stream().map(EntregaAlertaEmail::getId).toList();
        for (String id : ids) processarUmReivindicado(id);
    }

    private void processarUmReivindicado(String id) {
        LocalDateTime agora = LocalDateTime.now();
        int reivindicada = repository.reivindicarAtomico(id, agora, agora.plusMinutes(5));
        if (reivindicada == 0) return; // outra instância já reivindicou ou a mensagem não está pronta

        EntregaAlertaEmail entrega = repository.findById(id).orElse(null);
        if (entrega == null || !"PROCESSANDO".equals(entrega.getStatus())) return;
        try {
            // A reserva é gravada antes do SMTP; nenhuma transação de banco fica aberta durante a chamada externa.
            emailService.enviar(entrega.getDestino(), entrega.getAssunto(), entrega.getMensagem());
            self.concluirSucesso(id);
        } catch (Exception e) {
            self.concluirFalha(id, mensagemSegura(e));
            log.warn("Falha no envio de alerta por e-mail. entregaId={}", id);
        }
    }

    @Transactional
    protected void concluirSucesso(String id) {
        EntregaAlertaEmail entrega = repository.buscarParaAtualizacao(id).orElse(null);
        if (entrega == null || !"PROCESSANDO".equals(entrega.getStatus())) return;
        entrega.registrarSucesso(LocalDateTime.now());
        repository.save(entrega);
    }

    @Transactional
    protected void concluirFalha(String id, String erro) {
        EntregaAlertaEmail entrega = repository.buscarParaAtualizacao(id).orElse(null);
        if (entrega == null || !"PROCESSANDO".equals(entrega.getStatus())) return;
        entrega.registrarFalha(erro, LocalDateTime.now(), maxTentativas);
        repository.save(entrega);
    }

    @Scheduled(fixedDelayString = "${treinoflow.notificacoes.alertas.email.recuperacao-ms:60000}")
    @Transactional
    public void recuperarReservasExpiradas() {
        LocalDateTime agora = LocalDateTime.now();
        List<EntregaAlertaEmail> expiradas = repository.findTop100ByStatusAndReservadoAteLessThanEqualOrderByReservadoAteAsc("PROCESSANDO", agora);
        for (EntregaAlertaEmail entrega : expiradas) {
            EntregaAlertaEmail atual = repository.buscarParaAtualizacao(entrega.getId()).orElse(null);
            if (atual != null && "PROCESSANDO".equals(atual.getStatus()) && atual.getReservadoAte() != null && !atual.getReservadoAte().isAfter(agora)) {
                atual.recuperarReserva(agora, maxTentativas);
                repository.save(atual);
            }
        }
    }

    @Transactional
    public boolean reenviar(String id) {
        EntregaAlertaEmail entrega = repository.buscarParaAtualizacao(id).orElse(null);
        if (entrega == null || !"FALHA_FINAL".equals(entrega.getStatus())) return false;
        entrega.reabrirParaReenvio(LocalDateTime.now());
        repository.save(entrega);
        return true;
    }
    @Transactional(readOnly = true)
    public Page<EntregaResumo> pesquisar(String status, LocalDateTime inicio, LocalDateTime fim, int pagina, int tamanho) {
        int paginaSegura = Math.max(0, pagina);
        int tamanhoSeguro = Math.max(10, Math.min(100, tamanho));
        return repository.pesquisar(status, inicio, fim,
                PageRequest.of(paginaSegura, tamanhoSeguro, Sort.by(Sort.Direction.DESC, "criadaEm")))
                .map(e -> new EntregaResumo(e.getId(), e.getIncidenteId(), e.getDestino(), e.getAssunto(), e.getStatus(), e.getTentativas(), e.getCriadaEm(), e.getUltimaTentativaEm(), e.getEnviadaEm(), e.getUltimoErro()));
    }
    @Transactional(readOnly = true)
    public Indicadores obterIndicadores() {
        return new Indicadores(repository.countByStatus("PENDENTE"), repository.countByStatus("ENVIADO"), repository.countByStatus("FALHA_FINAL"));
    }
    public record Indicadores(long pendentes, long enviados, long falhasFinais) {}
    private String mensagemSegura(Exception e) { String m=e.getMessage(); if(m==null || m.isBlank()) return e.getClass().getSimpleName(); m=m.replaceAll("[\\r\\n\\t]+"," "); return m.substring(0,Math.min(m.length(),1200)); }
    public record EntregaResumo(String id, String incidenteId, String destino, String assunto, String status, int tentativas, LocalDateTime criadaEm, LocalDateTime ultimaTentativaEm, LocalDateTime enviadaEm, String ultimoErro) {}
}
