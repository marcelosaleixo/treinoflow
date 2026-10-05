package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CrmService {
    private final InteracaoCrmRepository interacoes;
    private final AlunoRepository alunos;
    private final UsuarioPersonalRepository personais;

    public CrmService(InteracaoCrmRepository interacoes, AlunoRepository alunos, UsuarioPersonalRepository personais) {
        this.interacoes = interacoes;
        this.alunos = alunos;
        this.personais = personais;
    }

    @Transactional(readOnly = true)
    public Page<Aluno> listarAlunos(Long personalId, String termo, Pageable pageable) {
        String filtro = termo == null || termo.isBlank() ? null : termo.trim();
        return alunos.buscarPorPersonalETermo(personalId, filtro, pageable);
    }

    @Transactional(readOnly = true)
    public Aluno buscarAluno(Long personalId, Long alunoId) {
        return validarAluno(personalId, alunoId);
    }

    @Transactional(readOnly = true)
    public List<InteracaoCrm> timeline(Long personalId, Long alunoId) {
        validarAluno(personalId, alunoId);
        return interacoes.findByPersonalIdAndAlunoIdOrderByDataContatoDesc(personalId, alunoId);
    }

    @Transactional(readOnly = true)
    public List<InteracaoCrm> acoesPendentes(Long personalId) {
        return interacoes.buscarAcoesAtePorResultado(
                personalId, LocalDate.now().plusDays(7), ResultadoCrm.EM_ACOMPANHAMENTO);
    }

    public InteracaoCrm salvar(Long personalId, Long alunoId, InteracaoCrm interacao) {
        UsuarioPersonal personal = personais.findById(personalId)
                .orElseThrow(() -> new IllegalArgumentException("Personal não encontrado."));
        Aluno aluno = validarAluno(personalId, alunoId);
        if (interacao.getCanal() == null) throw new IllegalArgumentException("Informe o canal da interação.");
        if (interacao.getTipo() == null) throw new IllegalArgumentException("Informe o tipo da interação.");
        if (interacao.getResultado() == null) interacao.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO);
        if (interacao.getAssunto() == null || interacao.getAssunto().isBlank() || interacao.getAssunto().trim().length() > 160) {
            throw new IllegalArgumentException("Informe um assunto com até 160 caracteres.");
        }
        if (interacao.getDescricao() == null || interacao.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Informe o que foi tratado com o aluno.");
        }
        interacao.setPersonal(personal);
        interacao.setAluno(aluno);
        interacao.setAssunto(interacao.getAssunto().trim());
        if (interacao.getDataContato() == null) interacao.setDataContato(LocalDateTime.now());
        return interacoes.save(interacao);
    }

    @Transactional(readOnly = true)
    public Dashboard dadosDashboard(Long personalId) {
        LocalDateTime inicio30 = LocalDateTime.now().minusDays(30);
        long ativos = alunos.countByPersonalIdAndStatus(personalId, "ATIVO");
        long inativos = alunos.countByPersonalIdAndStatus(personalId, "INATIVO");
        long risco = interacoes.countAlunosSemContatoDesde(personalId, inicio30);
        long recuperados = interacoes.countAlunosDistinctPorResultado(personalId, ResultadoCrm.RECUPERADO);
        long renovados = interacoes.countAlunosDistinctPorResultado(personalId, ResultadoCrm.RENOVADO);
        long cancelamentos = interacoes.countAlunosDistinctPorResultado(personalId, ResultadoCrm.CANCELAMENTO);
        long acoes = interacoes.countAcoesPendentes(personalId, LocalDate.now(), ResultadoCrm.EM_ACOMPANHAMENTO);
        long contatos30 = interacoes.countAlunosComContatoDesde(personalId, inicio30);
        long baseDecisao = recuperados + cancelamentos;
        double taxaRecuperacao = baseDecisao == 0 ? 0.0 : (recuperados * 100.0 / baseDecisao);
        return new Dashboard(ativos, inativos, risco, recuperados, renovados, cancelamentos, acoes, contatos30, taxaRecuperacao);
    }

    private Aluno validarAluno(Long personalId, Long alunoId) {
        if (personalId == null || alunoId == null) throw new IllegalArgumentException("Aluno ou personal inválido.");
        return alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal."));
    }

    public record Dashboard(long alunosAtivos, long alunosInativos, long riscoChurn, long recuperados,
                            long renovados, long cancelamentos, long acoesPendentes, long contatos30Dias,
                            double taxaRecuperacao) {}
}
