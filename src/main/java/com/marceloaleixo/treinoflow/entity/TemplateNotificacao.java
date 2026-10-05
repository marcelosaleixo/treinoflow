package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.CanalNotificacao;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "templates_notificacao",
        uniqueConstraints = @UniqueConstraint(name = "uk_template_tipo_canal", columnNames = {"tipo", "canal"}),
        indexes = @Index(name = "idx_template_tipo", columnList = "tipo"))
public class TemplateNotificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalNotificacao canal;

    @Column(nullable = false, length = 140)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @PrePersist
    void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        if (dataCriacao == null) dataCriacao = agora;
        dataAtualizacao = agora;
    }

    @PreUpdate
    void preUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }
    public CanalNotificacao getCanal() { return canal; }
    public void setCanal(CanalNotificacao canal) { this.canal = canal; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
}
