package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "treino_exercicios",
       uniqueConstraints = @UniqueConstraint(name = "uk_treino_exercicio_ordem", columnNames = {"treino_id","ordem"}),
       indexes = @Index(name = "idx_treino_exercicio_treino", columnList = "treino_id"))
public class TreinoExercicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_id", nullable = false, foreignKey = @ForeignKey(name = "fk_te_treino"))
    private Treino treino;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercicio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_te_exercicio"))
    private Exercicio exercicio;
    @Column(nullable = false)
    private Integer ordem;
    private Integer series;
    @Column(length = 30)
    private String repeticoes;
    @Column(length = 30)
    private String carga;
    @Column(name = "descanso_segundos")
    private Integer descansoSegundos;
    @Column(columnDefinition = "text")
    private String observacao;

    public TreinoExercicio() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Treino getTreino(){return treino;} public void setTreino(Treino treino){this.treino=treino;}
    public Exercicio getExercicio(){return exercicio;} public void setExercicio(Exercicio exercicio){this.exercicio=exercicio;}
    public Integer getOrdem(){return ordem;} public void setOrdem(Integer ordem){this.ordem=ordem;}
    public Integer getSeries(){return series;} public void setSeries(Integer series){this.series=series;}
    public String getRepeticoes(){return repeticoes;} public void setRepeticoes(String repeticoes){this.repeticoes=repeticoes;}
    public String getCarga(){return carga;} public void setCarga(String carga){this.carga=carga;}
    public Integer getDescansoSegundos(){return descansoSegundos;} public void setDescansoSegundos(Integer descansoSegundos){this.descansoSegundos=descansoSegundos;}
    public String getObservacao(){return observacao;} public void setObservacao(String observacao){this.observacao=observacao;}
}
