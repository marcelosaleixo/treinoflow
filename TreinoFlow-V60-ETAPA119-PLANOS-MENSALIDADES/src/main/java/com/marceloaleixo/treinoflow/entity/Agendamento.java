package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos",
        indexes = {
                @Index(name = "idx_agendamento_personal_inicio", columnList = "personal_id,inicio"),
                @Index(name = "idx_agendamento_aluno_inicio", columnList = "aluno_id,inicio")
        })
public class Agendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_agendamento_personal"))
    private UsuarioPersonal personal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_agendamento_aluno"))
    private Aluno aluno;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Column(nullable = false, length = 20)
    private String status = "AGENDADO";

    @Column(nullable = false, length = 30)
    private String tipo = "TREINO";

    @Column(columnDefinition = "text")
    private String observacoes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public Agendamento() {}

    @PrePersist
    protected void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
        if (status == null || status.isBlank()) status = "AGENDADO";
        if (tipo == null || tipo.isBlank()) tipo = "TREINO";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public LocalDateTime getFim() { return fim; }
    public void setFim(LocalDateTime fim) { this.fim = fim; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
