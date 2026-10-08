package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "automacoes_retencao_config", uniqueConstraints = @UniqueConstraint(name = "uk_automacao_retencao_personal", columnNames = "personal_id"))
public class AutomacaoRetencaoConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false, unique = true)
    private UsuarioPersonal personal;

    @Column(nullable = false) private boolean ativa = false;
    @Column(name = "whatsapp_critico", nullable = false) private boolean whatsappCritico = true;
    @Column(name = "followup_alto", nullable = false) private boolean followUpAlto = true;
    @Column(name = "score_minimo", nullable = false) private int scoreMinimo = 50;
    @Column(name = "max_acoes_dia", nullable = false) private int maxAcoesDia = 10;
    @Column(name = "cooldown_dias", nullable = false) private int cooldownDias = 7;
    @Column(name = "hora_inicio", nullable = false, length = 5) private String horaInicio = "08:00";
    @Column(name = "hora_fim", nullable = false, length = 5) private String horaFim = "20:00";
    @Column(name = "valor_mensal_aluno_estimado", nullable = false, precision = 10, scale = 2) private java.math.BigDecimal valorMensalAlunoEstimado = java.math.BigDecimal.ZERO;
    @Column(name = "custo_automacao_mensal", nullable = false, precision = 10, scale = 2) private java.math.BigDecimal custoAutomacaoMensal = java.math.BigDecimal.ZERO;
    @Column(name = "atualizado_em", nullable = false) private LocalDateTime atualizadoEm;

    @PrePersist @PreUpdate
    void timestamps() { atualizadoEm = LocalDateTime.now(); }

    public Long getId(){return id;} public UsuarioPersonal getPersonal(){return personal;} public void setPersonal(UsuarioPersonal p){this.personal=p;}
    public boolean isAtiva(){return ativa;} public void setAtiva(boolean v){ativa=v;}
    public boolean isWhatsappCritico(){return whatsappCritico;} public void setWhatsappCritico(boolean v){whatsappCritico=v;}
    public boolean isFollowUpAlto(){return followUpAlto;} public void setFollowUpAlto(boolean v){followUpAlto=v;}
    public int getScoreMinimo(){return scoreMinimo;} public void setScoreMinimo(int v){scoreMinimo=v;}
    public int getMaxAcoesDia(){return maxAcoesDia;} public void setMaxAcoesDia(int v){maxAcoesDia=v;}
    public int getCooldownDias(){return cooldownDias;} public void setCooldownDias(int v){cooldownDias=v;}
    public String getHoraInicio(){return horaInicio;} public void setHoraInicio(String v){horaInicio=v;}
    public String getHoraFim(){return horaFim;} public void setHoraFim(String v){horaFim=v;}
    public java.math.BigDecimal getValorMensalAlunoEstimado(){return valorMensalAlunoEstimado;} public void setValorMensalAlunoEstimado(java.math.BigDecimal v){valorMensalAlunoEstimado=v;}
    public java.math.BigDecimal getCustoAutomacaoMensal(){return custoAutomacaoMensal;} public void setCustoAutomacaoMensal(java.math.BigDecimal v){custoAutomacaoMensal=v;}
    public LocalDateTime getAtualizadoEm(){return atualizadoEm;}
}
