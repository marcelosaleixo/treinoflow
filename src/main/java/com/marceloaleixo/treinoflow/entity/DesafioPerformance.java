package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.TipoDesafioPerformance;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "desafios_performance",
       uniqueConstraints = @UniqueConstraint(name = "uk_desafio_performance_personal_mes_tipo",
               columnNames = {"personal_id", "mes_referencia", "tipo"}))
public class DesafioPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_desafio_performance_personal"))
    private UsuarioPersonal personal;

    @Column(name = "mes_referencia", nullable = false)
    private LocalDate mesReferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoDesafioPerformance tipo;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "meta", nullable = false)
    private int meta;

    @Column(name = "bonus_pontos", nullable = false)
    private int bonusPontos;

    @Column(name = "concluido_em")
    private LocalDateTime concluidoEm;

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public LocalDate getMesReferencia() { return mesReferencia; }
    public void setMesReferencia(LocalDate mesReferencia) { this.mesReferencia = mesReferencia; }
    public TipoDesafioPerformance getTipo() { return tipo; }
    public void setTipo(TipoDesafioPerformance tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public int getMeta() { return meta; }
    public void setMeta(int meta) { this.meta = meta; }
    public int getBonusPontos() { return bonusPontos; }
    public void setBonusPontos(int bonusPontos) { this.bonusPontos = bonusPontos; }
    public LocalDateTime getConcluidoEm() { return concluidoEm; }
    public void setConcluidoEm(LocalDateTime concluidoEm) { this.concluidoEm = concluidoEm; }
}
