package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "modelos_treino_exercicios", uniqueConstraints = @UniqueConstraint(name = "uk_modelo_treino_exercicio_ordem", columnNames = {"modelo_id","ordem"}))
public class ModeloTreinoExercicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name="modelo_id", nullable=false, foreignKey=@ForeignKey(name="fk_modelo_exercicio_modelo")) private ModeloTreino modelo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name="exercicio_id", nullable=false, foreignKey=@ForeignKey(name="fk_modelo_exercicio_exercicio")) private Exercicio exercicio;
    @Column(nullable=false) private Integer ordem;
    private Integer series;
    @Column(length=30) private String repeticoes;
    @Column(length=30) private String carga;
    @Column(name="descanso_segundos") private Integer descansoSegundos;
    @Column(columnDefinition="text") private String observacao;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public ModeloTreino getModelo(){return modelo;} public void setModelo(ModeloTreino modelo){this.modelo=modelo;}
    public Exercicio getExercicio(){return exercicio;} public void setExercicio(Exercicio exercicio){this.exercicio=exercicio;}
    public Integer getOrdem(){return ordem;} public void setOrdem(Integer ordem){this.ordem=ordem;}
    public Integer getSeries(){return series;} public void setSeries(Integer series){this.series=series;}
    public String getRepeticoes(){return repeticoes;} public void setRepeticoes(String repeticoes){this.repeticoes=repeticoes;}
    public String getCarga(){return carga;} public void setCarga(String carga){this.carga=carga;}
    public Integer getDescansoSegundos(){return descansoSegundos;} public void setDescansoSegundos(Integer descansoSegundos){this.descansoSegundos=descansoSegundos;}
    public String getObservacao(){return observacao;} public void setObservacao(String observacao){this.observacao=observacao;}
}
