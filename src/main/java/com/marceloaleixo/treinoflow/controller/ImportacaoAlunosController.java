package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.HistoricoImportacaoAlunos;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.HistoricoImportacaoAlunosRepository;
import com.marceloaleixo.treinoflow.service.AlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;

@Controller
@RequestMapping("/alunos/importacao")
public class ImportacaoAlunosController {
    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;
    private final AlunoService alunoService;
    private final UsuarioPersonalService usuarioPersonalService;
    private final AlunoRepository alunoRepository;
    private final HistoricoImportacaoAlunosRepository historicoRepository;

    /** Limite configurável via TREINOFLOW_IMPORTACAO_LIMITE_PROBLEMAS_PERCENTUAL (padrão: 10%). */
    @Value("${treinoflow.importacao.limite-problemas-percentual:10}")
    private double limiteProblemasPercentual;

    public ImportacaoAlunosController(AlunoService alunoService, UsuarioPersonalService usuarioPersonalService,
                                      AlunoRepository alunoRepository, HistoricoImportacaoAlunosRepository historicoRepository) {
        this.alunoService = alunoService;
        this.usuarioPersonalService = usuarioPersonalService;
        this.alunoRepository = alunoRepository;
        this.historicoRepository = historicoRepository;
    }

    @GetMapping("/configuracoes-alertas")
    public String configuracoesAlertas(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        model.addAttribute("limiteAtual", resolverLimite(personal));
        model.addAttribute("limitePadrao", limiteProblemasPercentual);
        return "aluno/configuracoes-alertas-importacao";
    }

    @PostMapping("/configuracoes-alertas")
    public String salvarConfiguracoesAlertas(Authentication authentication,
            @RequestParam String limitePercentual, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        try {
            double valor = Double.parseDouble(limitePercentual.replace(',', '.'));
            if (!Double.isFinite(valor) || valor < 0.1 || valor > 100.0) {
                throw new IllegalArgumentException("O limite deve estar entre 0,1% e 100%.");
            }
            personal.setLimiteProblemasImportacaoPercentual(valor);
            usuarioPersonalService.salvar(personal);
            redirectAttributes.addFlashAttribute("sucesso", "Limite de alerta salvo com sucesso.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", "Informe um percentual válido entre 0,1 e 100.");
        }
        return "redirect:/alunos/importacao/configuracoes-alertas";
    }

    private double resolverLimite(UsuarioPersonal personal) {
        Double individual = personal.getLimiteProblemasImportacaoPercentual();
        return individual != null && Double.isFinite(individual) && individual >= 0.1 && individual <= 100.0
                ? individual : limiteProblemasPercentual;
    }

    @GetMapping
    public String index() { return "aluno/importacao-csv"; }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        double limitePersonal = resolverLimite(personal);
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = parseDataFiltro(dataInicio);
        LocalDate fim = parseDataFiltro(dataFim);
        if (inicio == null) inicio = hoje.minusDays(29);
        if (fim == null) fim = hoje;
        if (inicio.isAfter(fim)) { LocalDate temporaria = inicio; inicio = fim; fim = temporaria; }

