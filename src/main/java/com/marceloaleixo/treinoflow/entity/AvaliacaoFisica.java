package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacoes_fisicas", indexes = @Index(name = "idx_avaliacao_aluno_data", columnList = "aluno_id,data_avaliacao"))
public class AvaliacaoFisica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDate dataAvaliacao;
    @Column(name = "peso_kg", precision = 6, scale = 2) private BigDecimal pesoKg;
    @Column(name = "altura_metros", precision = 4, scale = 2) private BigDecimal alturaMetros;
    @Column(name = "percentual_gordura", precision = 5, scale = 2) private BigDecimal percentualGordura;
    @Column(name = "cintura_cm", precision = 6, scale = 2) private BigDecimal cinturaCm;
    @Column(name = "quadril_cm", precision = 6, scale = 2) private BigDecimal quadrilCm;
    @Column(name = "torax_cm", precision = 6, scale = 2) private BigDecimal toraxCm;
    @Column(name = "braco_cm", precision = 6, scale = 2) private BigDecimal bracoCm;
    @Column(name = "coxa_cm", precision = 6, scale = 2) private BigDecimal coxaCm;
    @Column(columnDefinition = "text") private String observacoes;
    @Column(nullable = false, updatable = false) private LocalDateTime dataCriacao;

    public AvaliacaoFisica() {}
    @PrePersist protected void prePersist() { if (dataCriacao == null) dataCriacao = LocalDateTime.now(); if (dataAvaliacao == null) dataAvaliacao = LocalDate.now(); }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Aluno getAluno(){return aluno;} public void setAluno(Aluno aluno){this.aluno=aluno;}
    public LocalDate getDataAvaliacao(){return dataAvaliacao;} public void setDataAvaliacao(LocalDate v){this.dataAvaliacao=v;}
    public BigDecimal getPesoKg(){return pesoKg;} public void setPesoKg(BigDecimal v){this.pesoKg=v;}
    public BigDecimal getAlturaMetros(){return alturaMetros;} public void setAlturaMetros(BigDecimal v){this.alturaMetros=v;}
    public BigDecimal getPercentualGordura(){return percentualGordura;} public void setPercentualGordura(BigDecimal v){this.percentualGordura=v;}
    public BigDecimal getCinturaCm(){return cinturaCm;} public void setCinturaCm(BigDecimal v){this.cinturaCm=v;}
    public BigDecimal getQuadrilCm(){return quadrilCm;} public void setQuadrilCm(BigDecimal v){this.quadrilCm=v;}
    public BigDecimal getToraxCm(){return toraxCm;} public void setToraxCm(BigDecimal v){this.toraxCm=v;}
    public BigDecimal getBracoCm(){return bracoCm;} public void setBracoCm(BigDecimal v){this.bracoCm=v;}
    public BigDecimal getCoxaCm(){return coxaCm;} public void setCoxaCm(BigDecimal v){this.coxaCm=v;}
    public String getObservacoes(){return observacoes;} public void setObservacoes(String v){this.observacoes=v;}
    public LocalDateTime getDataCriacao(){return dataCriacao;} public void setDataCriacao(LocalDateTime v){this.dataCriacao=v;}
}
