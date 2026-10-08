package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.StatusAssinatura;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="assinaturas_treinoflow", indexes={@Index(name="idx_assinatura_status", columnList="status"), @Index(name="idx_assinatura_vencimento", columnList="data_vencimento")})
public class Assinatura {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="personal_id", nullable=false, unique=true) private UsuarioPersonal personal;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="plano_id", nullable=false) private Plano plano;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private StatusAssinatura status=StatusAssinatura.ATIVA;
    @Column(name="data_inicio", nullable=false) private LocalDate dataInicio;
    @Column(name="data_vencimento") private LocalDate dataVencimento;
    @Column(name="data_proxima_cobranca") private LocalDate dataProximaCobranca;
    @Column(name="data_cancelamento") private LocalDate dataCancelamento;
    @Column(name="observacao", length=500) private String observacao;
    @Column(name="data_atualizacao", nullable=false) private LocalDateTime dataAtualizacao;
    @PrePersist @PreUpdate private void atualizar(){dataAtualizacao=LocalDateTime.now(); if(dataInicio==null)dataInicio=LocalDate.now();}
    public Long getId(){return id;} public UsuarioPersonal getPersonal(){return personal;} public void setPersonal(UsuarioPersonal v){personal=v;}
    public Plano getPlano(){return plano;} public void setPlano(Plano v){plano=v;} public StatusAssinatura getStatus(){return status;} public void setStatus(StatusAssinatura v){status=v;}
    public LocalDate getDataInicio(){return dataInicio;} public void setDataInicio(LocalDate v){dataInicio=v;} public LocalDate getDataVencimento(){return dataVencimento;} public void setDataVencimento(LocalDate v){dataVencimento=v;}
    public LocalDate getDataProximaCobranca(){return dataProximaCobranca;} public void setDataProximaCobranca(LocalDate v){dataProximaCobranca=v;} public LocalDate getDataCancelamento(){return dataCancelamento;} public void setDataCancelamento(LocalDate v){dataCancelamento=v;}
    public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;} public LocalDateTime getDataAtualizacao(){return dataAtualizacao;}
    public boolean permiteAcesso(){return status==StatusAssinatura.ATIVA || status==StatusAssinatura.EM_TESTE;}
    public boolean estaVencida(){return dataVencimento!=null && dataVencimento.isBefore(LocalDate.now()) && (status==StatusAssinatura.ATIVA || status==StatusAssinatura.EM_TESTE);}
}
