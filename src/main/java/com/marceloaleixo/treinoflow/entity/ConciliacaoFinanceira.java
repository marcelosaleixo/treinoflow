package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conciliacoes_financeiras_personal", uniqueConstraints = {
    @UniqueConstraint(name = "uk_conciliacao_pagamento", columnNames = "pagamento_id")
}, indexes = @Index(name = "idx_conciliacao_personal", columnList = "personal_id"))
public class ConciliacaoFinanceira {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private UsuarioPersonal personal;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pagamento_id", nullable = false, unique = true)
    private PagamentoContaReceber pagamento;

    @Column(name = "data_conciliacao", nullable = false)
    private LocalDateTime dataConciliacao;

    @Column(length = 300)
    private String observacao;

    @PrePersist
    void prePersist() { if (dataConciliacao == null) dataConciliacao = LocalDateTime.now(); }

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public PagamentoContaReceber getPagamento() { return pagamento; }
    public void setPagamento(PagamentoContaReceber pagamento) { this.pagamento = pagamento; }
    public LocalDateTime getDataConciliacao() { return dataConciliacao; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
