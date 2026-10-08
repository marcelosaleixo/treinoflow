package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.RadarAcaoView;
import com.marceloaleixo.treinoflow.dto.RadarAcaoRegistro;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/** Etapa 110: transforma um item do Radar em ação humana registrável no CRM. */
@Service
public class RadarAcaoService {
    private final AlunoRepository alunos;
    private final UsuarioPersonalRepository personais;
    private final CrmService crm;
    private final ResultadoAcoesAssistenteService resultadosAssistente;

    public RadarAcaoService(AlunoRepository alunos, UsuarioPersonalRepository personais, CrmService crm,
                            ResultadoAcoesAssistenteService resultadosAssistente) {
        this.alunos = alunos;
        this.personais = personais;
        this.crm = crm;
        this.resultadosAssistente = resultadosAssistente;
    }

    @Transactional(readOnly = true)
    public RadarAcaoView preparar(Long personalId, Long alunoId, String tipo,
                                  String titulo, String motivo, String acao) {
        Aluno aluno = validarAluno(personalId, alunoId);
        String nome = aluno.getNome() == null || aluno.getNome().isBlank() ? "Aluno" : aluno.getNome().trim();
        String primeiro = nome.split("\\s+")[0];
        String tituloSeguro = limitar(titulo == null || titulo.isBlank() ? "Ação do Radar" : titulo.trim(), 160);
        String motivoSeguro = motivo == null || motivo.isBlank() ? "Sinal identificado pelo Radar Diário." : motivo.trim();
        String acaoSegura = acao == null || acao.isBlank() ? "Entrar em contato, entender o contexto e registrar o resultado." : acao.trim();
        String mensagem = mensagem(primeiro, tipo, motivoSeguro);
        return new RadarAcaoView(aluno.getId(), nome, tipo == null ? "RADAR" : tipo, tituloSeguro,
                motivoSeguro, acaoSegura, mensagem, whatsappUrl(aluno, mensagem));
    }

    @Transactional
    public void registrar(RadarAcaoRegistro registro) {
        if (registro == null) throw new IllegalArgumentException("Dados da ação não informados.");

        Long personalId = registro.personalId();
        Long alunoId = registro.alunoId();
        String tipo = registro.tipo();
        String assunto = registro.assunto();
        String motivo = registro.motivo();
        String acao = registro.acao();
        CanalCrm canal = registro.canal();
        ResultadoCrm resultado = registro.resultado();
        LocalDate proximaAcao = registro.proximaAcao();
        String observacao = registro.observacao();
        int scoreRiscoAntes = registro.scoreRiscoAntes();

        if (resultado == null) throw new IllegalArgumentException("Informe o resultado da ação.");
        if (resultado == ResultadoCrm.EM_ACOMPANHAMENTO && proximaAcao == null) {
            throw new IllegalArgumentException("Defina a próxima ação quando o resultado continuar em acompanhamento.");
        }
        if (proximaAcao != null && proximaAcao.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A próxima ação não pode estar no passado.");
        }
        Aluno aluno = validarAluno(personalId, alunoId);
        UsuarioPersonal personal = personais.findById(personalId)
                .orElseThrow(() -> new IllegalArgumentException("Personal não encontrado."));

        InteracaoCrm interacao = new InteracaoCrm();
        interacao.setPersonal(personal);
        interacao.setAluno(aluno);
        interacao.setCanal(canal == null ? canalPadrao(aluno) : canal);
        interacao.setTipo(TipoInteracaoCrm.RETENCAO);
        interacao.setResultado(resultado);
        interacao.setAssunto(limitar(assunto == null || assunto.isBlank() ? "Ação do Radar Diário" : assunto.trim(), 160));
        StringBuilder descricao = new StringBuilder();
        descricao.append("Ação executada a partir do Radar Diário.\n\n");
        descricao.append("Sinal identificado: ").append(motivo == null ? "não informado" : motivo.trim()).append("\n\n");
        descricao.append("Ação recomendada: ").append(acao == null ? "não informada" : acao.trim());
        if (observacao != null && !observacao.isBlank()) {
            descricao.append("\n\nResultado/observação do Personal: ").append(observacao.trim());
        }
        interacao.setDescricao(descricao.toString());
        interacao.setDataProximaAcao(proximaAcao);
        crm.salvar(personalId, alunoId, interacao);

        // Etapa 112: o Radar passa a alimentar o histórico de aprendizado.
        // O CRM continua sendo o registro operacional principal; esta cópia
        // estruturada serve apenas para aprender qual tipo de ação performa melhor.
        TipoAcaoAssistente tipoAprendizado = canal == CanalCrm.WHATSAPP
                ? TipoAcaoAssistente.WHATSAPP
                : TipoAcaoAssistente.FOLLOW_UP;
        String primeiro = aluno.getNome() == null || aluno.getNome().isBlank()
                ? "Aluno" : aluno.getNome().trim().split("\\s+")[0];
        String mensagemAprendizado = mensagem(primeiro, tipo, motivo == null ? "" : motivo.trim());
        String descricaoAprendizado = "RADAR_DIARIO · " +
                (assunto == null || assunto.isBlank() ? "Ação de retenção" : assunto.trim());
        resultadosAssistente.registrarAcaoRadar(personalId, alunoId, Math.max(0, Math.min(100, scoreRiscoAntes)),
                limitar(descricaoAprendizado, 500), mensagemAprendizado, tipoAprendizado, resultado);
    }

    private Aluno validarAluno(Long personalId, Long alunoId) {
        if (personalId == null || alunoId == null) throw new IllegalArgumentException("Aluno ou Personal inválido.");
        return alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este Personal."));
    }

    private CanalCrm canalPadrao(Aluno aluno) {
        return aluno.getTelefone() != null && !aluno.getTelefone().isBlank() ? CanalCrm.WHATSAPP : CanalCrm.TELEFONE;
    }

    private String mensagem(String primeiro, String tipo, String motivo) {
        if ("FEEDBACK".equalsIgnoreCase(tipo)) {
            return "Oi, " + primeiro + "! Vi seu feedback recente do treino e queria saber como você está. " +
                    "Quero entender melhor o que aconteceu para acompanharmos sua evolução. Está tudo bem?";
        }
        if ("FOLLOW-UP".equalsIgnoreCase(tipo)) {
            return "Oi, " + primeiro + "! Passando para dar continuidade ao nosso acompanhamento. " +
                    "Como você está e como estão os treinos?";
        }
        return "Oi, " + primeiro + "! Passando para acompanhar seus treinos. " +
                "Queria entender como você está e se posso te ajudar em alguma coisa.";
    }

    private String whatsappUrl(Aluno aluno, String mensagem) {
        if (aluno.getTelefone() == null || aluno.getTelefone().isBlank()) return "";
        String numero = aluno.getTelefone().replaceAll("\\D", "");
        if (numero.isBlank()) return "";
        return "https://wa.me/" + numero + "?text=" + URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
    }

    private String limitar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
