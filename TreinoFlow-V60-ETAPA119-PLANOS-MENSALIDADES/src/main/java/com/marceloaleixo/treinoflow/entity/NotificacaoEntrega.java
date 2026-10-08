package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.CanalNotificacao;
import com.marceloaleixo.treinoflow.enums.StatusEntregaNotificacao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes_entregas",
        uniqueConstraints = @UniqueConstraint(name = "uk_notificacao_canal", columnNames = {"notificacao_id", "canal"}),
        indexes = {
                @Index(name = "idx_entrega_status", columnList = "status"),
                @Index(name = "idx_entrega_tentativa", columnList = "proxima_tentativa")
        })
public class NotificacaoEntrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notificacao_id", nullable = false)
    private Notificacao notificacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalNotificacao canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEntregaNotificacao status = StatusEntregaNotificacao.PENDENTE;

    @Column(name = "tentativas", nullable = false)
    private int tentativas;

    @Column(name = "destino", length = 180)
    private String destino;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "proxima_tentativa")
    private LocalDateTime proximaTentativa;

    @Column(name = "ultimo_erro", length = 1000)
    private String ultimoErro;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
        if (proximaTentativa == null) proximaTentativa = dataCriacao;
    }

    public Long getId() { return id; }
    public Notificacao getNotificacao() { return notificacao; }
    public void setNotificacao(Notificacao notificacao) { this.notificacao = notificacao; }
    public CanalNotificacao getCanal() { return canal; }
    public void setCanal(CanalNotificacao canal) { this.canal = canal; }
    public StatusEntregaNotificacao getStatus() { return status; }
    public void setStatus(StatusEntregaNotificacao status) { this.status = status; }
    public int getTentativas() { return tentativas; }
    public void setTentativas(int tentativas) { this.tentativas = tentativas; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }
    public LocalDateTime getProximaTentativa() { return proximaTentativa; }
    public void setProximaTentativa(LocalDateTime proximaTentativa) { this.proximaTentativa = proximaTentativa; }
    public String getUltimoErro() { return ultimoErro; }
    public void setUltimoErro(String ultimoErro) { this.ultimoErro = ultimoErro; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}
