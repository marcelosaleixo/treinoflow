package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "treinos", indexes = @Index(name = "idx_treino_aluno", columnList = "aluno_id"))
public class Treino {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false, foreignKey = @ForeignKey(name = "fk_treino_aluno"))
    private Aluno aluno;
    @Column(nullable = false, length = 80)
    private String nome;
    @Column(columnDefinition = "text")
    private String descricao;
    @Column(nullable = false, length = 20)
    private String status = "RASCUNHO";
    @Column(name = "token_acesso", unique = true, length = 100)
    private String tokenAcesso;
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
    private LocalDateTime dataLiberacao;
    private LocalDateTime acessoExpiraEm;
    private LocalDateTime dataRevogacao;
    @Column(nullable = false)
    private long totalVisualizacoes = 0;
    private LocalDateTime dataUltimaVisualizacao;

    public Treino() {}
    @PrePersist protected void prePersist(){if(dataCriacao==null)dataCriacao=LocalDateTime.now();if(status==null)status="RASCUNHO";}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Aluno getAluno(){return aluno;} public void setAluno(Aluno aluno){this.aluno=aluno;}
    public String getNome(){return nome;} public void setNome(String nome){this.nome=nome;}
    public String getDescricao(){return descricao;} public void setDescricao(String descricao){this.descricao=descricao;}
    public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
    public String getTokenAcesso(){return tokenAcesso;} public void setTokenAcesso(String tokenAcesso){this.tokenAcesso=tokenAcesso;}
    public LocalDateTime getDataCriacao(){return dataCriacao;} public void setDataCriacao(LocalDateTime dataCriacao){this.dataCriacao=dataCriacao;}
    public LocalDateTime getDataLiberacao(){return dataLiberacao;} public void setDataLiberacao(LocalDateTime dataLiberacao){this.dataLiberacao=dataLiberacao;}
    public LocalDateTime getAcessoExpiraEm(){return acessoExpiraEm;} public void setAcessoExpiraEm(LocalDateTime acessoExpiraEm){this.acessoExpiraEm=acessoExpiraEm;}
    public LocalDateTime getDataRevogacao(){return dataRevogacao;} public void setDataRevogacao(LocalDateTime dataRevogacao){this.dataRevogacao=dataRevogacao;}
    public long getTotalVisualizacoes(){return totalVisualizacoes;} public void setTotalVisualizacoes(long totalVisualizacoes){this.totalVisualizacoes=totalVisualizacoes;}
    public LocalDateTime getDataUltimaVisualizacao(){return dataUltimaVisualizacao;} public void setDataUltimaVisualizacao(LocalDateTime dataUltimaVisualizacao){this.dataUltimaVisualizacao=dataUltimaVisualizacao;}
}
