package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ObjetivoAluno;
import com.marceloaleixo.treinoflow.service.AlunoService;
import com.marceloaleixo.treinoflow.service.PortalAlunoService;
import com.marceloaleixo.treinoflow.service.PerfilInteligenteAlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.data.domain.Page;

@Controller
public class AlunoController {

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private UsuarioPersonalService usuarioPersonalService;

    @Autowired
    private PortalAlunoService portalAlunoService;

    @Autowired
    private PerfilInteligenteAlunoService perfilInteligenteAlunoService;

    @GetMapping("/alunos")
    public String listar(Authentication authentication, Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size,
                         @RequestParam(required = false) String q) {
        UsuarioPersonal personal = personalAutenticado(authentication);
        int paginaAtual = Math.max(0, page);
        int tamanhoPagina = Math.min(50, Math.max(5, size));
        model.addAttribute("pagina", alunoService.listarPaginado(personal.getId(), q, PageRequest.of(paginaAtual, tamanhoPagina)));
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("personal", personal);
        return "aluno/lista";
    }

    @GetMapping(value = "/alunos/exportar.csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportarCsv(Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String objetivo,
            @RequestParam(defaultValue = "nome") String ordenar) {
        UsuarioPersonal personal = personalAutenticado(authentication);
        String campoOrdenacao = switch (ordenar == null ? "nome" : ordenar.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "cadastro" -> "dataCriacao";
            case "inicio" -> "dataInicio";
            default -> "nome";
        };
        Sort sort = Sort.by(Sort.Direction.ASC, campoOrdenacao).and(Sort.by(Sort.Direction.ASC, "id"));
        java.util.List<Aluno> alunos = alunoService.listarParaExportacao(personal.getId(), q, status, objetivo,
                PageRequest.of(0, 500, sort));
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Nome;CPF;Sexo;E-mail;Telefone;Objetivo;Status;Data de nascimento;Data de início;Data de cadastro\r\n");
        for (Aluno aluno : alunos) {
            csv.append(csv(aluno.getNome())).append(';')
               .append(csv(aluno.getCpf())).append(';')
               .append(csv(aluno.getSexo())).append(';')
               .append(csv(aluno.getEmail())).append(';')
               .append(csv(aluno.getTelefone())).append(';')
               .append(csv(aluno.getObjetivo())).append(';')
               .append(csv(aluno.getStatus())).append(';')
               .append(csv(aluno.getDataNascimento() == null ? "" : aluno.getDataNascimento().toString())).append(';')
               .append(csv(aluno.getDataInicio() == null ? "" : aluno.getDataInicio().toString())).append(';')
               .append(csv(aluno.getDataCriacao() == null ? "" : aluno.getDataCriacao().toString())).append("\r\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alunos-treinoflow.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @GetMapping(value = "/alunos/exportar.xlsx")
    public ResponseEntity<byte[]> exportarExcel(Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String objetivo,
            @RequestParam(defaultValue = "nome") String ordenar) throws IOException {
        UsuarioPersonal personal = personalAutenticado(authentication);
        String campoOrdenacao = switch (ordenar == null ? "nome" : ordenar.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "cadastro" -> "dataCriacao";
            case "inicio" -> "dataInicio";
            default -> "nome";
        };
        Sort sort = Sort.by(Sort.Direction.ASC, campoOrdenacao).and(Sort.by(Sort.Direction.ASC, "id"));
        java.util.List<Aluno> alunos = alunoService.listarParaExportacao(personal.getId(), q, status, objetivo,
                PageRequest.of(0, 10000, sort));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Alunos");
            String[] cabecalhos = {"Nome", "CPF", "Sexo", "E-mail", "Telefone", "Objetivo", "Status",
                    "Data de nascimento", "Data de início", "Data de cadastro"};
            Font font = workbook.createFont(); font.setBold(true);
            CellStyle estiloCabecalho = workbook.createCellStyle(); estiloCabecalho.setFont(font);
            Row header = sheet.createRow(0);
            for (int i = 0; i < cabecalhos.length; i++) {
                Cell cell = header.createCell(i); cell.setCellValue(cabecalhos[i]); cell.setCellStyle(estiloCabecalho);
            }
            int linha = 1;
            for (Aluno aluno : alunos) {
                Row row = sheet.createRow(linha++);
                String[] valores = {textoExcel(aluno.getNome()), textoExcel(aluno.getCpf()), textoExcel(aluno.getSexo()),
                    textoExcel(aluno.getEmail()), textoExcel(aluno.getTelefone()), textoExcel(aluno.getObjetivo()),
                    textoExcel(aluno.getStatus()), dataExcel(aluno.getDataNascimento()), dataExcel(aluno.getDataInicio()),
                    dataExcel(aluno.getDataCriacao())};
                for (int i = 0; i < valores.length; i++) row.createCell(i).setCellValue(valores[i]);
            }
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, linha - 1), 0, cabecalhos.length - 1));
            for (int i = 0; i < cabecalhos.length; i++) sheet.autoSizeColumn(i);
            workbook.write(output);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alunos-treinoflow.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(output.toByteArray());
        }
    }

    private String textoExcel(String valor) { return valor == null ? "" : valor.replace("\r", " ").replace("\n", " ").trim(); }
    private String dataExcel(java.time.temporal.TemporalAccessor data) { return data == null ? "" : data.toString(); }

    private String csv(String valor) {
        if (valor == null) return "\"\"";
        String seguro = valor.replace("\r", " ").replace("\n", " ");
        // Evita que planilhas interpretem valores iniciados por caracteres de fórmula.
        if (!seguro.isEmpty() && "=+-@".indexOf(seguro.charAt(0)) >= 0) seguro = "'" + seguro;
        return "\"" + seguro.replace("\"", "\"\"") + "\"";
    }

    @GetMapping("/alunos/novo")
    public String novo(Model model) {
        model.addAttribute("aluno", new Aluno());
        model.addAttribute("titulo", "Novo aluno");
        model.addAttribute("dataAtual", LocalDate.now());
        adicionarObjetivos(model);
        return "aluno/form";
    }

    @GetMapping("/alunos/{id}/editar")
    public String editar(@PathVariable Long id, Authentication authentication, Model model) {
        UsuarioPersonal personal = personalAutenticado(authentication);
        model.addAttribute("aluno", alunoService.buscarPorId(id, personal.getId()));
        model.addAttribute("titulo", "Editar aluno");
        model.addAttribute("dataAtual", LocalDate.now());
        adicionarObjetivos(model);
        return "aluno/form";
    }

    @GetMapping("/alunos/{id}/perfil-inteligente")
    public String perfilInteligente(@PathVariable Long id, Authentication authentication, Model model) {
        UsuarioPersonal personal = personalAutenticado(authentication);
        Aluno aluno = alunoService.buscarPorId(id, personal.getId());
        model.addAttribute("aluno", aluno);
        model.addAttribute("perfil", perfilInteligenteAlunoService.montar(aluno));
        return "aluno/perfil-inteligente";
    }

    @GetMapping("/alunos/{id}/portal")
    public String abrirPortal(@PathVariable Long id, Authentication authentication) {
        UsuarioPersonal personal = personalAutenticado(authentication);
        String token = portalAlunoService.obterOuCriarToken(id, personal.getId());
        return "redirect:/portal/" + token;
    }

    @PostMapping("/alunos/salvar")
    public String salvar(@ModelAttribute("aluno") Aluno aluno,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            UsuarioPersonal personal = personalAutenticado(authentication);
            alunoService.salvar(personal.getId(), aluno);
            redirectAttributes.addFlashAttribute("sucesso", "Aluno salvo com sucesso.");
            return "redirect:/alunos";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            return aluno.getId() == null ? "redirect:/alunos/novo" : "redirect:/alunos/" + aluno.getId() + "/editar";
        }
    }

    @PostMapping("/alunos/{id}/excluir")
    public String excluir(@PathVariable Long id,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        try {
            UsuarioPersonal personal = personalAutenticado(authentication);
            alunoService.excluir(id, personal.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Aluno inativado com sucesso. O histórico foi preservado.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/alunos";
    }

    private void adicionarObjetivos(Model model) {
        model.addAttribute("objetivos", java.util.Arrays.stream(ObjetivoAluno.values())
                .map(ObjetivoAluno::getDescricao)
                .toList());
    }

    private UsuarioPersonal personalAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
