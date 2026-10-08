package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.AvaliacaoFisica;
import com.marceloaleixo.treinoflow.repository.AvaliacaoFisicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AvaliacaoFisicaService {
    @Autowired private AvaliacaoFisicaRepository avaliacoes;
    @Autowired private AlunoService alunoService;

    @Transactional(readOnly = true)
    public List<AvaliacaoFisica> listar(Long alunoId, Long personalId) {
        alunoService.buscarPorId(alunoId, personalId);
        return avaliacoes.findByAlunoIdOrderByDataAvaliacaoDescIdDesc(alunoId);
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoFisica> listarPaginado(Long alunoId, Long personalId, Pageable pageable) {
        alunoService.buscarPorId(alunoId, personalId);
        return avaliacoes.findByAlunoIdOrderByDataAvaliacaoDescIdDesc(alunoId, pageable);
    }

    @Transactional(readOnly = true)
    public AvaliacaoFisica buscar(Long id, Long alunoId, Long personalId) {
        alunoService.buscarPorId(alunoId, personalId);
        return avaliacoes.findByIdAndAlunoId(id, alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada para este aluno."));
    }

    public AvaliacaoFisica salvar(Long alunoId, Long personalId, AvaliacaoFisica form) {
        Aluno aluno = alunoService.buscarPorId(alunoId, personalId);
        if (form.getDataAvaliacao() == null) throw new IllegalArgumentException("Informe a data da avaliação.");
        validarNaoNegativo(form.getPesoKg(), "Peso");
        normalizarAlturaParaMetros(form);
        validarNaoNegativo(form.getPercentualGordura(), "Percentual de gordura");
        validarNaoNegativo(form.getCinturaCm(), "Cintura");
        validarNaoNegativo(form.getQuadrilCm(), "Quadril");
        validarNaoNegativo(form.getToraxCm(), "Tórax");
        validarNaoNegativo(form.getBracoCm(), "Braço");
        validarNaoNegativo(form.getCoxaCm(), "Coxa");
        if (form.getPercentualGordura() != null && form.getPercentualGordura().compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("O percentual de gordura deve estar entre 0 e 100.");
        if (form.getId() != null) {
            AvaliacaoFisica existente = buscar(form.getId(), alunoId, personalId);
            form.setDataCriacao(existente.getDataCriacao());
        }
        form.setAluno(aluno);
        return avaliacoes.save(form);
    }

    public void excluir(Long id, Long alunoId, Long personalId) {
        avaliacoes.delete(buscar(id, alunoId, personalId));
    }

    /**
     * Aceita altura em metros (1.60) ou centímetros (160) no formulário,
     * mas sempre persiste em metros para manter o padrão da coluna altura_metros.
     */
    private void normalizarAlturaParaMetros(AvaliacaoFisica form) {
        BigDecimal altura = form.getAlturaMetros();
        if (altura == null) return;

        if (altura.compareTo(new BigDecimal("2.50")) > 0
                && altura.compareTo(new BigDecimal("250")) <= 0) {
            altura = altura.divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            form.setAlturaMetros(altura);
        }

        if (altura.compareTo(new BigDecimal("0.50")) < 0
                || altura.compareTo(new BigDecimal("2.50")) > 0) {
            throw new IllegalArgumentException(
                    "Altura inválida. Informe em centímetros (ex.: 160) ou metros (ex.: 1,60)."
            );
        }
    }

    private void validarNaoNegativo(BigDecimal valor, String campo) {
        if (valor != null && valor.signum() < 0) throw new IllegalArgumentException(campo + " não pode ser negativo.");
    }
}
