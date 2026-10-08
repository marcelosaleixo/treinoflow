package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "experimentos_retencao", indexes = {
        @Index(name = "idx_exp_ret_personal_status", columnList = "personal_id,status"),
        @Index(name = "idx_exp_ret_faixa_tipo", columnList = "faixa_risco,tipo_acao")
})
public class ExperimentoRetencao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private UsuarioPersonal personal;
    @Column(name = "faixa_risco", nullable = false, length = 20)
    private String faixaRisco;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acao", nullable = false, length = 30)
    private TipoAcaoAssistente tipoAcao;
    @Column(name = "variante_a", nullable = false, columnDefinition = "TEXT")
    private String varianteA;
    @Column(name = "variante_b", nullable = false, columnDefinition = "TEXT")
    private String varianteB;
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ATIVO";
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;
    @Column(name = "encerrado_em")
    private LocalDateTime encerradoEm;
    @PrePersist void prePersist(){ if(criadoEm==null) criadoEm=LocalDateTime.now(); }
    public Long getId(){return id;}
    public UsuarioPersonal getPersonal(){return personal;} public void setPersonal(UsuarioPersonal v){personal=v;}
    public String getFaixaRisco(){return faixaRisco;} public void setFaixaRisco(String v){faixaRisco=v;}
    public TipoAcaoAssistente getTipoAcao(){return tipoAcao;} public void setTipoAcao(TipoAcaoAssistente v){tipoAcao=v;}
    public String getVarianteA(){return varianteA;} public void setVarianteA(String v){varianteA=v;}
    public String getVarianteB(){return varianteB;} public void setVarianteB(String v){varianteB=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public LocalDateTime getCriadoEm(){return criadoEm;}
    public LocalDateTime getEncerradoEm(){return encerradoEm;} public void setEncerradoEm(LocalDateTime v){encerradoEm=v;}
}
