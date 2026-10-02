package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import com.marceloaleixo.treinoflow.enums.GrupoMuscular;

@Entity
@Table(name = "exercicios", indexes = @Index(name = "idx_exercicio_personal_nome", columnList = "personal_id,nome"))
public class Exercicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", foreignKey = @ForeignKey(name = "fk_exercicio_personal"))
    private UsuarioPersonal personal; // null = exercício global do sistema
    @Column(nullable = false, length = 120)
    private String nome;
    @Enumerated(EnumType.STRING)
    @Column(name = "grupo_muscular", length = 40)
    private GrupoMuscular grupoMuscular;
    @Column(columnDefinition = "text")
    private String descricao;
    @Column(name = "url_video", length = 500)
    private String urlVideo;

    public Exercicio() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public UsuarioPersonal getPersonal(){return personal;} public void setPersonal(UsuarioPersonal personal){this.personal=personal;}
    public String getNome(){return nome;} public void setNome(String nome){this.nome=nome;}
    public GrupoMuscular getGrupoMuscular(){return grupoMuscular;} public void setGrupoMuscular(GrupoMuscular grupoMuscular){this.grupoMuscular=grupoMuscular;}
    public String getDescricao(){return descricao;} public void setDescricao(String descricao){this.descricao=descricao;}
    public String getUrlVideo(){return urlVideo;} public void setUrlVideo(String urlVideo){this.urlVideo=urlVideo;}
}
