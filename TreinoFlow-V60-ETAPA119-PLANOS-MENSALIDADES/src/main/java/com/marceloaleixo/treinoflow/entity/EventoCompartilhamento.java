package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_compartilhamento", indexes = @Index(name = "idx_evento_treino_data", columnList = "treino_id,data_evento"))
public class EventoCompartilhamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treino_id", nullable = false)
    private Treino treino;
    @Column(name = "tipo_evento", nullable = false, length = 20)
    private String tipoEvento;
    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;
    @Column(name = "detalhe", length = 250)
    private String detalhe;
    protected EventoCompartilhamento() {}
    public EventoCompartilhamento(Treino treino, String tipoEvento, LocalDateTime dataEvento, String detalhe) {
        this.treino = treino; this.tipoEvento = tipoEvento; this.dataEvento = dataEvento; this.detalhe = detalhe;
    }
    public Long getId(){return id;} public Treino getTreino(){return treino;}
    public String getTipoEvento(){return tipoEvento;} public LocalDateTime getDataEvento(){return dataEvento;}
    public String getDetalhe(){return detalhe;}
}
