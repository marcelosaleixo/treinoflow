package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.FormaPagamento;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contas_receber_personal",
       indexes = {
           @Index(name = "idx_conta_receber_personal_status", columnList = "personal_id,status"),
           @Index(name = "idx_conta_receber_personal_vencimento", columnList = "personal_id,data_vencimento")
       })
public class ContaReceber {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_conta_receber_personal"))
    private UsuarioPersonal personal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", foreignKey = @ForeignKey(name = "fk_conta_receber_aluno"))
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_mensalidade_id", foreignKey = @ForeignKey(name = "fk_conta_receber_plano_mensalidade"))
    private PlanoMensalidade planoMensalidade;

    @Column(nullable = false, length = 160)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusContaReceber status = StatusContaReceber.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", length = 30)
    private FormaPagamento formaPagamento;

    @Column(length = 500)
    private String observacao;

    /** Pagamentos que compõem a quitação da conta. Permite pagamento dividido em múltiplas formas. */
    @OneToMany(mappedBy = "contaReceber", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<PagamentoContaReceber> pagamentos = new ArrayList<>();

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @PrePersist
    void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (status == null) status = StatusContaReceber.PENDENTE;
    }

    @PreUpdate
    void preUpdate() { dataAtualizacao = LocalDateTime.now(); }

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public PlanoMensalidade getPlanoMensalidade() { return planoMensalidade; }
    public void setPlanoMensalidade(PlanoMensalidade planoMensalidade) { this.planoMensalidade = planoMensalidade; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }
    public StatusContaReceber getStatus() { return status; }
    public void setStatus(StatusContaReceber status) { this.status = status; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public List<PagamentoContaReceber> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<PagamentoContaReceber> pagamentos) { this.pagamentos = pagamentos; }

    public void adicionarPagamento(PagamentoContaReceber pagamento) {
        pagamento.setContaReceber(this);
        this.pagamentos.add(pagamento);
    }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
}
