package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.StatusCobranca;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cobrancas_treinoflow",
       uniqueConstraints = @UniqueConstraint(name = "uk_cobranca_assinatura_competencia", columnNames = {"assinatura_id", "competencia"}),
       indexes = {
           @Index(name = "idx_cobranca_status", columnList = "status"),
           @Index(name = "idx_cobranca_vencimento", columnList = "data_vencimento")
       })
public class Cobranca {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assinatura_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cobranca_assinatura"))
    private Assinatura assinatura;

    @Column(name = "competencia", nullable = false)
    private LocalDate competencia;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCobranca status = StatusCobranca.PENDENTE;

    @Column(length = 500)
    private String observacao;

    @Column(name = "gateway", length = 30)
    private String gateway;

    @Column(name = "external_payment_id", length = 100)
    private String externalPaymentId;

    @Column(name = "external_reference", length = 120)
    private String externalReference;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "qr_code_base64", columnDefinition = "TEXT")
    private String qrCodeBase64;

    @Column(name = "payment_url", columnDefinition = "TEXT")
    private String paymentUrl;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @PrePersist
    void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (status == null) status = StatusCobranca.PENDENTE;
    }

    @PreUpdate
    void preUpdate() { dataAtualizacao = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Assinatura getAssinatura() { return assinatura; }
    public void setAssinatura(Assinatura assinatura) { this.assinatura = assinatura; }
    public LocalDate getCompetencia() { return competencia; }
    public void setCompetencia(LocalDate competencia) { this.competencia = competencia; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    public StatusCobranca getStatus() { return status; }
    public void setStatus(StatusCobranca status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }
    public String getExternalPaymentId() { return externalPaymentId; }
    public void setExternalPaymentId(String externalPaymentId) { this.externalPaymentId = externalPaymentId; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
}
