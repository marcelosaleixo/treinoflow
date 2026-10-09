package com.marceloaleixo.treinoflow.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Gera CSV compatível com planilhas brasileiras (separador ; e BOM UTF-8). */
@Service
public class RelatorioCsvService {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public byte[] bytes(String conteudo) {
        return ("\uFEFF" + conteudo).getBytes(StandardCharsets.UTF_8);
    }
    public String linha(String... campos) {
        return String.join(";", java.util.Arrays.stream(campos).map(this::escapar).toList()) + "\r\n";
    }
    public String escapar(String valor) {
        if (valor == null) return "\"\"";
        String limpo = valor.replace("\r", " ").replace("\n", " ");
        return "\"" + limpo.replace("\"", "\"\"") + "\"";
    }
    public String dinheiro(BigDecimal valor) {
        return valor == null ? "0,00" : valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
    public String data(LocalDate data) { return data == null ? "" : DATA.format(data); }
    public String mes(YearMonth mes) { return mes == null ? "" : mes.toString(); }
}
