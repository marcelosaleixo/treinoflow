package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="metas_financeiras_personal", uniqueConstraints=@UniqueConstraint(name="uk_meta_financeira_personal_mes", columnNames={"personal_id","mes_referencia"}))
public class MetaFinanceira {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="personal_id",nullable=false) private UsuarioPersonal personal;
 @Column(name="mes_referencia",nullable=false) private LocalDate mesReferencia;
 @Column(name="meta_receita",nullable=false,precision=12,scale=2) private BigDecimal metaReceita=BigDecimal.ZERO;
 @Column(name="limite_despesas",nullable=false,precision=12,scale=2) private BigDecimal limiteDespesas=BigDecimal.ZERO;
 @Column(length=500) private String observacao;
 @Column(name="data_atualizacao",nullable=false) private LocalDateTime dataAtualizacao;
 @PrePersist @PreUpdate void atualizar(){dataAtualizacao=LocalDateTime.now();}
 public Long getId(){return id;} public UsuarioPersonal getPersonal(){return personal;} public void setPersonal(UsuarioPersonal v){personal=v;}
 public LocalDate getMesReferencia(){return mesReferencia;} public void setMesReferencia(LocalDate v){mesReferencia=v;}
 public BigDecimal getMetaReceita(){return metaReceita;} public void setMetaReceita(BigDecimal v){metaReceita=v;}
 public BigDecimal getLimiteDespesas(){return limiteDespesas;} public void setLimiteDespesas(BigDecimal v){limiteDespesas=v;}
 public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;}
}
