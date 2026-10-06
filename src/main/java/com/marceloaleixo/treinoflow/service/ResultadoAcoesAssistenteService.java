package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.ResultadoAcoesAssistenteView;
import com.marceloaleixo.treinoflow.entity.AcaoAssistente;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.repository.AcaoAssistenteRepository;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ResultadoAcoesAssistenteService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final AcaoAssistenteRepository repository;
    private final AlunoRepository alunos;
    private final UsuarioPersonalRepository personais;

    public ResultadoAcoesAssistenteService(AcaoAssistenteRepository repository,
                                            AlunoRepository alunos,
                                            UsuarioPersonalRepository personais) {
        this.repository = repository;
        this.alunos = alunos;
        this.personais = personais;
    }

    @Transactional(readOnly = true)
    public ResultadoAcoesAssistenteView dashboard(Long personalId, int dias) {
        int periodo = dias <= 0 ? 30 : Math.min(dias, 365);
        LocalDateTime inicio = LocalDateTime.now().minusDays(periodo);
        List<AcaoAssistente> acoes = repository.buscarDesde(personalId, inicio);
        long total = acoes.size();
        long recuperados = contar(acoes, ResultadoCrm.RECUPERADO);
        long renovados = contar(acoes, ResultadoCrm.RENOVADO);
        long acompanhamento = contar(acoes, ResultadoCrm.EM_ACOMPANHAMENTO);
        long semResposta = contar(acoes, ResultadoCrm.SEM_RESPOSTA);
        long cancelamentos = contar(acoes, ResultadoCrm.CANCELAMENTO);
        long base = recuperados + renovados + cancelamentos;
        double taxa = base == 0 ? 0.0 : ((recuperados + renovados) * 100.0 / base);
        List<ResultadoAcoesAssistenteView.AcaoResultadoItemView> itens = acoes.stream().map(this::item).toList();
        return new ResultadoAcoesAssistenteView("Últimos " + periodo + " dias", total, recuperados, renovados,
                acompanhamento, semResposta, cancelamentos, taxa, itens);
    }

    @Transactional
    public void registrarResultado(Long personalId, Long acaoId, ResultadoCrm resultado) {
        if (resultado == null || resultado == ResultadoCrm.EM_ACOMPANHAMENTO) {
            throw new IllegalArgumentException("Selecione um resultado final ou mantenha a ação em acompanhamento.");
        }
        AcaoAssistente acao = repository.buscarPorIdDoPersonal(acaoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Ação não encontrada."));
        acao.setResultado(resultado);
        acao.setResultadoEm(LocalDateTime.now());
        repository.save(acao);
    }

    @Transactional
    public void registrarAcaoExecutada(Long personalId, Long alunoId, int score, String descricao,
                                       String mensagem, com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente tipo) {
        UsuarioPersonal personal = personais.findById(personalId)
                .orElseThrow(() -> new IllegalArgumentException("Personal não encontrado."));
        Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal."));
        AcaoAssistente acao = new AcaoAssistente();
        acao.setPersonal(personal);
        acao.setAluno(aluno);
        acao.setTipoAcao(tipo);
        acao.setScoreRisco(score);
        acao.setDescricaoAcao(descricao);
        acao.setMensagem(mensagem);
        acao.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO);
        repository.save(acao);
    }

    private long contar(List<AcaoAssistente> acoes, ResultadoCrm resultado) {
        return acoes.stream().filter(a -> a.getResultado() == resultado).count();
    }

    private ResultadoAcoesAssistenteView.AcaoResultadoItemView item(AcaoAssistente a) {
        String nome = a.getAluno() == null ? "Aluno" : a.getAluno().getNome();
        String tipo = a.getTipoAcao() == null ? "Ação" : a.getTipoAcao().getDescricao();
        return new ResultadoAcoesAssistenteView.AcaoResultadoItemView(a.getId(), nome, tipo,
                a.getResultado().getDescricao(), a.getScoreRisco(),
                a.getExecutadaEm() == null ? "-" : a.getExecutadaEm().format(FORMATTER), a.getDescricaoAcao());
    }
}
