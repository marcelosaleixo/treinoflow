package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.FollowUpInteligenteView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Etapa 108: acompanha ações de CRM que ficaram pendentes após um contato.
 * Não dispara mensagens automaticamente; apenas organiza prazo, prioridade e próximo passo.
 */
@Service
public class FollowUpInteligenteService {
    private final InteracaoCrmRepository interacoes;
    private final UsuarioPersonalRepository personais;
    private final CrmService crm;

    public FollowUpInteligenteService(InteracaoCrmRepository interacoes,
                                      UsuarioPersonalRepository personais,
                                      CrmService crm) {
        this.interacoes = interacoes;
        this.personais = personais;
        this.crm = crm;
    }

    @Transactional(readOnly = true)
    public List<FollowUpInteligenteView> listar(Long personalId) {
        LocalDate hoje = LocalDate.now();
        return crm.acoesPendentes(personalId).stream()
                .map(i -> montar(i, hoje))
                .sorted(Comparator
                        .comparingInt((FollowUpInteligenteView v) -> peso(v.prioridade())).reversed()
                        .thenComparing(FollowUpInteligenteView::dataProximaAcao)
                        .thenComparing(FollowUpInteligenteView::alunoNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarVencidos(Long personalId) {
        return interacoes.buscarAcoesVencidas(personalId, LocalDate.now(), ResultadoCrm.EM_ACOMPANHAMENTO).size();
    }

    @Transactional
    public void resolver(Long personalId, Long interacaoId, ResultadoCrm resultado,
                         LocalDate proximaAcao, String observacao) {
        if (resultado == null) throw new IllegalArgumentException("Informe o resultado do follow-up.");
        if (resultado == ResultadoCrm.EM_ACOMPANHAMENTO && proximaAcao == null) {
            throw new IllegalArgumentException("Defina a próxima ação quando o resultado continuar em acompanhamento.");
        }
        UsuarioPersonal personal = personais.findById(personalId)
                .orElseThrow(() -> new IllegalArgumentException("Personal não encontrado."));
        InteracaoCrm atual = interacoes.findByIdAndPersonalId(interacaoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Follow-up não encontrado para este personal."));
        if (atual.getResultado() != ResultadoCrm.EM_ACOMPANHAMENTO) {
            throw new IllegalArgumentException("Este follow-up já foi encerrado.");
        }

        Aluno aluno = atual.getAluno();
        atual.setResultado(resultado);
        atual.setDataProximaAcao(null);
        String nota = observacao == null || observacao.isBlank() ? "" : "\n\nResultado do follow-up: " + observacao.trim();
        atual.setDescricao(atual.getDescricao() + nota);
        interacoes.save(atual);

        if (proximaAcao != null) {
            if (proximaAcao.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("A próxima ação não pode estar no passado.");
            }
            InteracaoCrm proximo = new InteracaoCrm();
            proximo.setPersonal(personal);
            proximo.setAluno(aluno);
            proximo.setCanal(atual.getCanal());
            proximo.setTipo(atual.getTipo());
            proximo.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO);
            proximo.setAssunto("Follow-up — " + (aluno.getNome() == null ? "aluno" : aluno.getNome()).trim());
            if (proximo.getAssunto().length() > 160) proximo.setAssunto(proximo.getAssunto().substring(0, 160));
            proximo.setDescricao("Próximo follow-up gerado a partir do contato de "
                    + atual.getDataContato().toLocalDate() + ".\n\n"
                    + "Continue acompanhando o aluno e registre o resultado desta próxima ação.");
            proximo.setDataProximaAcao(proximaAcao);
            interacoes.save(proximo);
        }
    }

    private FollowUpInteligenteView montar(InteracaoCrm i, LocalDate hoje) {
        LocalDate prazo = i.getDataProximaAcao();
        long dias = prazo == null ? 0 : ChronoUnit.DAYS.between(prazo, hoje);
        boolean vencido = prazo != null && prazo.isBefore(hoje);
        boolean hojeMesmo = prazo != null && prazo.isEqual(hoje);
        String status = vencido ? "VENCIDO" : (hojeMesmo ? "HOJE" : "PRÓXIMO");
        String prioridade = vencido && dias >= 3 ? "ATENÇÃO" : (vencido || hojeMesmo ? "ALTA" : "MÉDIA");
        String acao = vencido
                ? "Retomar o contato e registrar o resultado agora."
                : hojeMesmo
                    ? "Executar o follow-up hoje e registrar o resultado."
                    : "Preparar o próximo contato e confirmar o contexto do aluno.";
        return FollowUpInteligenteView.from(i, status, prioridade, acao, Math.max(0, dias), whatsappUrl(i.getAluno()));
    }

    private int peso(String prioridade) {
        return switch (prioridade) {
            case "ATENÇÃO" -> 3;
            case "ALTA" -> 2;
            default -> 1;
        };
    }

    private String whatsappUrl(Aluno aluno) {
        if (aluno == null || aluno.getTelefone() == null || aluno.getTelefone().isBlank()) return "";
        String telefone = aluno.getTelefone().replaceAll("\\D", "");
        if (telefone.isBlank()) return "";
        String primeiroNome = aluno.getNome() == null || aluno.getNome().isBlank()
                ? "tudo bem" : aluno.getNome().trim().split("\\s+")[0];
        String mensagem = "Oi, " + primeiroNome + "! Passando para saber como você está e acompanhar sua evolução. Está tudo bem com seus treinos?";
        return "https://wa.me/" + telefone + "?text=" + URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
    }
}
