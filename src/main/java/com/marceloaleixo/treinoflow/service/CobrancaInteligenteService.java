package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.CobrancaInteligenteView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.ContaReceber;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Etapa 123: transforma contas a receber em uma fila de cobrança operacional.
 * Não envia mensagens automaticamente e não baixa a conta: a decisão continua humana.
 */
@Service
public class CobrancaInteligenteService {
    private final ContaReceberRepository contas;
    private final InteracaoCrmRepository interacoes;
    private final UsuarioPersonalRepository personais;
    private final CrmService crm;

    public CobrancaInteligenteService(ContaReceberRepository contas,
                                      InteracaoCrmRepository interacoes,
                                      UsuarioPersonalRepository personais,
                                      CrmService crm) {
        this.contas = contas;
        this.interacoes = interacoes;
        this.personais = personais;
        this.crm = crm;
    }

    @Transactional
    public List<CobrancaInteligenteView> listar(Long personalId) {
        contasAtualizarAtrasadas(personalId);
        LocalDate hoje = LocalDate.now();
        return contas.buscarParaCobranca(personalId,
                        List.of(StatusContaReceber.PENDENTE, StatusContaReceber.ATRASADA),
                        hoje.plusDays(7))
                .stream()
                .map(c -> montar(c, hoje, c.getAluno() != null &&
                        interacoes.existeCobrancaPendente(personalId, c.getAluno().getId(), assunto(c))))
                .sorted(Comparator
                        .comparingInt((CobrancaInteligenteView v) -> peso(v.prioridade())).reversed()
                        .thenComparing(CobrancaInteligenteView::vencimento)
                        .thenComparing(CobrancaInteligenteView::alunoNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public CobrancaInteligenteView preparar(Long personalId, Long contaId) {
        ContaReceber conta = buscarConta(personalId, contaId);
        LocalDate hoje = LocalDate.now();
        boolean pendente = conta.getAluno() != null &&
                interacoes.existeCobrancaPendente(personalId, conta.getAluno().getId(), assunto(conta));
        return montar(conta, hoje, pendente);
    }

    @Transactional
    public void registrar(Long personalId, Long contaId, CanalCrm canal, ResultadoCrm resultado,
                          LocalDate proximaAcao, String observacao) {
        ContaReceber conta = buscarConta(personalId, contaId);
        if (conta.getAluno() == null) {
            throw new IllegalArgumentException("Esta conta não está vinculada a um aluno. Vincule um aluno antes de registrar a cobrança no CRM.");
        }
        if (resultado == null) throw new IllegalArgumentException("Informe o resultado da cobrança.");
        if (resultado == ResultadoCrm.EM_ACOMPANHAMENTO && proximaAcao == null) {
            throw new IllegalArgumentException("Defina a próxima ação quando a cobrança continuar em acompanhamento.");
        }
        if (proximaAcao != null && proximaAcao.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A próxima ação não pode estar no passado.");
        }

        String assunto = assunto(conta);
        if (resultado == ResultadoCrm.EM_ACOMPANHAMENTO &&
                interacoes.existeCobrancaPendente(personalId, conta.getAluno().getId(), assunto)) {
            throw new IllegalArgumentException("Já existe uma cobrança em acompanhamento para esta conta.");
        }

        UsuarioPersonal personal = personais.findById(personalId)
                .orElseThrow(() -> new IllegalArgumentException("Personal não encontrado."));
        Aluno aluno = conta.getAluno();

        InteracaoCrm interacao = new InteracaoCrm();
        interacao.setPersonal(personal);
        interacao.setAluno(aluno);
        interacao.setCanal(canal == null ? canalPadrao(aluno) : canal);
        interacao.setTipo(TipoInteracaoCrm.COBRANCA);
        interacao.setResultado(resultado);
        interacao.setAssunto(assunto);
        interacao.setDataProximaAcao(proximaAcao);
        interacao.setDescricao(descricao(conta, observacao));
        crm.salvar(personalId, aluno.getId(), interacao);
    }

    private ContaReceber buscarConta(Long personalId, Long contaId) {
        return contas.buscarPorIdComAluno(contaId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Conta a receber não encontrada para este Personal."));
    }

    private void contasAtualizarAtrasadas(Long personalId) {
        LocalDate hoje = LocalDate.now();
        List<ContaReceber> vencidas = contas.buscarParaCobranca(personalId,
                List.of(StatusContaReceber.PENDENTE), hoje.minusDays(1));
        if (!vencidas.isEmpty()) {
            vencidas.forEach(c -> c.setStatus(StatusContaReceber.ATRASADA));
            contas.saveAll(vencidas);
        }
    }

    private CobrancaInteligenteView montar(ContaReceber c, LocalDate hoje, boolean acompanhamentoPendente) {
        long dias = ChronoUnit.DAYS.between(hoje, c.getDataVencimento());
        String status;
        String prioridade;
        String titulo;
        String acao;
        if (dias < 0) {
            long atraso = Math.abs(dias);
            if (atraso >= 4) {
                status = "ATRASADA_4_PLUS";
                prioridade = "CRÍTICA";
                titulo = "Cobrança em atraso crítico";
                acao = "Entrar em contato hoje, entender o motivo do atraso e combinar uma regularização.";
            } else {
                status = "ATRASADA_1_3";
                prioridade = "ALTA";
                titulo = "Cobrança em atraso";
                acao = "Fazer uma cobrança cordial e registrar o retorno do aluno.";
            }
        } else if (dias == 0) {
            status = "VENCE_HOJE";
            prioridade = "ALTA";
            titulo = "Mensalidade vence hoje";
            acao = "Enviar lembrete de pagamento e acompanhar o retorno.";
        } else {
            status = "VENCE_EM_BREVE";
            prioridade = dias <= 3 ? "MÉDIA" : "BAIXA";
            titulo = "Mensalidade próxima do vencimento";
            acao = dias <= 3
                    ? "Enviar lembrete preventivo antes do vencimento."
                    : "Revisar a cobrança e preparar o lembrete do aluno.";
        }
        String nome = c.getAluno() == null || c.getAluno().getNome() == null || c.getAluno().getNome().isBlank()
                ? "Aluno" : c.getAluno().getNome().trim();
        String primeiro = nome.split("\\s+")[0];
        String mensagem = mensagem(primeiro, c, dias);
        return new CobrancaInteligenteView(c.getId(),
                c.getAluno() == null ? null : c.getAluno().getId(), nome,
                c.getAluno() == null ? null : c.getAluno().getTelefone(),
                c.getValor(), c.getDataVencimento(), dias, status, prioridade, titulo, acao,
                mensagem, whatsappUrl(c.getAluno(), mensagem), acompanhamentoPendente);
    }

    private String assunto(ContaReceber conta) {
        return "Cobrança conta #" + conta.getId();
    }

    private String descricao(ContaReceber conta, String observacao) {
        StringBuilder b = new StringBuilder();
        b.append("Cobrança registrada a partir do Financeiro Inteligente.\n\n");
        b.append("Conta: #").append(conta.getId()).append("\n");
        b.append("Descrição: ").append(conta.getDescricao()).append("\n");
        b.append("Valor: R$ ").append(conta.getValor()).append("\n");
        b.append("Vencimento: ").append(conta.getDataVencimento()).append("\n");
        if (observacao != null && !observacao.isBlank()) {
            b.append("\nObservação do Personal: ").append(observacao.trim());
        }
        return b.toString();
    }

    private CanalCrm canalPadrao(Aluno aluno) {
        return aluno.getTelefone() != null && !aluno.getTelefone().isBlank()
                ? CanalCrm.WHATSAPP : CanalCrm.TELEFONE;
    }

    private String mensagem(String primeiro, ContaReceber conta, long dias) {
        String valor = "R$ " + conta.getValor().setScale(2).toPlainString().replace('.', ',');
        if (dias < 0) {
            return "Oi, " + primeiro + "! Tudo bem? Estou passando para lembrar da mensalidade de " + valor
                    + ", com vencimento em " + conta.getDataVencimento().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + ". Se precisar combinar a melhor forma de regularização, me avise."
                    ;
        }
        if (dias == 0) {
            return "Oi, " + primeiro + "! Tudo bem? Sua mensalidade de " + valor
                    + " vence hoje. Quando realizar o pagamento, pode me enviar a confirmação. Obrigado!";
        }
        return "Oi, " + primeiro + "! Tudo bem? Passando para lembrar que sua mensalidade de " + valor
                + " vence em " + conta.getDataVencimento().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + ". Se precisar de alguma informação sobre o pagamento, estou à disposição.";
    }

    private String whatsappUrl(Aluno aluno, String mensagem) {
        if (aluno == null || aluno.getTelefone() == null || aluno.getTelefone().isBlank()) return "";
        String numero = aluno.getTelefone().replaceAll("\\D", "");
        if (numero.isBlank()) return "";
        return "https://wa.me/" + numero + "?text=" + URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
    }

    private int peso(String prioridade) {
        return switch (prioridade) {
            case "CRÍTICA" -> 4;
            case "ALTA" -> 3;
            case "MÉDIA" -> 2;
            default -> 1;
        };
    }
}
