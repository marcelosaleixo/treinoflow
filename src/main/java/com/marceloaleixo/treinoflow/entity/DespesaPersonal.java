package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "despesas_personal", indexes = {
        @Index(name = "idx_despesa_personal_data", columnList = "personal_id,data_despesa")
})
public class DespesaPersonal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private UsuarioPersonal personal;

    @Column(nullable = false, length = 140)
    private String descricao;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_despesa", nullable = false)
    private LocalDate dataDespesa;

    @Column(nullable = false)
    private boolean paga = true;

    @Column(length = 500)
    private String observacao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    void prePersist() { if (dataCriacao == null) dataCriacao = LocalDateTime.now(); }

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getDataDespesa() { return dataDespesa; }
    public void setDataDespesa(LocalDate dataDespesa) { this.dataDespesa = dataDespesa; }
    public boolean isPaga() { return paga; }
    public void setPaga(boolean paga) { this.paga = paga; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}
