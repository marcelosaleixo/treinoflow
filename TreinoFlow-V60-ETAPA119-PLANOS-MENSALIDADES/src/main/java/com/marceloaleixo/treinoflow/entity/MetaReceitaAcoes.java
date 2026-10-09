package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="metas_receita_acoes_crm", uniqueConstraints=@UniqueConstraint(name="uk_meta_receita_acoes_personal_mes", columnNames={"personal_id","mes_referencia"}))
public class MetaReceitaAcoes {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="personal_id",nullable=false) private UsuarioPersonal personal;
 @Column(name="mes_referencia",nullable=false) private LocalDate mesReferencia;
 @Column(name="valor_meta",nullable=false,precision=12,scale=2) private BigDecimal valorMeta=BigDecimal.ZERO;
 @Column(length=500) private String observacao;
 @Column(name="data_atualizacao",nullable=false) private LocalDateTime dataAtualizacao;
 @PrePersist @PreUpdate void atualizar(){dataAtualizacao=LocalDateTime.now();}
 public Long getId(){return id;} public UsuarioPersonal getPersonal(){return personal;} public void setPersonal(UsuarioPersonal v){personal=v;}
 public LocalDate getMesReferencia(){return mesReferencia;} public void setMesReferencia(LocalDate v){mesReferencia=v;}
 public BigDecimal getValorMeta(){return valorMeta;} public void setValorMeta(BigDecimal v){valorMeta=v;}
 public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;}
}
