package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entregas_alertas_email", uniqueConstraints = {
        @UniqueConstraint(name = "uk_entrega_email_incidente", columnNames = "incidente_id")
}, indexes = {
        @Index(name = "idx_entrega_email_status_proxima", columnList = "status, proxima_tentativa_em"),
        @Index(name = "idx_entrega_email_criada", columnList = "criada_em")
})
public class EntregaAlertaEmail {
    @Id @Column(length = 36, nullable = false, updatable = false)
    private String id;
    @Column(name = "incidente_id", length = 36, nullable = false) private String incidenteId;
    @Column(nullable = false, length = 320) private String destino;
    @Column(nullable = false, length = 250) private String assunto;
    @Column(nullable = false, length = 4000) private String mensagem;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "tentativas", nullable = false) private int tentativas;
    @Column(name = "criada_em", nullable = false) private LocalDateTime criadaEm;
    @Column(name = "proxima_tentativa_em", nullable = false) private LocalDateTime proximaTentativaEm;
    @Column(name = "ultima_tentativa_em") private LocalDateTime ultimaTentativaEm;
    @Column(name = "enviada_em") private LocalDateTime enviadaEm;
    @Column(name = "ultimo_erro", length = 1200) private String ultimoErro;
    @Column(name = "reservado_ate") private LocalDateTime reservadoAte;
    protected EntregaAlertaEmail() {}
    public EntregaAlertaEmail(String id, String incidenteId, String destino, String assunto, String mensagem, LocalDateTime agora) {
        this.id=id; this.incidenteId=incidenteId; this.destino=destino; this.assunto=assunto; this.mensagem=mensagem;
        this.status="PENDENTE"; this.tentativas=0; this.criadaEm=agora; this.proximaTentativaEm=agora;
    }
    public void registrarFalha(String erro, LocalDateTime agora, int maxTentativas) {
        ultimaTentativaEm=agora; reservadoAte=null; ultimoErro=erro == null ? "Erro não informado" : erro.substring(0, Math.min(erro.length(),1200));
        if (tentativas >= maxTentativas) status="FALHA_FINAL";
        else { status="PENDENTE"; proximaTentativaEm=agora.plusMinutes((long)Math.pow(2, tentativas)); }
    }
    public void reabrirParaReenvio(LocalDateTime agora) { status="PENDENTE"; reservadoAte=null; tentativas=0; proximaTentativaEm=agora; ultimaTentativaEm=null; enviadaEm=null; ultimoErro=null; }
    public void registrarSucesso(LocalDateTime agora) { ultimaTentativaEm=agora; enviadaEm=agora; status="ENVIADO"; reservadoAte=null; ultimoErro=null; }
    public void recuperarReserva(LocalDateTime agora, int maxTentativas) { reservadoAte=null; if (tentativas >= maxTentativas) { status="FALHA_FINAL"; ultimoErro="Processamento interrompido; reserva expirou após falha da instância."; } else { status="PENDENTE"; proximaTentativaEm=agora; ultimoErro="Processamento interrompido; mensagem devolvida à fila."; } }
    public String getId(){return id;} public String getIncidenteId(){return incidenteId;} public String getDestino(){return destino;}
    public String getAssunto(){return assunto;} public String getMensagem(){return mensagem;} public String getStatus(){return status;}
    public int getTentativas(){return tentativas;} public LocalDateTime getCriadaEm(){return criadaEm;} public LocalDateTime getProximaTentativaEm(){return proximaTentativaEm;}
    public LocalDateTime getUltimaTentativaEm(){return ultimaTentativaEm;} public LocalDateTime getEnviadaEm(){return enviadaEm;} public String getUltimoErro(){return ultimoErro;} public LocalDateTime getReservadoAte(){return reservadoAte;}
}