        final LocalDate inicioFiltro = inicio;
        final LocalDate fimFiltro = fim;
        List<HistoricoImportacaoAlunos> historicos = historicoRepository
                .findByPersonalIdOrderByDataImportacaoDesc(personal.getId()).stream()
                .filter(h -> h.getDataImportacao() != null
                        && !h.getDataImportacao().toLocalDate().isBefore(inicioFiltro)
                        && !h.getDataImportacao().toLocalDate().isAfter(fimFiltro))
                .collect(Collectors.toList());
        int analisadas = historicos.stream().mapToInt(HistoricoImportacaoAlunos::getLinhasAnalisadas).sum();
        int importados = historicos.stream().mapToInt(HistoricoImportacaoAlunos::getImportados).sum();
        int ignorados = historicos.stream().mapToInt(HistoricoImportacaoAlunos::getIgnorados).sum();
        int rejeitados = historicos.stream().mapToInt(HistoricoImportacaoAlunos::getRejeitados).sum();
        double percentualProblemas = analisadas == 0 ? 0.0 : (ignorados + rejeitados) * 100.0 / analisadas;
        model.addAttribute("limiteProblemasPercentual", String.format(Locale.ROOT, "%.1f", limitePersonal));
        model.addAttribute("alertaProblemas", analisadas > 0 && percentualProblemas >= limitePersonal);
        model.addAttribute("dataInicio", inicio.toString());
        model.addAttribute("dataFim", fim.toString());
        model.addAttribute("totalImportacoes", historicos.size());
        model.addAttribute("linhasAnalisadas", analisadas);
        model.addAttribute("importados", importados);
        model.addAttribute("ignorados", ignorados);
        model.addAttribute("rejeitados", rejeitados);
        model.addAttribute("percentualProblemas", String.format(Locale.ROOT, "%.1f", percentualProblemas));
        model.addAttribute("historicos", historicos.stream().limit(8).collect(Collectors.toList()));
        return "aluno/dashboard-importacoes-csv";
    }

    @GetMapping("/dashboard.csv")
    public ResponseEntity<byte[]> exportarDashboardCsv(Authentication authentication,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = parseDataFiltro(dataInicio);
        LocalDate fim = parseDataFiltro(dataFim);
        if (inicio == null) inicio = hoje.minusDays(29);
        if (fim == null) fim = hoje;
        if (inicio.isAfter(fim)) { LocalDate temporaria = inicio; inicio = fim; fim = temporaria; }
        final LocalDate inicioFiltro = inicio;
        final LocalDate fimFiltro = fim;
        List<HistoricoImportacaoAlunos> historicos = historicoRepository
                .findByPersonalIdOrderByDataImportacaoDesc(personal.getId()).stream()
                .filter(h -> h.getDataImportacao() != null
                        && !h.getDataImportacao().toLocalDate().isBefore(inicioFiltro)
                        && !h.getDataImportacao().toLocalDate().isAfter(fimFiltro))
                .collect(Collectors.toList());
        int analisadas = historicos.stream().mapToInt(HistoricoImportacaoAlunos::getLinhasAnalisadas).sum();
        int importados = historicos.stream().mapToInt(HistoricoImportacaoAlunos::getImportados).sum();
        int ignorados = historicos.stream().mapToInt(HistoricoImportacaoAlunos::getIgnorados).sum();
        int rejeitados = historicos.stream().mapToInt(HistoricoImportacaoAlunos::getRejeitados).sum();
        StringBuilder csv = new StringBuilder("\uFEFFTipo;Data;Arquivo;Importações;Linhas analisadas;Importados;Ignorados;Rejeitados;Percentual de problemas\r\n");
        double percentual = analisadas == 0 ? 0.0 : (ignorados + rejeitados) * 100.0 / analisadas;
        csv.append("RESUMO;").append(inicio).append(" a ").append(fim).append(";Todos os arquivos;")
                .append(historicos.size()).append(';').append(analisadas).append(';').append(importados).append(';')
                .append(ignorados).append(';').append(rejeitados).append(';')
                .append(String.format(Locale.ROOT, "%.1f%%", percentual)).append("\r\n");
        for (HistoricoImportacaoAlunos h : historicos) {
            int total = h.getLinhasAnalisadas();
            double problemas = total == 0 ? 0.0 : (h.getIgnorados() + h.getRejeitados()) * 100.0 / total;
            csv.append("IMPORTAÇÃO;").append(h.getDataImportacao()).append(';').append(csvCampo(h.getNomeArquivo()))
                    .append(";1;").append(total).append(';').append(h.getImportados()).append(';')
                    .append(h.getIgnorados()).append(';').append(h.getRejeitados()).append(';')
                    .append(String.format(Locale.ROOT, "%.1f%%", problemas)).append("\r\n");
        }
        String nome = "dashboard-importacoes-" + inicio + "-a-" + fim + ".csv";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String csvCampo(String valor) {
        String seguro = valor == null ? "" : valor;
        return "\"" + seguro.replace("\"", "\"\"") + "\"";
    }

    /** Relatório mensal de importações, limitado aos dados do personal autenticado. */
    @GetMapping("/relatorio-mensal")
    public String relatorioMensal(Authentication authentication,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        double limitePersonal = resolverLimite(personal);
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = parseDataFiltro(dataInicio);
        LocalDate fim = parseDataFiltro(dataFim);
        if (inicio == null) inicio = hoje.withDayOfMonth(1).minusMonths(5);
        if (fim == null) fim = hoje;
        if (inicio.isAfter(fim)) { LocalDate temporaria = inicio; inicio = fim; fim = temporaria; }

        List<Map<String, Object>> meses = montarResumoMensal(personal.getId(), inicio, fim, limitePersonal);
        model.addAttribute("dataInicio", inicio.toString());
        model.addAttribute("dataFim", fim.toString());
        model.addAttribute("meses", meses);
        model.addAttribute("quantidadeMeses", meses.size());
        model.addAttribute("limiteProblemasPercentual", String.format(Locale.ROOT, "%.1f", limitePersonal));
        model.addAttribute("existeAlertaMensal", meses.stream().anyMatch(m -> Boolean.TRUE.equals(m.get("alerta"))));
        model.addAttribute("totalImportacoes", meses.stream().mapToInt(m -> (Integer) m.get("importacoes")).sum());
        model.addAttribute("totalAnalisadas", meses.stream().mapToInt(m -> (Integer) m.get("analisadas")).sum());
        model.addAttribute("totalImportados", meses.stream().mapToInt(m -> (Integer) m.get("importados")).sum());
        model.addAttribute("totalIgnorados", meses.stream().mapToInt(m -> (Integer) m.get("ignorados")).sum());
        model.addAttribute("totalRejeitados", meses.stream().mapToInt(m -> (Integer) m.get("rejeitados")).sum());
        return "aluno/relatorio-mensal-importacoes-csv";
    }

    @GetMapping("/relatorio-mensal.csv")
    public ResponseEntity<byte[]> exportarRelatorioMensal(Authentication authentication,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        double limitePersonal = resolverLimite(personal);
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = parseDataFiltro(dataInicio);
        LocalDate fim = parseDataFiltro(dataFim);
        if (inicio == null) inicio = hoje.withDayOfMonth(1).minusMonths(5);
        if (fim == null) fim = hoje;
        if (inicio.isAfter(fim)) { LocalDate temporaria = inicio; inicio = fim; fim = temporaria; }

        List<Map<String, Object>> meses = montarResumoMensal(personal.getId(), inicio, fim, limitePersonal);
        StringBuilder csv = new StringBuilder("\uFEFFMês;Importações;Linhas analisadas;Importados;Ignorados;Rejeitados;Percentual de problemas;Situação;Variação vs. mês anterior (p.p.)\r\n");
        for (Map<String, Object> mes : meses) {
            csv.append(csvCampo(String.valueOf(mes.get("mes")))).append(';')
                    .append(mes.get("importacoes")).append(';').append(mes.get("analisadas")).append(';')
                    .append(mes.get("importados")).append(';').append(mes.get("ignorados")).append(';')
                    .append(mes.get("rejeitados")).append(';').append(mes.get("percentualProblemas")).append(';')
                    .append(Boolean.TRUE.equals(mes.get("alerta")) ? "ALERTA" : "NORMAL").append(';')
                    .append(mes.get("variacaoProblemas") == null ? "" : mes.get("variacaoProblemas"))
                    .append("\r\n");
        }
        String nome = "relatorio-mensal-importacoes-" + inicio + "-a-" + fim + ".csv";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private List<Map<String, Object>> montarResumoMensal(Long personalId, LocalDate inicio, LocalDate fim, double limitePersonal) {
        List<HistoricoImportacaoAlunos> historicos = historicoRepository
                .findByPersonalIdOrderByDataImportacaoDesc(personalId).stream()
                .filter(h -> h.getDataImportacao() != null
                        && !h.getDataImportacao().toLocalDate().isBefore(inicio)
                        && !h.getDataImportacao().toLocalDate().isAfter(fim))
                .collect(Collectors.toList());
        Map<YearMonth, List<HistoricoImportacaoAlunos>> agrupados = historicos.stream()
                .collect(Collectors.groupingBy(h -> YearMonth.from(h.getDataImportacao()), LinkedHashMap::new, Collectors.toList()));
        List<Map<String, Object>> resultado = new ArrayList<>();
        YearMonth mesAtual = YearMonth.from(inicio);
        YearMonth ultimoMes = YearMonth.from(fim);
        Double percentualAnterior = null;
        while (!mesAtual.isAfter(ultimoMes)) {
            List<HistoricoImportacaoAlunos> itens = agrupados.getOrDefault(mesAtual, List.of());
            int analisadas = itens.stream().mapToInt(HistoricoImportacaoAlunos::getLinhasAnalisadas).sum();
            int importados = itens.stream().mapToInt(HistoricoImportacaoAlunos::getImportados).sum();
            int ignorados = itens.stream().mapToInt(HistoricoImportacaoAlunos::getIgnorados).sum();
            int rejeitados = itens.stream().mapToInt(HistoricoImportacaoAlunos::getRejeitados).sum();
            double percentual = analisadas == 0 ? 0.0 : (ignorados + rejeitados) * 100.0 / analisadas;
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("mes", mesAtual.toString());
            linha.put("importacoes", itens.size());
            linha.put("analisadas", analisadas);
            linha.put("importados", importados);
            linha.put("ignorados", ignorados);
            linha.put("rejeitados", rejeitados);
            linha.put("percentualProblemas", String.format(Locale.ROOT, "%.1f%%", percentual));
            linha.put("alerta", analisadas > 0 && percentual >= limitePersonal);
            linha.put("variacaoProblemas", percentualAnterior == null ? null
                    : String.format(Locale.ROOT, "%+.1f", percentual - percentualAnterior));
            resultado.add(linha);
            percentualAnterior = percentual;
            mesAtual = mesAtual.plusMonths(1);
        }
        return resultado;
    }

    @GetMapping("/historico")
    public String historico(Authentication authentication,
            @RequestParam(required = false) String arquivo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";

        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        List<HistoricoImportacaoAlunos> todos =
                historicoRepository.findByPersonalIdOrderByDataImportacaoDesc(personal.getId());

        String arquivoFiltro = arquivo == null ? "" : arquivo.trim();
        String statusFiltro = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        LocalDate inicio = parseDataFiltro(dataInicio);
        LocalDate fim = parseDataFiltro(dataFim);

        List<HistoricoImportacaoAlunos> filtrados = todos.stream()
                .filter(h -> arquivoFiltro.isEmpty()
                        || (h.getNomeArquivo() != null && h.getNomeArquivo().toLowerCase(Locale.ROOT)
                                .contains(arquivoFiltro.toLowerCase(Locale.ROOT))))
                .filter(h -> statusFiltro.isEmpty() || statusFiltro.equals(statusHistorico(h)))
                .filter(h -> inicio == null || (h.getDataImportacao() != null
                        && !h.getDataImportacao().toLocalDate().isBefore(inicio)))
                .filter(h -> fim == null || (h.getDataImportacao() != null
                        && !h.getDataImportacao().toLocalDate().isAfter(fim)))
                .sorted(Comparator.comparing(HistoricoImportacaoAlunos::getDataImportacao,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        int tamanhoSeguro = (tamanho == 25 || tamanho == 50) ? tamanho : 10;
        int total = filtrados.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / tamanhoSeguro));
        int paginaSegura = Math.max(0, Math.min(pagina, totalPaginas - 1));
        int inicioPagina = Math.min(paginaSegura * tamanhoSeguro, total);
        int fimPagina = Math.min(inicioPagina + tamanhoSeguro, total);
        model.addAttribute("historicos", filtrados.subList(inicioPagina, fimPagina));
        model.addAttribute("arquivo", arquivoFiltro);
        model.addAttribute("status", statusFiltro);
        model.addAttribute("dataInicio", dataInicio == null ? "" : dataInicio);
        model.addAttribute("dataFim", dataFim == null ? "" : dataFim);
        model.addAttribute("pagina", paginaSegura);
        model.addAttribute("tamanho", tamanhoSeguro);
        model.addAttribute("totalRegistros", total);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("temAnterior", paginaSegura > 0);
        model.addAttribute("temProxima", paginaSegura < totalPaginas - 1);
        return "aluno/historico-importacao-csv";
    }

    @GetMapping("/historico/{id}/relatorio.csv")
    public ResponseEntity<byte[]> baixarRelatorioHistorico(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        HistoricoImportacaoAlunos historico = historicoRepository
                .findByIdAndPersonalId(id, personal.getId()).orElse(null);
        if (historico == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=historico-importacao-" + id + ".csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(historico.getRelatorioCsv().getBytes(StandardCharsets.UTF_8));
    }

    private static LocalDate parseDataFiltro(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try { return LocalDate.parse(valor.trim()); }
        catch (DateTimeParseException ex) { return null; }
    }

    private static String statusHistorico(HistoricoImportacaoAlunos h) {
        if (h.getRejeitados() > 0) return "REJEITADO";
        if (h.getIgnorados() > 0) return "IGNORADO";
        if (h.getImportados() > 0) return "IMPORTADO";
        return "SEM_RESULTADOS";
    }

    @GetMapping("/modelo.csv")
    public ResponseEntity<byte[]> modelo() {
        String csv = "\uFEFFNome;CPF;Sexo;E-mail;Telefone;Objetivo;Status;Data de nascimento;Data de início\r\n";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modelo-importacao-alunos.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/resultado-importacao.csv")
    public ResponseEntity<byte[]> baixarRelatorioResultadoImportacao(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<String[]> linhas = (List<String[]>) session.getAttribute("treinoflowResultadoImportacaoAlunos");
        if (linhas == null || linhas.isEmpty()) return ResponseEntity.noContent().build();
        StringBuilder csv = new StringBuilder("\uFEFFLinha;Nome;E-mail;Resultado;Motivo\r\n");
        for (String[] item : linhas) {
            for (int i = 0; i < 5; i++) {
                if (i > 0) csv.append(';');
                csv.append(csvCampo(i < item.length ? item[i] : ""));
            }
            csv.append("\r\n");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resultado-importacao-alunos.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/linhas-pendentes.csv")
    public ResponseEntity<byte[]> baixarLinhasPendentes(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<String[]> resultados = (List<String[]>) session.getAttribute("treinoflowResultadoImportacaoAlunos");
        if (resultados == null || resultados.isEmpty()) return ResponseEntity.noContent().build();
        StringBuilder csv = new StringBuilder("\uFEFFNome;CPF;Sexo;E-mail;Telefone;Objetivo;Status;Data de nascimento;Data de início\r\n");
        int quantidade = 0;
        for (String[] item : resultados) {
            if (item.length < 5 || "IMPORTADO".equals(item[3])) continue;
            // Os campos originais são preservados a partir do índice 5 para permitir correção e reenvio.
            for (int i = 5; i < 14; i++) {
                if (i > 5) csv.append(';');
                csv.append(csvCampo(i < item.length ? item[i] : ""));
            }
            csv.append("\r\n");
            quantidade++;
        }
        if (quantidade == 0) return ResponseEntity.noContent().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=linhas-pendentes-importacao-alunos.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/resultado-erros.csv")
    public ResponseEntity<byte[]> baixarRelatorioErros(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<String> erros = (List<String>) session.getAttribute("treinoflowErrosImportacaoAlunos");
        if (erros == null || erros.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        StringBuilder csv = new StringBuilder("\uFEFFLinha;Motivo\r\n");
        for (String erro : erros) {
            int separador = erro.indexOf(": ");
            String linha = separador > 0 ? erro.substring(0, separador) : "Linha não identificada";
            String motivo = separador > 0 ? erro.substring(separador + 2) : erro;
            csv.append(csvCampo(linha)).append(';').append(csvCampo(motivo)).append("\r\n");
        }
        session.removeAttribute("treinoflowErrosImportacaoAlunos");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=erros-importacao-alunos.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String csvCampo(String valor) {
        String seguro = valor == null ? "" : valor;
        return "\"" + seguro.replace("\"", "\"\"") + "\"";
    }


    @GetMapping("/validacao-erros.csv")
    public ResponseEntity<byte[]> baixarRelatorioErrosValidacao(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<String> erros = (List<String>) session.getAttribute("treinoflowErrosValidacaoAlunos");
        if (erros == null || erros.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        StringBuilder csv = new StringBuilder("\uFEFFLinha;Motivo\r\n");
        for (String erro : erros) {
            int separador = erro.indexOf(": ");
            String linha = separador > 0 ? erro.substring(0, separador).replaceFirst("^Linha ", "") : "Não identificada";
            String motivo = separador > 0 ? erro.substring(separador + 2) : erro;
            csv.append(csvCampo(linha)).append(';').append(csvCampo(motivo)).append("\r\n");
        }
        session.removeAttribute("treinoflowErrosValidacaoAlunos");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=erros-validacao-alunos.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/validar")
    public String validarAntesDeImportar(Authentication authentication,
            @RequestParam("arquivoValidacao") MultipartFile arquivo, Model model, HttpSession session) {
        session.removeAttribute("treinoflowErrosValidacaoAlunos");
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        if (arquivo == null || arquivo.isEmpty()) {
            model.addAttribute("erroValidacao", "Selecione um arquivo CSV para validar.");
            return "aluno/importacao-csv";
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            model.addAttribute("erroValidacao", "O arquivo excede o limite de 5 MB.");
            return "aluno/importacao-csv";
        }
        String nome = arquivo.getOriginalFilename();
        if (nome == null || !nome.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            model.addAttribute("erroValidacao", "Envie um arquivo com extensão .csv.");
            return "aluno/importacao-csv";
        }
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        List<List<String>> amostra = new ArrayList<>();
        List<String> problemas = new ArrayList<>();
        List<String> todosProblemas = new ArrayList<>();
        int total = 0, validas = 0, invalidas = 0;
        java.util.Set<String> cpfsNoArquivo = new java.util.HashSet<>();
        java.util.Set<String> emailsNoArquivo = new java.util.HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {
            String cabecalho = reader.readLine();
            if (cabecalho == null) {
                model.addAttribute("erroValidacao", "O arquivo CSV está vazio.");
                return "aluno/importacao-csv";
            }
            cabecalho = cabecalho.replace("\uFEFF", "").trim();
            List<String> colunas = parseLinha(cabecalho);
            if (colunas.isEmpty() || !"nome".equals(normalizar(colunas.get(0)))) {
                model.addAttribute("erroValidacao", "Cabeçalho inválido. A primeira coluna deve ser Nome.");
                return "aluno/importacao-csv";
            }
            String linha; int numero = 1;
            while ((linha = reader.readLine()) != null) {
                numero++;
                if (linha.isBlank()) continue;
                total++;
                List<String> c;
                try {
                    c = parseLinha(linha);
                } catch (IllegalArgumentException formatoCsv) {
                    invalidas++;
                    String detalhe = "Linha " + numero + ": " + formatoCsv.getMessage();
                    if (problemas.size() < 100) problemas.add(detalhe);
                    if (todosProblemas.size() < 10000) todosProblemas.add(detalhe);
                    continue;
                }
                if (c.size() != colunas.size()) {
                    invalidas++;
                    String detalhe = "Linha " + numero + ": quantidade de colunas diferente do cabeçalho (esperado " + colunas.size() + ", encontrado " + c.size() + ").";
                    if (problemas.size() < 100) problemas.add(detalhe);
                    if (todosProblemas.size() < 10000) todosProblemas.add(detalhe);
                    continue;
                }
                if (amostra.size() < 20) {
                    List<String> linhaExibida = new ArrayList<>();
                    for (int i = 0; i < 9; i++) linhaExibida.add(valor(c, i));
                    amostra.add(linhaExibida);
                }
                String problema = null;
                String nomeAluno = valor(c, 0);
                String emailValidacao = valor(c, 3);
                String telefoneValidacao = valor(c, 4);
                String objetivoValidacao = valor(c, 5);
                if (nomeAluno.isBlank()) problema = "Nome obrigatório.";
                else if (nomeAluno.length() > 120) problema = "Nome excede o limite de 120 caracteres.";
                else if (emailValidacao.length() > 150) problema = "E-mail excede o limite de 150 caracteres.";
                else if (telefoneValidacao.length() > 20) problema = "Telefone excede o limite de 20 caracteres.";
                else if (objetivoValidacao.length() > 100) problema = "Objetivo excede o limite de 100 caracteres.";
                String status = valor(c, 6);
                if (problema == null && !status.isBlank() && !List.of("ATIVO", "INATIVO").contains(status.toUpperCase(Locale.ROOT))) {
                    problema = "Status inválido; use ATIVO ou INATIVO.";
                }
                String sexo = valor(c, 2).toUpperCase(Locale.ROOT);
                if (problema == null && !sexo.isBlank() && !List.of("FEMININO", "MASCULINO", "NAO_INFORMADO").contains(sexo)) {
                    problema = "Sexo inválido; use FEMININO, MASCULINO ou NAO_INFORMADO.";
                }
                String email = valor(c, 3);
                if (problema == null && !email.isBlank() && !emailValido(email)) {
                    problema = "E-mail inválido.";
                }
                if (problema == null && !email.isBlank()) {
                    String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
                    if (!emailsNoArquivo.add(emailNormalizado)) {
                        problema = "E-mail repetido dentro do próprio arquivo.";
                    } else if (alunoRepository.existsByPersonalIdAndEmailIgnoreCase(personal.getId(), emailNormalizado)) {
                        problema = "E-mail já cadastrado para este personal.";
                    }
                }
                for (int idx : new int[]{7, 8}) {
                    String v = valor(c, idx);
                    if (problema == null && !v.isBlank()) {
                        try {
                            LocalDate dataInformada = LocalDate.parse(v);
                            if (dataInformada.isAfter(LocalDate.now())) {
                                problema = idx == 7 ? "Data de nascimento não pode ser futura." : "Data de início não pode ser futura.";
                            }
                        } catch (Exception e) { problema = "Data inválida; use AAAA-MM-DD."; }
                    }
                }
                String cpf = valor(c, 1).replaceAll("\\D", "");
                if (problema == null && !cpf.isBlank()) {
                    if (!cpfValido(cpf)) problema = "CPF inválido; informe 11 dígitos com verificadores válidos.";
                    else if (!cpfsNoArquivo.add(cpf)) problema = "CPF repetido dentro do próprio arquivo.";
                    else if (alunoRepository.existsByPersonalIdAndCpf(personal.getId(), cpf)) problema = "CPF já cadastrado para este personal.";
                }
                if (problema == null) validas++;
                else {
                    invalidas++;
                    String detalhe = "Linha " + numero + ": " + problema;
                    if (problemas.size() < 100) problemas.add(detalhe);
                    if (todosProblemas.size() < 10000) todosProblemas.add(detalhe);
                }
            }
        } catch (Exception e) {
            model.addAttribute("erroValidacao", "Não foi possível ler o CSV. Confira se está em UTF-8 e separado por ponto e vírgula.");
            return "aluno/importacao-csv";
        }
        if (!todosProblemas.isEmpty()) {
            session.setAttribute("treinoflowErrosValidacaoAlunos", todosProblemas);
        }
        model.addAttribute("relatorioErrosValidacaoDisponivel", !todosProblemas.isEmpty());
        model.addAttribute("validacaoExecutada", true);
        model.addAttribute("totalLinhasValidacao", total);
        model.addAttribute("linhasValidas", validas);
        model.addAttribute("linhasInvalidas", invalidas);
        model.addAttribute("amostraValidacao", amostra);
        model.addAttribute("problemasValidacao", problemas);
        model.addAttribute("maisProblemas", invalidas > problemas.size());
        return "aluno/importacao-csv";
    }

    @PostMapping
    public String importar(Authentication authentication, @RequestParam("arquivo") MultipartFile arquivo,
                           RedirectAttributes redirectAttributes, Model model, HttpSession session) {
        if (authentication == null || !authentication.isAuthenticated()) return "redirect:/login";
        if (arquivo == null || arquivo.isEmpty()) {
            model.addAttribute("erro", "Selecione um arquivo CSV para importar.");
            return "aluno/importacao-csv";
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            model.addAttribute("erro", "O arquivo excede o limite de 5 MB.");
            return "aluno/importacao-csv";
        }
        String nomeArquivo = arquivo.getOriginalFilename();
        if (nomeArquivo == null || !nomeArquivo.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            model.addAttribute("erro", "Envie um arquivo com extensão .csv. Exporte a lista do TreinoFlow ou use o modelo disponibilizado.");
            return "aluno/importacao-csv";
        }

        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        int importados = 0, ignorados = 0, linhasAnalisadas = 0;
        List<String> erros = new ArrayList<>();
        List<String> todosErros = new ArrayList<>();
        List<String[]> resultadoImportacao = new ArrayList<>();
        java.util.Set<String> emailsImportadosNoArquivo = new java.util.HashSet<>();
        session.removeAttribute("treinoflowErrosImportacaoAlunos");
        session.removeAttribute("treinoflowResultadoImportacaoAlunos");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {
            String cabecalho = reader.readLine();
            if (cabecalho == null) {
                model.addAttribute("erro", "O arquivo CSV está vazio.");
                return "aluno/importacao-csv";
            }
            cabecalho = cabecalho.replace("\uFEFF", "").trim();
            List<String> colunas = parseLinha(cabecalho);
            if (colunas.isEmpty() || !"nome".equals(normalizar(colunas.get(0)))) {
                model.addAttribute("erro", "Cabeçalho inválido. A primeira coluna deve ser Nome. Use o modelo CSV do TreinoFlow.");
                return "aluno/importacao-csv";
            }
            String linha; int numero = 1;
            while ((linha = reader.readLine()) != null) {
                numero++;
                if (linha.isBlank()) continue;
                linhasAnalisadas++;
                String nomeRelatorio = "";
                String emailRelatorio = "";
                List<String> dadosOriginais = new ArrayList<>();
                try {
                    List<String> c = parseLinha(linha);
                    dadosOriginais.addAll(c);
                    nomeRelatorio = valor(c, 0);
                    emailRelatorio = valor(c, 3);
                    if (c.size() != colunas.size()) {
                        throw new IllegalArgumentException("Quantidade de colunas diferente do cabeçalho (esperado " + colunas.size() + ", encontrado " + c.size() + ").");
                    }
                    if (c.isEmpty() || valor(c, 0).isBlank()) throw new IllegalArgumentException("Nome obrigatório.");
                    Aluno aluno = new Aluno();
                    String nomeAluno = valor(c, 0);
                    String emailImportacao = valor(c, 3);
                    String telefoneImportacao = valor(c, 4);
                    String objetivoImportacao = valor(c, 5);
                    validarComprimento(nomeAluno, 120, "Nome");
                    validarComprimento(emailImportacao, 150, "E-mail");
                    validarComprimento(telefoneImportacao, 20, "Telefone");
                    validarComprimento(objetivoImportacao, 100, "Objetivo");
                    aluno.setNome(nomeAluno);
                    String cpfInformado = valor(c, 1).replaceAll("\\D", "");
                    if (!cpfInformado.isBlank() && !cpfValido(cpfInformado)) {
                        throw new IllegalArgumentException("CPF inválido; informe 11 dígitos com verificadores válidos.");
                    }
                    // Persistir o CPF somente com dígitos para padronizar a comparação de duplicidade.
                    aluno.setCpf(cpfInformado.isBlank() ? null : cpfInformado);
                    String sexo = valor(c, 2).toUpperCase(Locale.ROOT);
                    if (!sexo.isBlank() && !List.of("FEMININO", "MASCULINO", "NAO_INFORMADO").contains(sexo)) {
                        throw new IllegalArgumentException("Sexo inválido; use FEMININO, MASCULINO ou NAO_INFORMADO.");
                    }
                    aluno.setSexo(sexo.isBlank() ? null : sexo);
                    String email = emailImportacao;
                    if (!email.isBlank() && !emailValido(email)) {
                        throw new IllegalArgumentException("E-mail inválido.");
                    }
                    aluno.setEmail(nuloSeVazio(email));
                    aluno.setTelefone(nuloSeVazio(telefoneImportacao));
                    aluno.setObjetivo(nuloSeVazio(objetivoImportacao));
                    String status = valor(c, 6);
                    status = status.isBlank() ? "ATIVO" : status.toUpperCase(Locale.ROOT);
                    if (!List.of("ATIVO", "INATIVO").contains(status)) {
                        throw new IllegalArgumentException("Status inválido; use ATIVO ou INATIVO.");
                    }
                    aluno.setStatus(status);
                    aluno.setDataNascimento(data(valor(c, 7), "data de nascimento"));
                    aluno.setDataInicio(data(valor(c, 8), "data de início"));
                    String emailNormalizado = aluno.getEmail() == null ? "" : aluno.getEmail().trim().toLowerCase(Locale.ROOT);
                    if (!emailNormalizado.isBlank()) {
                        if (!emailsImportadosNoArquivo.add(emailNormalizado)) {
                            ignorados++;
                            String detalhe = "Linha " + numero + ": E-mail repetido dentro do próprio arquivo; registro ignorado.";
                            if (erros.size() < 20) erros.add(detalhe);
                            if (todosErros.size() < 10000) todosErros.add(detalhe);
                            resultadoImportacao.add(montarResultado(numero, nomeRelatorio, emailRelatorio, "IGNORADO", "E-mail repetido dentro do próprio arquivo", dadosOriginais));
                            continue;
                        }
                        if (alunoRepository.existsByPersonalIdAndEmailIgnoreCase(personal.getId(), emailNormalizado)) {
                            ignorados++;
                            String detalhe = "Linha " + numero + ": E-mail já cadastrado para este personal; registro ignorado.";
                            if (erros.size() < 20) erros.add(detalhe);
                            if (todosErros.size() < 10000) todosErros.add(detalhe);
                            resultadoImportacao.add(montarResultado(numero, nomeRelatorio, emailRelatorio, "IGNORADO", "E-mail já cadastrado para este personal", dadosOriginais));
                            continue;
                        }
                    }
                    String cpf = aluno.getCpf() == null ? "" : aluno.getCpf().replaceAll("\\D", "");
                    if (!cpf.isBlank() && alunoRepository.existsByPersonalIdAndCpf(personal.getId(), cpf)) {
                        ignorados++;
                        if (erros.size() < 20) erros.add("Linha " + numero + ": CPF já cadastrado; registro ignorado.");
                        if (todosErros.size() < 10000) todosErros.add("Linha " + numero + ": CPF já cadastrado; registro ignorado.");
                        resultadoImportacao.add(montarResultado(numero, nomeRelatorio, emailRelatorio, "IGNORADO", "CPF já cadastrado para este personal", dadosOriginais));
                        continue;
                    }
                    alunoService.salvar(personal.getId(), aluno);
                    importados++;
                    resultadoImportacao.add(montarResultado(numero, nomeRelatorio, emailRelatorio, "IMPORTADO", "Cadastro criado com sucesso", dadosOriginais));
                } catch (RuntimeException ex) {
                    ignorados++;
                    String detalhe = "Linha " + numero + ": " + (ex.getMessage() == null ? "dados inválidos." : ex.getMessage());
                    if (erros.size() < 20) erros.add(detalhe);
                    if (todosErros.size() < 10000) todosErros.add(detalhe);
                    resultadoImportacao.add(montarResultado(numero, nomeRelatorio, emailRelatorio, "REJEITADO", ex.getMessage() == null ? "Dados inválidos" : ex.getMessage(), dadosOriginais));
                }
            }
        } catch (Exception ex) {
            model.addAttribute("erro", "Não foi possível ler o CSV. Confira se o arquivo está em UTF-8 e separado por ponto e vírgula.");
            return "aluno/importacao-csv";
        }
        model.addAttribute("importados", importados);
        model.addAttribute("ignorados", ignorados);
        model.addAttribute("linhasAnalisadas", linhasAnalisadas);
        model.addAttribute("relatorioResultadoDisponivel", !resultadoImportacao.isEmpty());
        if (!resultadoImportacao.isEmpty()) session.setAttribute("treinoflowResultadoImportacaoAlunos", resultadoImportacao);
        model.addAttribute("errosImportacao", erros);
        if (!todosErros.isEmpty()) session.setAttribute("treinoflowErrosImportacaoAlunos", todosErros);
        model.addAttribute("relatorioErrosDisponivel", !todosErros.isEmpty());
        try {
            HistoricoImportacaoAlunos historico = new HistoricoImportacaoAlunos();
            historico.setPersonal(personal);
            String nomeSeguro = nomeArquivo == null ? "importacao.csv" : nomeArquivo.replaceAll("[\\r\\n\\\\/]", "_");
            historico.setNomeArquivo(nomeSeguro.length() > 255 ? nomeSeguro.substring(0, 255) : nomeSeguro);
            historico.setLinhasAnalisadas(linhasAnalisadas);
            historico.setImportados(importados);
            historico.setIgnorados(ignorados);
            int rejeitados = (int) resultadoImportacao.stream().filter(item -> item.length > 3 && "REJEITADO".equals(item[3])).count();
            historico.setRejeitados(rejeitados);
            historico.setRelatorioCsv(gerarCsvResultado(resultadoImportacao));
            historicoRepository.save(historico);
            model.addAttribute("historicoSalvo", true);
        } catch (RuntimeException historicoErro) {
            model.addAttribute("avisoHistorico", "A importação foi processada, mas não foi possível salvar o histórico. Verifique o banco de dados.");
        }
        model.addAttribute("concluido", true);
        return "aluno/importacao-csv";
    }

    private static String gerarCsvResultado(List<String[]> resultados) {
        StringBuilder csv = new StringBuilder("\uFEFFLinha;Nome;E-mail;Resultado;Motivo\r\n");
        for (String[] item : resultados) {
            for (int i = 0; i < 5; i++) {
                if (i > 0) csv.append(';');
                csv.append(csvCampo(i < item.length ? item[i] : ""));
            }
            csv.append("\r\n");
        }
        return csv.toString();
    }

    private static String[] montarResultado(int numero, String nome, String email, String status, String motivo, List<String> dadosOriginais) {
        String[] resultado = new String[14];
        resultado[0] = String.valueOf(numero);
        resultado[1] = nome == null ? "" : nome;
        resultado[2] = email == null ? "" : email;
        resultado[3] = status;
        resultado[4] = motivo == null ? "Dados inválidos" : motivo;
        for (int i = 0; i < 9; i++) {
            resultado[5 + i] = dadosOriginais != null && i < dadosOriginais.size() ? dadosOriginais.get(i) : "";
        }
        return resultado;
    }

    private static String valor(List<String> valores, int indice) { return indice < valores.size() && valores.get(indice) != null ? valores.get(indice).trim() : ""; }
    private static void validarComprimento(String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw new IllegalArgumentException(campo + " excede o limite de " + maximo + " caracteres.");
        }
    }
    private static String nuloSeVazio(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private static LocalDate data(String valor, String campo) {
        if (valor == null || valor.isBlank()) return null;
        try { return LocalDate.parse(valor.trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Formato inválido para " + campo + "; use AAAA-MM-DD."); }
    }
    /** Valida formato e dígitos verificadores do CPF; CPF vazio é tratado como opcional pelo fluxo. */
    private static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) return false;
        if (cpf.chars().distinct().count() == 1) return false;
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
        int primeiro = 11 - (soma % 11);
        if (primeiro >= 10) primeiro = 0;
        if (primeiro != cpf.charAt(9) - '0') return false;
        soma = 0;
        for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
        int segundo = 11 - (soma % 11);
        if (segundo >= 10) segundo = 0;
        return segundo == cpf.charAt(10) - '0';
    }

    private static boolean emailValido(String email) {
        if (email == null) return false;
        String valor = email.trim();
        return valor.length() <= 150 && valor.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private static String normalizar(String s) { return s == null ? "" : s.trim().toLowerCase(Locale.ROOT); }
    private static List<String> parseLinha(String linha) {
        List<String> campos = new ArrayList<>(); StringBuilder atual = new StringBuilder(); boolean aspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char ch = linha.charAt(i);
            if (ch == '"') {
                if (aspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') { atual.append('"'); i++; }
                else aspas = !aspas;
            } else if (ch == ';' && !aspas) { campos.add(atual.toString()); atual.setLength(0); }
            else atual.append(ch);
        }
        if (aspas) {
            throw new IllegalArgumentException("Aspas não fechadas na linha CSV.");
        }
        campos.add(atual.toString()); return campos;
    }
}
