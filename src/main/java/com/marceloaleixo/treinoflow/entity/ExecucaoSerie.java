package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Registro individual de cada série executada pelo aluno. */
@Entity
@Table(name = "execucoes_series",
       uniqueConstraints = @UniqueConstraint(name = "uk_execucao_serie_registro_item_numero",
               columnNames = {"registro_id", "treino_exercicio_id", "numero_serie"}),
       indexes = {
           @Index(name = "idx_execucao_serie_registro", columnList = "registro_id"),
           @Index(name = "idx_execucao_serie_item", columnList = "treino_exercicio_id")
       })
public class ExecucaoSerie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registro_id", nullable = false, foreignKey = @ForeignKey(name = "fk_execucao_serie_registro"))
    private RegistroTreinoAluno registro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_exercicio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_execucao_serie_item"))
    private TreinoExercicio treinoExercicio;

    @Column(name = "numero_serie", nullable = false)
    private Integer numeroSerie;

    @Column(name = "carga_realizada", length = 40)
    private String cargaRealizada;

    @Column(name = "repeticoes_realizadas")
    private Integer repeticoesRealizadas;

    @Column(name = "rpe")
    private Integer rpe;

    @Column(columnDefinition = "text")
    private String observacao;

    @Column(nullable = false)
    private boolean concluido = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataRegistro;

    @PrePersist
    protected void prePersist() {
        if (dataRegistro == null) dataRegistro = LocalDateTime.now();
    }

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public RegistroTreinoAluno getRegistro(){return registro;} public void setRegistro(RegistroTreinoAluno registro){this.registro=registro;}
    public TreinoExercicio getTreinoExercicio(){return treinoExercicio;} public void setTreinoExercicio(TreinoExercicio treinoExercicio){this.treinoExercicio=treinoExercicio;}
    public Integer getNumeroSerie(){return numeroSerie;} public void setNumeroSerie(Integer numeroSerie){this.numeroSerie=numeroSerie;}
    public String getCargaRealizada(){return cargaRealizada;} public void setCargaRealizada(String cargaRealizada){this.cargaRealizada=cargaRealizada;}
    public Integer getRepeticoesRealizadas(){return repeticoesRealizadas;} public void setRepeticoesRealizadas(Integer repeticoesRealizadas){this.repeticoesRealizadas=repeticoesRealizadas;}
    public Integer getRpe(){return rpe;} public void setRpe(Integer rpe){this.rpe=rpe;}
    public String getObservacao(){return observacao;} public void setObservacao(String observacao){this.observacao=observacao;}
    public boolean isConcluido(){return concluido;} public void setConcluido(boolean concluido){this.concluido=concluido;}
    public LocalDateTime getDataRegistro(){return dataRegistro;} public void setDataRegistro(LocalDateTime dataRegistro){this.dataRegistro=dataRegistro;}
}
