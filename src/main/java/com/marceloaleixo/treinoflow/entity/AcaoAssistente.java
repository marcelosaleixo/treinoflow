package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "acoes_assistente", indexes = {
        @Index(name = "idx_acao_assistente_personal_data", columnList = "personal_id,executada_em"),
        @Index(name = "idx_acao_assistente_aluno_data", columnList = "aluno_id,executada_em")
})
public class AcaoAssistente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private UsuarioPersonal personal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acao", nullable = false, length = 30)
    private TipoAcaoAssistente tipoAcao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResultadoCrm resultado = ResultadoCrm.EM_ACOMPANHAMENTO;

    @Column(name = "score_risco", nullable = false)
    private int scoreRisco;

    @Column(name = "descricao_acao", nullable = false, length = 500)
    private String descricaoAcao;

    @Column(columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "executada_em", nullable = false)
    private LocalDateTime executadaEm;

    @Column(name = "resultado_em")
    private LocalDateTime resultadoEm;

    @Column(name = "automatica", nullable = false)
    private boolean automatica = false;

    @Column(name = "regra_automacao", length = 50)
    private String regraAutomacao;

    @Column(name = "jornada_id", length = 36)
    private String jornadaId;

    @Column(name = "experimento_id")
    private Long experimentoId;

    @Column(name = "experimento_variante", length = 1)
    private String experimentoVariante;

    @Column(name = "etapa_jornada", nullable = false)
    private int etapaJornada = 0;

    @Column(name = "proxima_acao_em")
    private LocalDateTime proximaAcaoEm;

    @Column(name = "jornada_status", length = 30)
    private String jornadaStatus;

    @PrePersist
    void prePersist() {
        if (executadaEm == null) executadaEm = LocalDateTime.now();
        if (resultado == null) resultado = ResultadoCrm.EM_ACOMPANHAMENTO;
    }

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public TipoAcaoAssistente getTipoAcao() { return tipoAcao; }
    public void setTipoAcao(TipoAcaoAssistente tipoAcao) { this.tipoAcao = tipoAcao; }
    public ResultadoCrm getResultado() { return resultado; }
    public void setResultado(ResultadoCrm resultado) { this.resultado = resultado; }
    public int getScoreRisco() { return scoreRisco; }
    public void setScoreRisco(int scoreRisco) { this.scoreRisco = scoreRisco; }
    public String getDescricaoAcao() { return descricaoAcao; }
    public void setDescricaoAcao(String descricaoAcao) { this.descricaoAcao = descricaoAcao; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public LocalDateTime getExecutadaEm() { return executadaEm; }
    public void setExecutadaEm(LocalDateTime executadaEm) { this.executadaEm = executadaEm; }
    public LocalDateTime getResultadoEm() { return resultadoEm; }
    public void setResultadoEm(LocalDateTime resultadoEm) { this.resultadoEm = resultadoEm; }
    public boolean isAutomatica() { return automatica; }
    public void setAutomatica(boolean automatica) { this.automatica = automatica; }
    public String getRegraAutomacao() { return regraAutomacao; }
    public void setRegraAutomacao(String regraAutomacao) { this.regraAutomacao = regraAutomacao; }
    public Long getExperimentoId() { return experimentoId; }
    public void setExperimentoId(Long experimentoId) { this.experimentoId = experimentoId; }
    public String getExperimentoVariante() { return experimentoVariante; }
    public void setExperimentoVariante(String experimentoVariante) { this.experimentoVariante = experimentoVariante; }
    public String getJornadaId() { return jornadaId; }
    public void setJornadaId(String jornadaId) { this.jornadaId = jornadaId; }
    public int getEtapaJornada() { return etapaJornada; }
    public void setEtapaJornada(int etapaJornada) { this.etapaJornada = etapaJornada; }
    public LocalDateTime getProximaAcaoEm() { return proximaAcaoEm; }
    public void setProximaAcaoEm(LocalDateTime proximaAcaoEm) { this.proximaAcaoEm = proximaAcaoEm; }
    public String getJornadaStatus() { return jornadaStatus; }
    public void setJornadaStatus(String jornadaStatus) { this.jornadaStatus = jornadaStatus; }
}
