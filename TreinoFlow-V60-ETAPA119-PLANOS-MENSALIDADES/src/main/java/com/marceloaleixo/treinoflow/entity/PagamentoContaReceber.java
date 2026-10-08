package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.FormaPagamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contas_receber_pagamentos", indexes = {
        @Index(name = "idx_pagamento_conta_receber", columnList = "conta_receber_id"),
        @Index(name = "idx_pagamento_forma", columnList = "forma_pagamento")
})
public class PagamentoContaReceber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_receber_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pagamento_conta_receber"))
    private ContaReceber contaReceber;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @Column(length = 300)
    private String observacao;

    public Long getId() { return id; }
    public ContaReceber getContaReceber() { return contaReceber; }
    public void setContaReceber(ContaReceber contaReceber) { this.contaReceber = contaReceber; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
