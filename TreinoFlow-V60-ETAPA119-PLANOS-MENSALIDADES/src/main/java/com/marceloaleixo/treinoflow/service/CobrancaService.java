package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Assinatura;
import com.marceloaleixo.treinoflow.entity.Cobranca;
import com.marceloaleixo.treinoflow.enums.StatusAssinatura;
import com.marceloaleixo.treinoflow.enums.StatusCobranca;
import com.marceloaleixo.treinoflow.repository.AssinaturaRepository;
import com.marceloaleixo.treinoflow.repository.CobrancaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class CobrancaService {
    private final CobrancaRepository cobrancas;
    private final AssinaturaRepository assinaturas;
    private final NotificacaoService notificacoes;

    public CobrancaService(CobrancaRepository cobrancas, AssinaturaRepository assinaturas, NotificacaoService notificacoes) {
        this.cobrancas = cobrancas;
        this.assinaturas = assinaturas;
        this.notificacoes = notificacoes;
    }

    @Transactional
    public int gerarCobrancasDoMes() {
        int criadas = 0;
        LocalDate hoje = LocalDate.now();
        YearMonth mes = YearMonth.from(hoje);
        for (Assinatura assinatura : assinaturas.findByStatusInOrderByDataVencimentoAsc(List.of(StatusAssinatura.ATIVA, StatusAssinatura.EM_TESTE))) {
            if (cobrancas.findByAssinaturaIdAndCompetencia(assinatura.getId(), mes.atDay(1)).isPresent()) continue;
            Cobranca cobranca = new Cobranca();
            cobranca.setAssinatura(assinatura);
            cobranca.setCompetencia(mes.atDay(1));
            cobranca.setValor(assinatura.getPlano().getValorMensal());
            LocalDate vencimento = assinatura.getDataProximaCobranca() != null ? assinatura.getDataProximaCobranca() : hoje;
            cobranca.setDataVencimento(vencimento);
            cobranca.setStatus(StatusCobranca.PENDENTE);
            cobrancas.save(cobranca);
            criadas++;
        }
        return criadas;
    }

    @Transactional
    public void atualizarAtrasadas() {
        LocalDate hoje = LocalDate.now();
        for (Cobranca c : cobrancas.vencidasNaoPagas(hoje, StatusCobranca.PENDENTE)) {
            c.setStatus(StatusCobranca.ATRASADA);
            cobrancas.save(c);
            Assinatura a = c.getAssinatura();
            if (a.getStatus() == StatusAssinatura.ATIVA || a.getStatus() == StatusAssinatura.EM_TESTE) {
                a.setStatus(StatusAssinatura.INADIMPLENTE);
                assinaturas.save(a);
            }
        }
    }

    @Transactional
    public Cobranca marcarPaga(Long id) {
        Cobranca c = cobrancas.findById(id).orElseThrow();
        c.setStatus(StatusCobranca.PAGA);
        c.setDataPagamento(LocalDate.now());
        Assinatura a = c.getAssinatura();
        if (a.getStatus() == StatusAssinatura.INADIMPLENTE || a.getStatus() == StatusAssinatura.VENCIDA) {
            a.setStatus(StatusAssinatura.ATIVA);
        }
        LocalDate proxima = c.getDataVencimento().plusMonths(1);
        a.setDataProximaCobranca(proxima);
        a.setDataVencimento(proxima);
        assinaturas.save(a);
        return cobrancas.save(c);
    }

    @Transactional
    public void cancelar(Long id) {
        Cobranca c = cobrancas.findById(id).orElseThrow();
        if (c.getStatus() != StatusCobranca.PAGA) c.setStatus(StatusCobranca.CANCELADA);
        cobrancas.save(c);
    }

    @Transactional
    public Cobranca registrarPagamentoGateway(Long id, String gateway, String externalPaymentId,
                                              String externalReference, String qrCode, String qrCodeBase64, String paymentUrl) {
        Cobranca c = cobrancas.findById(id).orElseThrow();
        c.setGateway(gateway);
        c.setExternalPaymentId(externalPaymentId);
        c.setExternalReference(externalReference);
        c.setQrCode(qrCode);
        c.setQrCodeBase64(qrCodeBase64);
        c.setPaymentUrl(paymentUrl);
        return cobrancas.save(c);
    }

    @Transactional
    public Cobranca marcarPagaPorGateway(Long id, String externalPaymentId) {
        Cobranca c = cobrancas.findById(id).orElseThrow();
        if (c.getStatus() == StatusCobranca.PAGA || c.getStatus() == StatusCobranca.CANCELADA) return c;
        c.setExternalPaymentId(externalPaymentId);
        c.setStatus(StatusCobranca.PAGA);
        c.setDataPagamento(LocalDate.now());
        Assinatura a = c.getAssinatura();
        if (a.getStatus() == StatusAssinatura.INADIMPLENTE || a.getStatus() == StatusAssinatura.VENCIDA) a.setStatus(StatusAssinatura.ATIVA);
        LocalDate proxima = c.getDataVencimento().plusMonths(1);
        a.setDataProximaCobranca(proxima);
        a.setDataVencimento(proxima);
        assinaturas.save(a);
        Cobranca salva = cobrancas.save(c);
        notificacoes.registrarPagamentoConfirmado(salva);
        return salva;
    }

    public List<Cobranca> listarTodas() { return cobrancas.findAllByOrderByDataVencimentoAsc(); }
    public List<Cobranca> listarDoPersonal(Long personalId) { return cobrancas.findByAssinaturaPersonalIdOrderByDataVencimentoDesc(personalId); }
}
