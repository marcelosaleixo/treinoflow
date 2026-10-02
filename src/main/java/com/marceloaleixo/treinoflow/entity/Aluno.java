package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alunos", indexes = @Index(name = "idx_aluno_personal_status", columnList = "personal_id,status"))
public class Aluno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_aluno_personal"))
    private UsuarioPersonal personal;

    @Column(nullable = false, length = 120)
    private String nome;
    @Column(length = 150)
    private String email;
    @Column(length = 20)
    private String telefone;
    @Column(length = 100)
    private String objetivo;
    @Column(columnDefinition = "text")
    private String observacoes;
    @Column(nullable = false, length = 20)
    private String status = "ATIVO";
    private LocalDate dataInicio;
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public Aluno() {}
    @PrePersist protected void prePersist() { if (dataCriacao == null) dataCriacao = LocalDateTime.now(); if (status == null) status = "ATIVO"; }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public UsuarioPersonal getPersonal(){return personal;} public void setPersonal(UsuarioPersonal personal){this.personal=personal;}
    public String getNome(){return nome;} public void setNome(String nome){this.nome=nome;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public String getTelefone(){return telefone;} public void setTelefone(String telefone){this.telefone=telefone;}
    public String getObjetivo(){return objetivo;} public void setObjetivo(String objetivo){this.objetivo=objetivo;}
    public String getObservacoes(){return observacoes;} public void setObservacoes(String observacoes){this.observacoes=observacoes;}
    public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
    public LocalDate getDataInicio(){return dataInicio;} public void setDataInicio(LocalDate dataInicio){this.dataInicio=dataInicio;}
    public LocalDateTime getDataCriacao(){return dataCriacao;} public void setDataCriacao(LocalDateTime dataCriacao){this.dataCriacao=dataCriacao;}
}
