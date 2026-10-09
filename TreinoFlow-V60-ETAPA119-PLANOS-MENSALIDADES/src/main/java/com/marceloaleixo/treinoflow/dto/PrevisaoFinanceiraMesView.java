package com.marceloaleixo.treinoflow.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

/** Projeção gerencial baseada em contas e despesas já cadastradas e mensalidades ativas. */
public record PrevisaoFinanceiraMesView(YearMonth periodo, BigDecimal receitaPrevista,
                                        BigDecimal despesasCadastradas, BigDecimal saldoProjetado,
                                        String observacao) {}
