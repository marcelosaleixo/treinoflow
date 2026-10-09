package com.marceloaleixo.treinoflow.service;
import com.marceloaleixo.treinoflow.entity.MetaFinanceira;
import com.marceloaleixo.treinoflow.repository.MetaFinanceiraRepository;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import com.marceloaleixo.treinoflow.repository.DespesaPersonalRepository;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class MetaFinanceiraService {
 private final MetaFinanceiraRepository metas; private final ContaReceberRepository contas; private final DespesaPersonalRepository despesas; private final UsuarioPersonalService personals;
 public MetaFinanceiraService(MetaFinanceiraRepository metas, ContaReceberRepository contas, DespesaPersonalRepository despesas, UsuarioPersonalService personals){this.metas=metas;this.contas=contas;this.despesas=despesas;this.personals=personals;}
 @Transactional(readOnly=true) public MetaFinanceira buscar(Long personalId, YearMonth mes){return metas.findByPersonalIdAndMesReferencia(personalId,mes.atDay(1)).orElseGet(()->{MetaFinanceira m=new MetaFinanceira();m.setPersonal(personals.buscarPorId(personalId));m.setMesReferencia(mes.atDay(1));m.setMetaReceita(BigDecimal.ZERO);m.setLimiteDespesas(BigDecimal.ZERO);return m;});}
 @Transactional public void salvar(Long personalId, YearMonth mes, BigDecimal receita, BigDecimal limite, String observacao){if(receita==null||receita.signum()<0||limite==null||limite.signum()<0)throw new IllegalArgumentException("Informe metas iguais ou maiores que zero."); if(receita.scale()>2||limite.scale()>2)throw new IllegalArgumentException("Use no máximo duas casas decimais."); MetaFinanceira m=metas.findByPersonalIdAndMesReferencia(personalId,mes.atDay(1)).orElseGet(MetaFinanceira::new);m.setPersonal(personals.buscarPorId(personalId));m.setMesReferencia(mes.atDay(1));m.setMetaReceita(receita);m.setLimiteDespesas(limite);m.setObservacao(observacao==null?null:observacao.trim());metas.save(m);}
 public BigDecimal receitaRealizada(Long id,YearMonth m){return contas.somarPagasNoPeriodo(id,StatusContaReceber.PAGA,m.atDay(1),m.atEndOfMonth());}
 public BigDecimal despesasRealizadas(Long id,YearMonth m){return despesas.somarPagas(id,m.atDay(1),m.atEndOfMonth());}
}
