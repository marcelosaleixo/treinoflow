package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.Treino;
import com.marceloaleixo.treinoflow.entity.TreinoExercicio;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AlunoService;
import com.marceloaleixo.treinoflow.service.ExercicioService;
import com.marceloaleixo.treinoflow.service.TreinoExercicioService;
import com.marceloaleixo.treinoflow.service.TreinoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.marceloaleixo.treinoflow.entity.EventoCompartilhamento;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class TreinoController {

    @Autowired
    private TreinoService treinoService;
    @Autowired
    private TreinoExercicioService treinoExercicioService;
    @Autowired
    private ExercicioService exercicioService;
    @Autowired
    private AlunoService alunoService;
    @Autowired
    private UsuarioPersonalService usuarioPersonalService;

    @GetMapping("/alunos/{alunoId}/treinos")
    public String listar(@PathVariable Long alunoId, Authentication auth, Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size,
                         @RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "") String status) {
        UsuarioPersonal personal = personal(auth);
        Aluno aluno = alunoService.buscarPorId(alunoId, personal.getId());
        int paginaAtual = Math.max(0, page);
        int tamanhoPagina = Math.min(50, Math.max(5, size));
        model.addAttribute("aluno", aluno);
        String busca = q == null ? "" : q.trim();
        model.addAttribute("q", busca);
        String filtroStatus = status == null ? "" : status.trim().toUpperCase();
        if (!filtroStatus.equals("RASCUNHO") && !filtroStatus.equals("LIBERADO") && !filtroStatus.equals("REVOGADO")) filtroStatus = "";
        model.addAttribute("status", filtroStatus);
        model.addAttribute("pagina", treinoService.buscarPaginadoDoAluno(alunoId, personal.getId(), busca, filtroStatus, PageRequest.of(paginaAtual, tamanhoPagina)));
        return "treino/lista";
    }

    @GetMapping("/alunos/{alunoId}/treinos/novo")
    public String novo(@PathVariable Long alunoId, Authentication auth, Model model) {
        UsuarioPersonal personal = personal(auth);
        model.addAttribute("aluno", alunoService.buscarPorId(alunoId, personal.getId()));
        model.addAttribute("treino", new Treino());
        model.addAttribute("modoNovo", true);
        return "treino/form";
    }

    @GetMapping("/treinos/{treinoId}/historico-compartilhamento.csv")
    public ResponseEntity<byte[]> exportarHistoricoCompartilhamento(@PathVariable Long treinoId, Authentication auth) {
        UsuarioPersonal personal = personal(auth);
        Treino treino = treinoService.buscarPorIdComAluno(treinoId, personal.getId());
        List<EventoCompartilhamento> eventos = treinoService.listarHistoricoCompartilhamento(treinoId, personal.getId());

        StringBuilder csv = new StringBuilder("\uFEFFTreino;Aluno;Evento;Data e hora;Detalhes\r\n");
        for (EventoCompartilhamento evento : eventos) {
            csv.append(csv(treino.getNome())).append(';')
               .append(csv(treino.getAluno().getNome())).append(';')
               .append(csv(evento.getTipoEvento())).append(';')
               .append(csv(evento.getDataEvento().toString().replace('T', ' '))).append(';')
               .append(csv(evento.getDetalhe())).append("\r\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historico-compartilhamento-treino-" + treinoId + ".csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .contentLength(bytes.length)
                .body(bytes);
    }

    private String csv(String valor) {
        if (valor == null) return "\"\"";
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }

    @GetMapping("/treinos/{treinoId}/imprimir")
    public String imprimir(@PathVariable Long treinoId, Authentication auth, Model model) {
        UsuarioPersonal personal = personal(auth);
        Treino treino = treinoService.buscarPorIdComAluno(treinoId, personal.getId());
        model.addAttribute("treino", treino);
        model.addAttribute("aluno", treino.getAluno());
        model.addAttribute("itens", treinoExercicioService.listar(treinoId, personal.getId()));
        model.addAttribute("emitidoEm", java.time.LocalDateTime.now());
        return "treino/imprimir";
    }

    @GetMapping("/treinos/{treinoId}/editar")
    public String editar(@PathVariable Long treinoId, Authentication auth, Model model) {
        UsuarioPersonal personal = personal(auth);
        Treino treino = treinoService.buscarPorIdComAluno(treinoId, personal.getId());
        
        preencherEditor(model, treino, personal.getId());
        return "treino/editor";
    }

    @PostMapping("/alunos/{alunoId}/treinos/salvar")
    public String salvar(@PathVariable Long alunoId, @ModelAttribute Treino treino, Authentication auth, RedirectAttributes flash) {
        UsuarioPersonal personal = personal(auth);
        try {
            Treino salvo = treinoService.salvar(alunoId, personal.getId(), treino);
            flash.addFlashAttribute("sucesso", "Treino salvo. Agora adicione os exercícios.");
            return "redirect:/treinos/" + salvo.getId() + "/editar";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/alunos/" + alunoId + "/treinos/novo";
        }
    }

    @PostMapping("/treinos/{treinoId}/exercicios/adicionar")
    public String adicionar(@PathVariable Long treinoId, @RequestParam Long exercicioId,
            @ModelAttribute TreinoExercicio item, Authentication auth, RedirectAttributes flash) {
        UsuarioPersonal personal = personal(auth);
        try {
            treinoExercicioService.adicionar(treinoId, personal.getId(), exercicioId, item);
            flash.addFlashAttribute("sucesso", "Exercício adicionado ao treino.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/treinos/" + treinoId + "/editar";
    }

    @PostMapping("/treinos/{treinoId}/exercicios/{itemId}/mover")
    public String moverExercicio(@PathVariable Long treinoId, @PathVariable Long itemId,
            @RequestParam int direcao, Authentication auth, RedirectAttributes flash) {
        try {
            treinoExercicioService.mover(itemId, treinoId, personal(auth).getId(), direcao);
            flash.addFlashAttribute("sucesso", "Ordem dos exercícios atualizada.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/treinos/" + treinoId + "/editar";
    }

    @PostMapping("/treinos/{treinoId}/exercicios/{itemId}/atualizar")
    public String atualizarPrescricao(@PathVariable Long treinoId, @PathVariable Long itemId,
            @ModelAttribute TreinoExercicio dados, Authentication auth, RedirectAttributes flash) {
        try {
            treinoExercicioService.atualizarPrescricao(itemId, treinoId, personal(auth).getId(), dados);
            flash.addFlashAttribute("sucesso", "Prescrição do exercício atualizada.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/treinos/" + treinoId + "/editar";
    }

    @PostMapping("/treinos/{treinoId}/exercicios/{itemId}/excluir")
    public String excluirItem(@PathVariable Long treinoId, @PathVariable Long itemId, Authentication auth, RedirectAttributes flash) {
        try {
            treinoExercicioService.excluir(itemId, treinoId, personal(auth).getId());
            flash.addFlashAttribute("sucesso", "Exercício removido.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/treinos/" + treinoId + "/editar";
    }

    @PostMapping("/treinos/{treinoId}/duplicar")
    public String duplicar(@PathVariable Long treinoId, Authentication auth, RedirectAttributes flash) {
        try {
            Treino copia = treinoService.duplicar(treinoId, personal(auth).getId());
            flash.addFlashAttribute("sucesso", "Treino duplicado como rascunho. Revise a prescrição antes de liberar.");
            return "redirect:/treinos/" + copia.getId() + "/editar";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/alunos";
        }
    }

    @PostMapping("/treinos/{treinoId}/liberar")
    public String liberar(@PathVariable Long treinoId, @RequestParam(defaultValue = "30") Integer validadeDias, Authentication auth, RedirectAttributes flash) {
        try {
            treinoService.liberar(treinoId, personal(auth).getId(), validadeDias);
            flash.addFlashAttribute("sucesso", "Treino liberado por " + validadeDias + " dias. O link está disponível no editor.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/treinos/" + treinoId + "/editar";
    }

    @PostMapping("/treinos/{treinoId}/revogar")
    public String revogar(@PathVariable Long treinoId, Authentication auth, RedirectAttributes flash) {
        try {
            treinoService.revogarAcesso(treinoId, personal(auth).getId());
            flash.addFlashAttribute("sucesso", "Acesso público revogado. O treino voltou para rascunho.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/treinos/" + treinoId + "/editar";
    }

    @PostMapping("/treinos/{treinoId}/excluir")
    public String excluir(@PathVariable Long treinoId, Authentication auth, RedirectAttributes flash) {
        try {
            Long alunoId = treinoService.buscarPorId(treinoId, personal(auth).getId()).getAluno().getId();
            treinoService.excluir(treinoId, personal(auth).getId());
            flash.addFlashAttribute("sucesso", "Treino excluído.");
            return "redirect:/alunos/" + alunoId + "/treinos";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/alunos";
        }
    }

    private void preencherEditor(Model model, Treino treino, Long personalId) {
        model.addAttribute("treino", treino);
        model.addAttribute("aluno", treino.getAluno());
        model.addAttribute("itens", treinoExercicioService.listar(treino.getId(), personalId));
        model.addAttribute("exercicios", exercicioService.listarDisponiveis(personalId));
        model.addAttribute("novoItem", new TreinoExercicio());
        model.addAttribute("linkExpirado", treino.getAcessoExpiraEm() != null && !treino.getAcessoExpiraEm().isAfter(java.time.LocalDateTime.now()));
        model.addAttribute("historicoCompartilhamento", treinoService.listarHistoricoCompartilhamento(treino.getId(), personalId));
    }

    private UsuarioPersonal personal(Authentication auth) {
        return usuarioPersonalService.buscarPorEmail(auth.getName());
    }
}
