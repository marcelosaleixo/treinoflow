package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.ContaReceber;
import com.marceloaleixo.treinoflow.entity.PlanoMensalidade;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import com.marceloaleixo.treinoflow.repository.PlanoMensalidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class PlanoMensalidadeService {
    private final PlanoMensalidadeRepository repository;
    private final AlunoRepository alunos;
    private final ContaReceberRepository contas;
    private final UsuarioPersonalService personals;

    public PlanoMensalidadeService(PlanoMensalidadeRepository repository, AlunoRepository alunos,
                                   ContaReceberRepository contas, UsuarioPersonalService personals) {
        this.repository = repository;
        this.alunos = alunos;
        this.contas = contas;
        this.personals = personals;
    }

    @Transactional(readOnly = true)
    public List<PlanoMensalidade> listar(Long personalId) {
        return repository.findByPersonalIdOrderByAtivoDescAlunoNomeAsc(personalId);
    }

    public PlanoMensalidade criar(Long personalId, Long alunoId, String nome, BigDecimal valor,
                                  Integer diaVencimento, LocalDate dataInicio, String observacao) {
        if (alunoId == null) throw new IllegalArgumentException("Selecione um aluno.");
        if (nome == null || nome.isBlank() || nome.trim().length() > 120)
            throw new IllegalArgumentException("Nome do plano é obrigatório e deve ter até 120 caracteres.");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Informe um valor maior que zero.");
        if (diaVencimento == null || diaVencimento < 1 || diaVencimento > 28)
            throw new IllegalArgumentException("O dia de vencimento deve ficar entre 1 e 28.");
        if (dataInicio == null) throw new IllegalArgumentException("Informe a data de início.");

        UsuarioPersonal personal = personals.buscarPorId(personalId);
        Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não pertence ao personal autenticado."));

        PlanoMensalidade plano = new PlanoMensalidade();
        plano.setPersonal(personal);
        plano.setAluno(aluno);
        plano.setNome(nome.trim());
        plano.setValor(valor);
        plano.setDiaVencimento(diaVencimento);
        plano.setDataInicio(dataInicio);
        plano.setObservacao(observacao == null || observacao.isBlank() ? null : observacao.trim());
        plano.setAtivo(true);
        return repository.save(plano);
    }

    public PlanoMensalidade alternarAtivo(Long personalId, Long id) {
        PlanoMensalidade plano = repository.findByIdAndPersonalId(id, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado."));
        plano.setAtivo(!plano.isAtivo());
        return repository.save(plano);
    }

    public ContaReceber gerarCobranca(Long personalId, Long planoId, LocalDate referencia) {
        PlanoMensalidade plano = repository.findByIdAndPersonalId(planoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado."));
        if (!plano.isAtivo()) throw new IllegalArgumentException("Ative o plano antes de gerar uma cobrança.");

        LocalDate vencimento = calcularVencimento(plano, referencia == null ? LocalDate.now() : referencia);
        if (contas.existsByPlanoMensalidadeIdAndDataVencimento(planoId, vencimento))
            throw new IllegalArgumentException("A cobrança desta mensalidade para " + vencimento.format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy")) + " já existe.");

        ContaReceber conta = new ContaReceber();
        conta.setPersonal(plano.getPersonal());
        conta.setAluno(plano.getAluno());
        conta.setPlanoMensalidade(plano);
        conta.setDescricao(plano.getNome());
        conta.setValor(plano.getValor());
        conta.setDataVencimento(vencimento);
        conta.setStatus(vencimento.isBefore(LocalDate.now()) ? StatusContaReceber.ATRASADA : StatusContaReceber.PENDENTE);
        return contas.save(conta);
    }

    public LocalDate proximoVencimento(PlanoMensalidade plano, LocalDate referencia) {
        return calcularVencimento(plano, referencia);
    }

    private LocalDate calcularVencimento(PlanoMensalidade plano, LocalDate referencia) {
        YearMonth mes = YearMonth.from(referencia);
        LocalDate inicio = plano.getDataInicio();
        if (YearMonth.from(inicio).isAfter(mes)) mes = YearMonth.from(inicio);
        LocalDate candidato = mes.atDay(Math.min(plano.getDiaVencimento(), mes.lengthOfMonth()));
        if (candidato.isBefore(referencia) && !YearMonth.from(referencia).isBefore(YearMonth.from(inicio))) {
            mes = mes.plusMonths(1);
            candidato = mes.atDay(Math.min(plano.getDiaVencimento(), mes.lengthOfMonth()));
        }
        if (candidato.isBefore(inicio)) {
            mes = YearMonth.from(inicio);
            candidato = mes.atDay(Math.min(plano.getDiaVencimento(), mes.lengthOfMonth()));
        }
        return candidato;
    }
}
