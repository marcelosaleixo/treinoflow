package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "modelos_treino", indexes = @Index(name = "idx_modelo_treino_personal_nome", columnList = "personal_id,nome"))
public class ModeloTreino {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_modelo_treino_personal"))
    private UsuarioPersonal personal;
    @Column(nullable = false, length = 80) private String nome;
    @Column(columnDefinition = "text") private String descricao;
    @Column(name = "data_criacao", nullable = false, updatable = false) private LocalDateTime dataCriacao;
    @PrePersist protected void prePersist(){ if(dataCriacao==null) dataCriacao=LocalDateTime.now(); }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public UsuarioPersonal getPersonal(){return personal;} public void setPersonal(UsuarioPersonal personal){this.personal=personal;}
    public String getNome(){return nome;} public void setNome(String nome){this.nome=nome;}
    public String getDescricao(){return descricao;} public void setDescricao(String descricao){this.descricao=descricao;}
    public LocalDateTime getDataCriacao(){return dataCriacao;} public void setDataCriacao(LocalDateTime dataCriacao){this.dataCriacao=dataCriacao;}
}
