package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes_treinoflow",
       uniqueConstraints = @UniqueConstraint(name = "uk_notificacao_chave", columnNames = "chave_unica"),
       indexes = {
           @Index(name = "idx_notificacao_personal_lida", columnList = "personal_id, lida"),
           @Index(name = "idx_notificacao_criacao", columnList = "data_criacao")
       })
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private UsuarioPersonal personal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cobranca_id")
    private Cobranca cobranca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacao tipo;

    @Column(nullable = false, length = 140)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(nullable = false)
    private boolean lida = false;

    @Column(name = "chave_unica", nullable = false, length = 180)
    private String chaveUnica;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_leitura")
    private LocalDateTime dataLeitura;

    @PrePersist
    void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public Cobranca getCobranca() { return cobranca; }
    public void setCobranca(Cobranca cobranca) { this.cobranca = cobranca; }
    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
    public String getChaveUnica() { return chaveUnica; }
    public void setChaveUnica(String chaveUnica) { this.chaveUnica = chaveUnica; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataLeitura() { return dataLeitura; }
    public void marcarComoLida() {
        this.lida = true;
        this.dataLeitura = LocalDateTime.now();
    }
}
