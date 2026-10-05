package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Cobranca;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.payment.PagamentoGateway;
import com.marceloaleixo.treinoflow.payment.MercadoPagoGateway;
import com.marceloaleixo.treinoflow.service.CobrancaService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PagamentoController {
    private final UsuarioPersonalService usuarios;
    private final CobrancaService cobrancas;
    private final PagamentoGateway gateway;
    private final MercadoPagoGateway mercadoPago;

    public PagamentoController(UsuarioPersonalService usuarios, CobrancaService cobrancas,
                               PagamentoGateway gateway, MercadoPagoGateway mercadoPago) {
        this.usuarios = usuarios;
        this.cobrancas = cobrancas;
        this.gateway = gateway;
        this.mercadoPago = mercadoPago;
    }

    @PostMapping("/assinatura/cobrancas/{id}/pix")
    public String gerarPix(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        Cobranca cobranca = cobrancas.listarDoPersonal(personal.getId()).stream()
                .filter(c -> c.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cobrança não encontrada."));
        try {
            gateway.criarPix(cobranca);
            ra.addFlashAttribute("sucesso", "PIX gerado com sucesso.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/assinatura/cobrancas";
    }

    @GetMapping("/assinatura/cobrancas/{id}/pix")
    public String pix(@PathVariable Long id, Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        Cobranca cobranca = cobrancas.listarDoPersonal(personal.getId()).stream()
                .filter(c -> c.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cobrança não encontrada."));
        model.addAttribute("cobranca", cobranca);
        return "pagamento/pix";
    }

    @PostMapping("/webhooks/mercadopago/orders")
    @ResponseBody
    public ResponseEntity<Void> webhook(@RequestParam(name = "data.id", required = false) String dataId,
                                        @RequestHeader(name = "x-signature", required = false) String signature,
                                        @RequestHeader(name = "x-request-id", required = false) String requestId,
                                        @RequestHeader(name = "X-Signature", required = false) String signatureCapitalized,
                                        @RequestHeader(name = "X-Request-Id", required = false) String requestIdCapitalized,
                                        HttpServletRequest request) {
        String finalSignature = signature != null ? signature : signatureCapitalized;
        String finalRequestId = requestId != null ? requestId : requestIdCapitalized;
        String secret = System.getenv("TREINOFLOW_MP_WEBHOOK_SECRET");
        if (!mercadoPago.validarAssinaturaWebhook(finalSignature, finalRequestId, dataId, secret)) {
            return ResponseEntity.status(401).build();
        }
        try {
            mercadoPago.processarWebhook(dataId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
