package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescricoes_auditoria", indexes = {
        @Index(name = "idx_prescricao_auditoria_aluno_data", columnList = "aluno_id,data_decisao"),
        @Index(name = "idx_prescricao_auditoria_personal_data", columnList = "personal_id,data_decisao")
})
public class PrescricaoAuditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_auditoria_personal"))
    private UsuarioPersonal personal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false, foreignKey = @ForeignKey(name = "fk_auditoria_aluno"))
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_id", nullable = false, foreignKey = @ForeignKey(name = "fk_auditoria_treino"))
    private Treino treino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_exercicio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_auditoria_treino_exercicio"))
    private TreinoExercicio treinoExercicio;

    @Column(name = "exercicio_nome", nullable = false, length = 160)
    private String exercicioNome;
    @Column(name = "nivel_ia", nullable = false, length = 30)
    private String nivelIa;
    @Column(name = "confianca_ia", nullable = false, length = 30)
    private String confiancaIa;
    @Column(name = "justificativa", columnDefinition = "text")
    private String justificativa;

    @Column(name = "series_antes", length = 30) private String seriesAntes;
    @Column(name = "repeticoes_antes", length = 30) private String repeticoesAntes;
    @Column(name = "carga_antes", length = 30) private String cargaAntes;
    @Column(name = "series_depois", length = 30) private String seriesDepois;
    @Column(name = "repeticoes_depois", length = 30) private String repeticoesDepois;
    @Column(name = "carga_depois", length = 30) private String cargaDepois;
    @Column(name = "decisao", nullable = false, length = 30) private String decisao;

    /** Feedback posterior do Personal sobre a recomendação aplicada. */
    @Column(name = "feedback_decisao", length = 30) private String feedbackDecisao;
    @Column(name = "feedback_motivo", columnDefinition = "text") private String feedbackMotivo;
    @Column(name = "feedback_data") private LocalDateTime feedbackData;

    @Column(name = "data_decisao", nullable = false, updatable = false) private LocalDateTime dataDecisao;

    public PrescricaoAuditoria() {}
    @PrePersist protected void prePersist(){ if(dataDecisao == null) dataDecisao = LocalDateTime.now(); }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public UsuarioPersonal getPersonal(){return personal;} public void setPersonal(UsuarioPersonal personal){this.personal=personal;}
    public Aluno getAluno(){return aluno;} public void setAluno(Aluno aluno){this.aluno=aluno;}
    public Treino getTreino(){return treino;} public void setTreino(Treino treino){this.treino=treino;}
    public TreinoExercicio getTreinoExercicio(){return treinoExercicio;} public void setTreinoExercicio(TreinoExercicio v){this.treinoExercicio=v;}
    public String getExercicioNome(){return exercicioNome;} public void setExercicioNome(String v){this.exercicioNome=v;}
    public String getNivelIa(){return nivelIa;} public void setNivelIa(String v){this.nivelIa=v;}
    public String getConfiancaIa(){return confiancaIa;} public void setConfiancaIa(String v){this.confiancaIa=v;}
    public String getJustificativa(){return justificativa;} public void setJustificativa(String v){this.justificativa=v;}
    public String getSeriesAntes(){return seriesAntes;} public void setSeriesAntes(String v){this.seriesAntes=v;}
    public String getRepeticoesAntes(){return repeticoesAntes;} public void setRepeticoesAntes(String v){this.repeticoesAntes=v;}
    public String getCargaAntes(){return cargaAntes;} public void setCargaAntes(String v){this.cargaAntes=v;}
    public String getSeriesDepois(){return seriesDepois;} public void setSeriesDepois(String v){this.seriesDepois=v;}
    public String getRepeticoesDepois(){return repeticoesDepois;} public void setRepeticoesDepois(String v){this.repeticoesDepois=v;}
    public String getCargaDepois(){return cargaDepois;} public void setCargaDepois(String v){this.cargaDepois=v;}
    public String getDecisao(){return decisao;} public void setDecisao(String v){this.decisao=v;}
    public String getFeedbackDecisao(){return feedbackDecisao;} public void setFeedbackDecisao(String v){this.feedbackDecisao=v;}
    public String getFeedbackMotivo(){return feedbackMotivo;} public void setFeedbackMotivo(String v){this.feedbackMotivo=v;}
    public LocalDateTime getFeedbackData(){return feedbackData;} public void setFeedbackData(LocalDateTime v){this.feedbackData=v;}
    public LocalDateTime getDataDecisao(){return dataDecisao;} public void setDataDecisao(LocalDateTime v){this.dataDecisao=v;}
}
