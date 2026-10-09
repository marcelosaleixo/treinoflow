package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.InteracaoRecebimentoVinculo;
import com.marceloaleixo.treinoflow.entity.MetaReceitaAcoes;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.InteracaoRecebimentoVinculoRepository;
import com.marceloaleixo.treinoflow.repository.MetaReceitaAcoesRepository;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/financeiro/alertas-metas-receita")
public class AlertasMetaReceitaAcoesController {
    private final UsuarioPersonalService personals;
    private final MetaReceitaAcoesRepository metas;
    private final InteracaoRecebimentoVinculoRepository vinculos;
    private final com.marceloaleixo.treinoflow.service.AlertaMetaReceitaPersistenteService alertasPersistentes;

    public AlertasMetaReceitaAcoesController(UsuarioPersonalService personals,
                                              MetaReceitaAcoesRepository metas,
                                              InteracaoRecebimentoVinculoRepository vinculos,
                                              com.marceloaleixo.treinoflow.service.AlertaMetaReceitaPersistenteService alertasPersistentes) {
        this.personals = personals;
        this.metas = metas;
        this.vinculos = vinculos;
        this.alertasPersistentes = alertasPersistentes;
    }

    @GetMapping
    public String index(@RequestParam(required = false) Integer ano,
                        @RequestParam(required = false) Integer mes,
                        Authentication auth, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(auth.getName());
        YearMonth periodo;
        try {
            periodo = YearMonth.of(ano == null ? LocalDate.now().getYear() : ano,
                    mes == null ? LocalDate.now().getMonthValue() : mes);
        } catch (RuntimeException ex) {
            periodo = YearMonth.now();
        }
        // Captura final para uso seguro dentro das expressões lambda.
        final YearMonth periodoSelecionado = periodo;

        MetaReceitaAcoes meta = metas.findByPersonalIdAndMesReferencia(personal.getId(), periodo.atDay(1)).orElse(null);
        alertasPersistentes.sincronizar(personal, periodo);
        List<InteracaoRecebimentoVinculo> pagamentos = vinculos.findByInteracaoPersonalIdOrderByDataVinculoDesc(personal.getId())
                .stream()
                .filter(v -> v.getPagamento() != null && v.getPagamento().getDataPagamento() != null
                        && YearMonth.from(v.getPagamento().getDataPagamento()).equals(periodoSelecionado))
                .toList();
        BigDecimal realizado = pagamentos.stream()
                .map(v -> v.getPagamento().getValor() == null ? BigDecimal.ZERO : v.getPagamento().getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal alvo = meta == null || meta.getValorMeta() == null ? BigDecimal.ZERO : meta.getValorMeta();
        BigDecimal percentual = alvo.signum() <= 0 ? BigDecimal.ZERO
                : realizado.multiply(BigDecimal.valueOf(100)).divide(alvo, 2, RoundingMode.HALF_UP);
        boolean mesAtual = periodo.equals(YearMonth.now());
        int diaAtual = mesAtual ? LocalDate.now().getDayOfMonth() : periodo.lengthOfMonth();
        int diasNoMes = periodo.lengthOfMonth();
        BigDecimal percentualEsperado = BigDecimal.valueOf(diaAtual).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(diasNoMes), 2, RoundingMode.HALF_UP);
        BigDecimal realizadoEsperado = alvo.multiply(BigDecimal.valueOf(diaAtual))
                .divide(BigDecimal.valueOf(diasNoMes), 2, RoundingMode.HALF_UP);
        boolean metaDefinida = alvo.signum() > 0;
        boolean atingida = metaDefinida && realizado.compareTo(alvo) >= 0;
        boolean abaixoDoRitmo = mesAtual && metaDefinida && !atingida && realizado.compareTo(realizadoEsperado) < 0;
        String status;
        String mensagem;
        String classe;
        if (!metaDefinida) {
            status = "Meta não definida";
            mensagem = "Defina uma meta mensal para habilitar o acompanhamento do ritmo de receita.";
            classe = "warning";
        } else if (atingida) {
            status = "Meta atingida";
            mensagem = "Parabéns! A receita atribuída já alcançou ou superou a meta deste período.";
            classe = "success";
        } else if (abaixoDoRitmo) {
            status = "Abaixo do ritmo esperado";
            mensagem = "Até hoje, a referência proporcional seria " + realizadoEsperado.toPlainString()
                    + ". Avalie as ações em aberto e o acompanhamento dos alunos.";
            classe = "danger";
        } else if (mesAtual) {
            status = "No ritmo ou acima";
            mensagem = "A receita atribuída está no ritmo proporcional esperado para o dia do mês.";
            classe = "success";
        } else {
            status = "Período encerrado";
            mensagem = "O mês terminou sem atingir a meta definida.";
            classe = "warning";
        }

        model.addAttribute("periodo", periodo);
        model.addAttribute("metaValor", alvo);
        model.addAttribute("realizado", realizado);
        model.addAttribute("percentual", percentual);
        model.addAttribute("percentualEsperado", percentualEsperado);
        model.addAttribute("realizadoEsperado", realizadoEsperado);
        model.addAttribute("status", status);
        model.addAttribute("mensagem", mensagem);
        model.addAttribute("classeAlerta", classe);
        model.addAttribute("metaDefinida", metaDefinida);
        model.addAttribute("metaAtingida", atingida);
        model.addAttribute("abaixoDoRitmo", abaixoDoRitmo);
        model.addAttribute("mesAtual", mesAtual);
        model.addAttribute("pagamentos", pagamentos);
        return "financeiro/alertas-metas-receita";
    }
}
