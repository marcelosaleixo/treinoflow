package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidentes_operacionais_notificacao", indexes = {
        @Index(name = "idx_incidente_notificacao_status_data", columnList = "resolvido, ocorrido_em"),
        @Index(name = "idx_incidente_notificacao_codigo_status", columnList = "codigo, resolvido")
})
public class IncidenteOperacionalNotificacao {
    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 20)
    private String severidade;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(nullable = false, length = 1200)
    private String detalhe;

    @Column(name = "ocorrido_em", nullable = false, updatable = false)
    private LocalDateTime ocorridoEm;

    @Column(nullable = false)
    private boolean resolvido;

    @Column(name = "resolvido_em")
    private LocalDateTime resolvidoEm;

    protected IncidenteOperacionalNotificacao() { }

    public IncidenteOperacionalNotificacao(String id, String codigo, String severidade, String titulo,
                                           String detalhe, LocalDateTime ocorridoEm) {
        this.id = id;
        this.codigo = codigo;
        this.severidade = severidade;
        this.titulo = titulo;
        this.detalhe = detalhe;
        this.ocorridoEm = ocorridoEm;
        this.resolvido = false;
    }

    public void resolver(LocalDateTime data) {
        if (!resolvido) {
            resolvido = true;
            resolvidoEm = data;
        }
    }

    public String getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getSeveridade() { return severidade; }
    public String getTitulo() { return titulo; }
    public String getDetalhe() { return detalhe; }
    public LocalDateTime getOcorridoEm() { return ocorridoEm; }
    public boolean isResolvido() { return resolvido; }
    public LocalDateTime getResolvidoEm() { return resolvidoEm; }
}
