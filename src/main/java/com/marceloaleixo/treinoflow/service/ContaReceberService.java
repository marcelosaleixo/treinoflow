package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.ContaReceber;
import com.marceloaleixo.treinoflow.entity.PagamentoContaReceber;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.event.SincronizarMetaReceitaEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.marceloaleixo.treinoflow.enums.FormaPagamento;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class ContaReceberService {
    private final ContaReceberRepository repository;
    private final AlunoRepository alunos;
    private final UsuarioPersonalService personals;
    private final ApplicationEventPublisher eventos;

    public ContaReceberService(ContaReceberRepository repository, AlunoRepository alunos, UsuarioPersonalService personals, ApplicationEventPublisher eventos) {
        this.repository = repository;
        this.alunos = alunos;
        this.personals = personals;
        this.eventos = eventos;
    }

    public void atualizarAtrasadas(Long personalId) {
        LocalDate hoje = LocalDate.now();
        List<ContaReceber> vencidas = repository.vencidas(personalId, StatusContaReceber.PENDENTE, hoje);
        vencidas.forEach(c -> c.setStatus(StatusContaReceber.ATRASADA));
        if (!vencidas.isEmpty()) repository.saveAll(vencidas);
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> listar(Long personalId) {
        return repository.findByPersonalIdOrderByDataVencimentoAsc(personalId);
    }

    @Transactional(readOnly = true)
    public List<ContaReceber> proximas(Long personalId) {
        return repository.findTop12ByPersonalIdOrderByDataVencimentoAsc(personalId);
    }

    public ContaReceber criar(Long personalId, Long alunoId, String descricao, BigDecimal valor,
                              LocalDate vencimento, String observacao) {
        if (descricao == null || descricao.isBlank() || descricao.trim().length() > 160)
            throw new IllegalArgumentException("Descrição é obrigatória e deve ter até 160 caracteres.");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Informe um valor maior que zero.");
        if (vencimento == null)
            throw new IllegalArgumentException("Informe a data de vencimento.");

        UsuarioPersonal personal = personals.buscarPorId(personalId);
        ContaReceber conta = new ContaReceber();
        conta.setPersonal(personal);
        if (alunoId != null) {
            Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                    .orElseThrow(() -> new IllegalArgumentException("Aluno não pertence ao personal autenticado."));
            conta.setAluno(aluno);
        }
        conta.setDescricao(descricao.trim());
        conta.setValor(valor);
        conta.setDataVencimento(vencimento);
        conta.setObservacao(observacao == null || observacao.isBlank() ? null : observacao.trim());
        conta.setStatus(vencimento.isBefore(LocalDate.now()) ? StatusContaReceber.ATRASADA : StatusContaReceber.PENDENTE);
        return repository.save(conta);
    }

    public ContaReceber marcarPaga(Long personalId, Long id, List<FormaPagamento> formasPagamento,
                                   List<BigDecimal> valoresPagamento, LocalDate dataPagamento) {
        ContaReceber conta = repository.findByIdAndPersonalId(id, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Conta a receber não encontrada."));
        if (conta.getStatus() == StatusContaReceber.CANCELADA)
            throw new IllegalArgumentException("Uma conta cancelada não pode ser marcada como paga.");

        if (formasPagamento == null || valoresPagamento == null || formasPagamento.isEmpty()
                || formasPagamento.size() != valoresPagamento.size()) {
            throw new IllegalArgumentException("Informe pelo menos uma forma de pagamento com seu respectivo valor.");
        }

        BigDecimal total = BigDecimal.ZERO;
        java.util.Set<FormaPagamento> formasUsadas = new java.util.HashSet<>();
        LocalDate pagamentoEm = dataPagamento == null ? LocalDate.now() : dataPagamento;

        java.util.Set<YearMonth> periodosAfetados = new java.util.HashSet<>();
        conta.getPagamentos().stream()
                .map(PagamentoContaReceber::getDataPagamento)
                .filter(java.util.Objects::nonNull)
                .map(YearMonth::from)
                .forEach(periodosAfetados::add);
        conta.getPagamentos().clear();
        for (int i = 0; i < formasPagamento.size(); i++) {
            FormaPagamento forma = formasPagamento.get(i);
            BigDecimal valor = valoresPagamento.get(i);
            if (forma == null) throw new IllegalArgumentException("Selecione todas as formas de pagamento.");
            if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("Todos os valores de pagamento devem ser maiores que zero.");
            if (!formasUsadas.add(forma))
                throw new IllegalArgumentException("Não repita a mesma forma de pagamento. Some o valor em uma única linha.");

            PagamentoContaReceber pagamento = new PagamentoContaReceber();
            pagamento.setFormaPagamento(forma);
            pagamento.setValor(valor.setScale(2, java.math.RoundingMode.HALF_UP));
            pagamento.setDataPagamento(pagamentoEm);
            conta.adicionarPagamento(pagamento);
            total = total.add(pagamento.getValor());
        }

        total = total.setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal valorConta = conta.getValor().setScale(2, java.math.RoundingMode.HALF_UP);
        if (total.compareTo(valorConta) != 0) {
            throw new IllegalArgumentException("A soma dos pagamentos (R$ " + total.toPlainString()
                    + ") deve ser exatamente igual ao valor da conta (R$ " + valorConta.toPlainString() + ").");
        }

        conta.setStatus(StatusContaReceber.PAGA);
        conta.setDataPagamento(pagamentoEm);
        // Mantém o campo legado preenchido quando há uma única forma. Em pagamentos divididos,
        // o detalhe oficial fica na coleção pagamentos.
        conta.setFormaPagamento(formasPagamento.size() == 1 ? formasPagamento.get(0) : null);
        ContaReceber salva = repository.save(conta);
        periodosAfetados.add(YearMonth.from(pagamentoEm));
        for (YearMonth periodoAfetado : periodosAfetados) {
            eventos.publishEvent(new SincronizarMetaReceitaEvent(personalId, periodoAfetado));
        }
        return salva;
    }

    /** Compatibilidade com chamadas antigas que registravam uma única forma. */
    public ContaReceber marcarPaga(Long personalId, Long id, FormaPagamento formaPagamento, LocalDate dataPagamento) {
        return marcarPaga(personalId, id, List.of(formaPagamento), List.of(
                repository.findByIdAndPersonalId(id, personalId)
                        .orElseThrow(() -> new IllegalArgumentException("Conta a receber não encontrada."))
                        .getValor()), dataPagamento);
    }

    public ContaReceber cancelar(Long personalId, Long id) {
        ContaReceber conta = repository.findByIdAndPersonalId(id, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Conta a receber não encontrada."));
        if (conta.getStatus() == StatusContaReceber.PAGA)
            throw new IllegalArgumentException("Uma conta paga não deve ser cancelada.");
        conta.setStatus(StatusContaReceber.CANCELADA);
        return repository.save(conta);
    }

    @Transactional(readOnly = true)
    public BigDecimal recebidoNoMes(Long personalId) {
        YearMonth mes = YearMonth.now();
        return repository.somarPagasNoPeriodo(personalId, StatusContaReceber.PAGA, mes.atDay(1), mes.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public BigDecimal previstoNoMes(Long personalId) {
        YearMonth mes = YearMonth.now();
        return repository.somarVencimentosNoPeriodo(personalId, StatusContaReceber.PENDENTE, mes.atDay(1), mes.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public BigDecimal aReceber(Long personalId) {
        return repository.somarPorStatus(personalId, StatusContaReceber.PENDENTE);
    }

    @Transactional(readOnly = true)
    public BigDecimal emAtraso(Long personalId) {
        return repository.somarPorStatus(personalId, StatusContaReceber.ATRASADA);
    }

    @Transactional(readOnly = true)
    public long quantidadeAtrasadas(Long personalId) {
        return repository.contarPorStatus(personalId, StatusContaReceber.ATRASADA);
    }
}
