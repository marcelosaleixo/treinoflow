package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.DespesaPersonal;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.DespesaPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class DespesaPersonalService {
    private final DespesaPersonalRepository repository;
    private final UsuarioPersonalService personals;
    public DespesaPersonalService(DespesaPersonalRepository repository, UsuarioPersonalService personals) {
        this.repository = repository; this.personals = personals;
    }
    public DespesaPersonal criar(Long personalId, String descricao, String categoria, BigDecimal valor,
                                 LocalDate data, boolean paga, String observacao) {
        if (descricao == null || descricao.isBlank() || descricao.trim().length() > 140)
            throw new IllegalArgumentException("Informe uma descrição de até 140 caracteres.");
        if (categoria == null || categoria.isBlank() || categoria.trim().length() > 60)
            throw new IllegalArgumentException("Informe uma categoria de até 60 caracteres.");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        if (data == null) throw new IllegalArgumentException("Informe a data da despesa.");
        DespesaPersonal d = new DespesaPersonal();
        d.setPersonal(personals.buscarPorId(personalId)); d.setDescricao(descricao.trim());
        d.setCategoria(categoria.trim()); d.setValor(valor.setScale(2, java.math.RoundingMode.HALF_UP));
        d.setDataDespesa(data); d.setPaga(paga);
        d.setObservacao(observacao == null || observacao.isBlank() ? null : observacao.trim());
        return repository.save(d);
    }
    @Transactional(readOnly = true)
    public List<DespesaPersonal> listar(Long personalId, LocalDate inicio, LocalDate fim) {
        return repository.findByPersonalIdAndDataDespesaBetweenOrderByDataDespesaDescIdDesc(personalId, inicio, fim);
    }
    @Transactional(readOnly = true)
    public BigDecimal totalPago(Long personalId, LocalDate inicio, LocalDate fim) {
        return repository.somarPagas(personalId, inicio, fim);
    }
    @Transactional(readOnly = true)
    public BigDecimal totalPendente(Long personalId, LocalDate inicio, LocalDate fim) {
        return repository.somarPendentes(personalId, inicio, fim);
    }
    public void excluir(Long personalId, Long id) {
        DespesaPersonal d = repository.findByIdAndPersonalId(id, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada."));
        repository.delete(d);
    }
}
