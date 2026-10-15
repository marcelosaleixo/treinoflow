package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import jakarta.persistence.*;

@Entity
@Table(name = "preferencias_notificacao", uniqueConstraints =
        @UniqueConstraint(name = "uk_preferencia_notificacao_personal_tipo", columnNames = {"personal_id", "tipo"}),
        indexes = @Index(name = "idx_preferencia_notificacao_personal", columnList = "personal_id"))
public class PreferenciaNotificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private UsuarioPersonal personal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacao tipo;

    @Column(name = "tempo_real_ativo", nullable = false)
    private boolean tempoRealAtivo = true;

    protected PreferenciaNotificacao() { }

    public PreferenciaNotificacao(UsuarioPersonal personal, TipoNotificacao tipo, boolean tempoRealAtivo) {
        this.personal = personal;
        this.tipo = tipo;
        this.tempoRealAtivo = tempoRealAtivo;
    }

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public TipoNotificacao getTipo() { return tipo; }
    public boolean isTempoRealAtivo() { return tempoRealAtivo; }
    public void setTempoRealAtivo(boolean ativo) { this.tempoRealAtivo = ativo; }
}
