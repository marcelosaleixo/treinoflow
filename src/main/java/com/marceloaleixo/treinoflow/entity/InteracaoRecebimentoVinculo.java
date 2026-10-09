package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Vincula uma ação de CRM a um recebimento real, sem duplicar o lançamento financeiro. */
@Entity
@Table(name = "crm_recebimentos_vinculos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_crm_recebimento_vinculo", columnNames = {"interacao_crm_id", "pagamento_id"})
}, indexes = {
        @Index(name = "idx_crm_recebimento_interacao", columnList = "interacao_crm_id"),
        @Index(name = "idx_crm_recebimento_pagamento", columnList = "pagamento_id")
})
public class InteracaoRecebimentoVinculo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interacao_crm_id", nullable = false, foreignKey = @ForeignKey(name = "fk_crm_vinculo_interacao"))
    private InteracaoCrm interacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pagamento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_crm_vinculo_pagamento"))
    private PagamentoContaReceber pagamento;

    @Column(name = "data_vinculo", nullable = false, updatable = false)
    private LocalDateTime dataVinculo;

    @Column(length = 300)
    private String observacao;

    @PrePersist void prePersist() { if (dataVinculo == null) dataVinculo = LocalDateTime.now(); }
    public Long getId() { return id; }
    public InteracaoCrm getInteracao() { return interacao; }
    public void setInteracao(InteracaoCrm interacao) { this.interacao = interacao; }
    public PagamentoContaReceber getPagamento() { return pagamento; }
    public void setPagamento(PagamentoContaReceber pagamento) { this.pagamento = pagamento; }
    public LocalDateTime getDataVinculo() { return dataVinculo; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
