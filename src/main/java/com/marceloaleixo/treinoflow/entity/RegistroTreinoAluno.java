package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_treino_aluno",
       indexes = {
           @Index(name = "idx_registro_aluno_data", columnList = "aluno_id,data_execucao"),
           @Index(name = "idx_registro_treino_data", columnList = "treino_id,data_execucao")
       },
       uniqueConstraints = @UniqueConstraint(name = "uk_registro_treino_data", columnNames = {"treino_id", "data_execucao"}))
public class RegistroTreinoAluno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_id", nullable = false, foreignKey = @ForeignKey(name = "fk_registro_treino"))
    private Treino treino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false, foreignKey = @ForeignKey(name = "fk_registro_aluno"))
    private Aluno aluno;

    @Column(name = "data_execucao", nullable = false)
    private LocalDate dataExecucao;

    @Column(nullable = false)
    private boolean concluido = true;

    @Column
    private Integer nota;

    @Column(columnDefinition = "text")
    private String feedback;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataRegistro;

    @PrePersist
    protected void prePersist() {
        if (dataRegistro == null) dataRegistro = LocalDateTime.now();
        if (dataExecucao == null) dataExecucao = LocalDate.now();
    }

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Treino getTreino(){return treino;} public void setTreino(Treino treino){this.treino=treino;}
    public Aluno getAluno(){return aluno;} public void setAluno(Aluno aluno){this.aluno=aluno;}
    public LocalDate getDataExecucao(){return dataExecucao;} public void setDataExecucao(LocalDate dataExecucao){this.dataExecucao=dataExecucao;}
    public boolean isConcluido(){return concluido;} public void setConcluido(boolean concluido){this.concluido=concluido;}
    public Integer getNota(){return nota;} public void setNota(Integer nota){this.nota=nota;}
    public String getFeedback(){return feedback;} public void setFeedback(String feedback){this.feedback=feedback;}
    public LocalDateTime getDataRegistro(){return dataRegistro;} public void setDataRegistro(LocalDateTime dataRegistro){this.dataRegistro=dataRegistro;}
}
