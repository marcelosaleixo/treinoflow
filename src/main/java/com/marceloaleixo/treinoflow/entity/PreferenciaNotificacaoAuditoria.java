package com.marceloaleixo.treinoflow.entity;

import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_preferencias_notificacao", indexes = {
        @Index(name = "idx_auditoria_preferencia_personal_data", columnList = "personal_id, alterado_em"),
        @Index(name = "idx_auditoria_preferencia_tipo", columnList = "tipo")
})
public class PreferenciaNotificacaoAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private UsuarioPersonal personal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacao tipo;

    @Column(name = "valor_anterior", nullable = false)
    private boolean valorAnterior;

    @Column(name = "valor_novo", nullable = false)
    private boolean valorNovo;

    @Column(name = "alterado_em", nullable = false, updatable = false)
    private LocalDateTime alteradoEm;

    protected PreferenciaNotificacaoAuditoria() { }

    public PreferenciaNotificacaoAuditoria(UsuarioPersonal personal, TipoNotificacao tipo,
                                           boolean valorAnterior, boolean valorNovo) {
        this.personal = personal;
        this.tipo = tipo;
        this.valorAnterior = valorAnterior;
        this.valorNovo = valorNovo;
        this.alteradoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public TipoNotificacao getTipo() { return tipo; }
    public boolean isValorAnterior() { return valorAnterior; }
    public boolean isValorNovo() { return valorNovo; }
    public LocalDateTime getAlteradoEm() { return alteradoEm; }
}
