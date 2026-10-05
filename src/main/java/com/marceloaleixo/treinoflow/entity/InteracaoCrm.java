package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "interacoes_crm",
       indexes = {
           @Index(name = "idx_interacao_crm_personal_data", columnList = "personal_id,data_contato"),
           @Index(name = "idx_interacao_crm_aluno_data", columnList = "aluno_id,data_contato"),
           @Index(name = "idx_interacao_crm_proxima_acao", columnList = "data_proxima_acao")
       })
public class InteracaoCrm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_interacao_crm_personal"))
    private UsuarioPersonal personal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false, foreignKey = @ForeignKey(name = "fk_interacao_crm_aluno"))
    private Aluno aluno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalCrm canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoInteracaoCrm tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResultadoCrm resultado = ResultadoCrm.EM_ACOMPANHAMENTO;

    @Column(nullable = false, length = 160)
    private String assunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_contato", nullable = false)
    private LocalDateTime dataContato;

    @Column(name = "data_proxima_acao")
    private LocalDate dataProximaAcao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
        if (dataContato == null) dataContato = LocalDateTime.now();
        if (resultado == null) resultado = ResultadoCrm.EM_ACOMPANHAMENTO;
    }

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public CanalCrm getCanal() { return canal; }
    public void setCanal(CanalCrm canal) { this.canal = canal; }
    public TipoInteracaoCrm getTipo() { return tipo; }
    public void setTipo(TipoInteracaoCrm tipo) { this.tipo = tipo; }
    public ResultadoCrm getResultado() { return resultado; }
    public void setResultado(ResultadoCrm resultado) { this.resultado = resultado; }
    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDateTime getDataContato() { return dataContato; }
    public void setDataContato(LocalDateTime dataContato) { this.dataContato = dataContato; }
    public LocalDate getDataProximaAcao() { return dataProximaAcao; }
    public void setDataProximaAcao(LocalDate dataProximaAcao) { this.dataProximaAcao = dataProximaAcao; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}
