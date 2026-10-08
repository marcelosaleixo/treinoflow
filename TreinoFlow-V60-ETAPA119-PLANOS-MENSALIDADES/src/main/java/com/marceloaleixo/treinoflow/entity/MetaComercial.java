package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "metas_comerciais",
       uniqueConstraints = @UniqueConstraint(name = "uk_meta_comercial_personal_mes", columnNames = {"personal_id", "mes_referencia"}))
public class MetaComercial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_meta_comercial_personal"))
    private UsuarioPersonal personal;

    @Column(name = "mes_referencia", nullable = false)
    private LocalDate mesReferencia;

    @Column(name = "meta_alunos_ativos", nullable = false)
    private int metaAlunosAtivos = 20;

    @Column(name = "meta_treinos", nullable = false)
    private int metaTreinos = 80;

    @Column(name = "meta_recuperacoes", nullable = false)
    private int metaRecuperacoes = 5;

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public LocalDate getMesReferencia() { return mesReferencia; }
    public void setMesReferencia(LocalDate mesReferencia) { this.mesReferencia = mesReferencia; }
    public int getMetaAlunosAtivos() { return metaAlunosAtivos; }
    public void setMetaAlunosAtivos(int v) { this.metaAlunosAtivos = v; }
    public int getMetaTreinos() { return metaTreinos; }
    public void setMetaTreinos(int v) { this.metaTreinos = v; }
    public int getMetaRecuperacoes() { return metaRecuperacoes; }
    public void setMetaRecuperacoes(int v) { this.metaRecuperacoes = v; }
}
