package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "execucoes_exercicios",
       uniqueConstraints = @UniqueConstraint(name = "uk_execucao_exercicio_registro_item",
               columnNames = {"registro_id", "treino_exercicio_id"}),
       indexes = {
           @Index(name = "idx_execucao_exercicio_registro", columnList = "registro_id"),
           @Index(name = "idx_execucao_exercicio_item", columnList = "treino_exercicio_id")
       })
public class ExecucaoExercicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registro_id", nullable = false, foreignKey = @ForeignKey(name = "fk_execucao_exercicio_registro"))
    private RegistroTreinoAluno registro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_exercicio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_execucao_exercicio_item"))
    private TreinoExercicio treinoExercicio;

    @Column(name = "carga_realizada", length = 40)
    private String cargaRealizada;

    @Column(name = "repeticoes_realizadas")
    private Integer repeticoesRealizadas;

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
    public String getCargaRealizada(){return cargaRealizada;} public void setCargaRealizada(String cargaRealizada){this.cargaRealizada=cargaRealizada;}
    public Integer getRepeticoesRealizadas(){return repeticoesRealizadas;} public void setRepeticoesRealizadas(Integer repeticoesRealizadas){this.repeticoesRealizadas=repeticoesRealizadas;}
    public String getObservacao(){return observacao;} public void setObservacao(String observacao){this.observacao=observacao;}
    public boolean isConcluido(){return concluido;} public void setConcluido(boolean concluido){this.concluido=concluido;}
    public LocalDateTime getDataRegistro(){return dataRegistro;} public void setDataRegistro(LocalDateTime dataRegistro){this.dataRegistro=dataRegistro;}
}
