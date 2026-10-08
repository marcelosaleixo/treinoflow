package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lembretes_agendamento",
        uniqueConstraints = @UniqueConstraint(name = "uk_lembrete_agendamento_tipo", columnNames = {"agendamento_id", "tipo"}),
        indexes = @Index(name = "idx_lembrete_agendamento_data", columnList = "agendamento_id,tipo"))
public class LembreteAgendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agendamento_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lembrete_agendamento"))
    private Agendamento agendamento;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @Column(length = 30)
    private String canal = "WHATSAPP";

    public Long getId() { return id; }
    public LembreteAgendamento setAgendamento(Agendamento agendamento) { this.agendamento = agendamento; return this; }
    public Agendamento getAgendamento() { return agendamento; }
    public String getTipo() { return tipo; }
    public LembreteAgendamento setTipo(String tipo) { this.tipo = tipo; return this; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public LembreteAgendamento setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; return this; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
}
