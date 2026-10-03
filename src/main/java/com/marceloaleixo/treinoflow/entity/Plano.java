package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "planos_treinoflow")
public class Plano {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=80, unique=true)
    private String nome;
    @Column(length=500)
    private String descricao;
    @Column(name="valor_mensal", nullable=false, precision=10, scale=2)
    private BigDecimal valorMensal = BigDecimal.ZERO;
    @Column(name="limite_alunos", nullable=false)
    private Integer limiteAlunos = 10;
    @Column(nullable=false)
    private boolean ativo = true;
    @Column
    private Boolean padrao = false;
    @Column(name="data_criacao", nullable=false, updatable=false)
    private LocalDateTime dataCriacao;
    @PrePersist void criarData(){ if(dataCriacao==null) dataCriacao=LocalDateTime.now(); }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getNome(){return nome;} public void setNome(String nome){this.nome=nome;}
    public String getDescricao(){return descricao;} public void setDescricao(String descricao){this.descricao=descricao;}
    public BigDecimal getValorMensal(){return valorMensal;} public void setValorMensal(BigDecimal valorMensal){this.valorMensal=valorMensal;}
    public Integer getLimiteAlunos(){return limiteAlunos;} public void setLimiteAlunos(Integer limiteAlunos){this.limiteAlunos=limiteAlunos;}
    public boolean isAtivo(){return ativo;} public void setAtivo(boolean ativo){this.ativo=ativo;}
    public boolean isPadrao(){return Boolean.TRUE.equals(padrao);} public void setPadrao(boolean padrao){this.padrao=padrao;}
    public LocalDateTime getDataCriacao(){return dataCriacao;}
}
