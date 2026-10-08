package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.PrescricaoAuditoria;
import com.marceloaleixo.treinoflow.repository.PrescricaoAuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Etapa 90 - captura a avaliação do Personal sobre uma recomendação aplicada. */
@Service
@Transactional
public class FeedbackPrescricaoService {
    private final PrescricaoAuditoriaRepository auditorias;

    public FeedbackPrescricaoService(PrescricaoAuditoriaRepository auditorias) {
        this.auditorias = auditorias;
    }

    public PrescricaoAuditoria registrar(Long auditoriaId, Long alunoId, Long personalId, String feedback, String motivo) {
        PrescricaoAuditoria auditoria = auditorias.buscarPorIdDoPersonal(auditoriaId, alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Decisão da IA não encontrada para este Personal."));

        if (auditoria.getFeedbackDecisao() != null) {
            throw new IllegalStateException("Esta decisão da IA já possui feedback registrado.");
        }
        if (!"APLICADA".equals(auditoria.getDecisao())) {
            throw new IllegalStateException("Somente decisões aplicadas podem receber feedback.");
        }

        String valor = normalizarFeedback(feedback);
        String texto = motivo == null ? null : motivo.trim();
        if (("ACEITA_AJUSTADA".equals(valor) || "NAO_CONCORDEI".equals(valor)) && (texto == null || texto.isBlank())) {
            throw new IllegalArgumentException("Informe o motivo para este feedback.");
        }
        if (texto != null && texto.length() > 1000) {
            throw new IllegalArgumentException("O motivo deve ter no máximo 1000 caracteres.");
        }

        auditoria.setFeedbackDecisao(valor);
        auditoria.setFeedbackMotivo(texto == null || texto.isBlank() ? null : texto);
        auditoria.setFeedbackData(LocalDateTime.now());
        return auditorias.save(auditoria);
    }

    private String normalizarFeedback(String feedback) {
        String valor = feedback == null ? "" : feedback.trim().toUpperCase();
        return switch (valor) {
            case "ACEITA" -> "ACEITA";
            case "ACEITA_AJUSTADA" -> "ACEITA_AJUSTADA";
            case "NAO_CONCORDEI" -> "NAO_CONCORDEI";
            default -> throw new IllegalArgumentException("Feedback inválido.");
        };
    }
}
