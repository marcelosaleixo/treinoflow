package com.marceloaleixo.treinoflow.dto;

import java.math.BigDecimal;

/** Resumo financeiro por aluno, sem atribuir despesas gerais a alunos individualmente. */
public class ReceitaAlunoFinanceiraView {
    private final Long alunoId;
    private final String nomeAluno;
    private final BigDecimal recebidoNoMes;
    private final BigDecimal emAberto;
    private final BigDecimal atrasado;

    public ReceitaAlunoFinanceiraView(Long alunoId, String nomeAluno, BigDecimal recebidoNoMes,
                                      BigDecimal emAberto, BigDecimal atrasado) {
        this.alunoId = alunoId;
        this.nomeAluno = nomeAluno;
        this.recebidoNoMes = recebidoNoMes == null ? BigDecimal.ZERO : recebidoNoMes;
        this.emAberto = emAberto == null ? BigDecimal.ZERO : emAberto;
        this.atrasado = atrasado == null ? BigDecimal.ZERO : atrasado;
    }
    public Long getAlunoId() { return alunoId; }
    public String getNomeAluno() { return nomeAluno; }
    public BigDecimal getRecebidoNoMes() { return recebidoNoMes; }
    public BigDecimal getEmAberto() { return emAberto; }
    public BigDecimal getAtrasado() { return atrasado; }
}
