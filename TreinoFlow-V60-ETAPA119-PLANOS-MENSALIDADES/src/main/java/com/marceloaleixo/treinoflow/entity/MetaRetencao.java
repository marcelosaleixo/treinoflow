package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "metas_retencao",
       uniqueConstraints = @UniqueConstraint(name = "uk_meta_retencao_personal_mes", columnNames = {"personal_id", "mes_referencia"}))
public class MetaRetencao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_meta_retencao_personal"))
    private UsuarioPersonal personal;

    @Column(name = "mes_referencia", nullable = false)
    private LocalDate mesReferencia;

    @Column(name = "meta_recuperacoes", nullable = false)
    private int metaRecuperacoes = 5;

    @Column(name = "meta_taxa", nullable = false)
    private double metaTaxa = 60D;

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public LocalDate getMesReferencia() { return mesReferencia; }
    public void setMesReferencia(LocalDate mesReferencia) { this.mesReferencia = mesReferencia; }
    public int getMetaRecuperacoes() { return metaRecuperacoes; }
    public void setMetaRecuperacoes(int metaRecuperacoes) { this.metaRecuperacoes = metaRecuperacoes; }
    public double getMetaTaxa() { return metaTaxa; }
    public void setMetaTaxa(double metaTaxa) { this.metaTaxa = metaTaxa; }
}
