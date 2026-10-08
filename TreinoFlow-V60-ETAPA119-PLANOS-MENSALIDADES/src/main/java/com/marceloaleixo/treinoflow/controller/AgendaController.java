package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Agendamento;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AgendamentoService;
import com.marceloaleixo.treinoflow.service.AlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/agenda")
public class AgendaController {
    private final AgendamentoService agendamentos;
    private final AlunoService alunos;
    private final UsuarioPersonalService personals;

    public AgendaController(AgendamentoService agendamentos,
                            AlunoService alunos,
                            UsuarioPersonalService personals) {
        this.agendamentos = agendamentos;
        this.alunos = alunos;
        this.personals = personals;
    }

    @GetMapping
    public String agenda(Authentication authentication,
                         @RequestParam(required = false) String data,
                         Model model) {
        UsuarioPersonal personal = personal(authentication);
        LocalDate dia = parseData(data);
        model.addAttribute("personal", personal);
        model.addAttribute("data", dia);
        var agendaDoDia = agendamentos.listarDoDia(personal.getId(), dia);
        model.addAttribute("agendamentos", agendaDoDia);
        model.addAttribute("total", agendaDoDia.size());
        model.addAttribute("agora", LocalDateTime.now());
        return "agenda/index";
    }

    @GetMapping("/novo")
    public String novo(Authentication authentication,
                       @RequestParam(required = false) String data,
                       Model model) {
        UsuarioPersonal personal = personal(authentication);
        LocalDate dia = parseData(data);
        Agendamento agendamento = new Agendamento();
        agendamento.setAluno(new com.marceloaleixo.treinoflow.entity.Aluno());
        agendamento.setInicio(agendamentos.inicioPadrao(dia));
        agendamento.setFim(agendamentos.fimPadrao(dia));
        agendamento.setStatus("AGENDADO");
        agendamento.setTipo("TREINO");
        model.addAttribute("personal", personal);
        model.addAttribute("alunos", alunos.listarAtivosPorPersonal(personal.getId()));
        model.addAttribute("agendamento", agendamento);
        model.addAttribute("statusDisponiveis", agendamentos.statusDisponiveis());
        model.addAttribute("tiposDisponiveis", agendamentos.tiposDisponiveis());
        model.addAttribute("titulo", "Novo agendamento");
        return "agenda/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        model.addAttribute("alunos", alunos.listarAtivosPorPersonal(personal.getId()));
        model.addAttribute("agendamento", agendamentos.buscar(id, personal.getId()));
        model.addAttribute("statusDisponiveis", agendamentos.statusDisponiveis());
        model.addAttribute("tiposDisponiveis", agendamentos.tiposDisponiveis());
        model.addAttribute("titulo", "Editar agendamento");
        return "agenda/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("agendamento") Agendamento agendamento,
                         Authentication authentication,
                         RedirectAttributes flash) {
        try {
            agendamentos.salvar(personal(authentication).getId(), agendamento);
            flash.addFlashAttribute("sucesso", "Agendamento salvo com sucesso.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        LocalDate dia = agendamento.getInicio() == null ? LocalDate.now() : agendamento.getInicio().toLocalDate();
        return "redirect:/agenda?data=" + dia;
    }

    @PostMapping("/{id}/status")
    public String status(@PathVariable Long id,
                         @RequestParam String status,
                         @RequestParam(required = false) String data,
                         Authentication authentication,
                         RedirectAttributes flash) {
        try {
            agendamentos.alterarStatus(id, personal(authentication).getId(), status);
            flash.addFlashAttribute("sucesso", "Status atualizado.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/agenda?data=" + (data == null || data.isBlank() ? LocalDate.now() : data);
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id,
                          @RequestParam(required = false) String data,
                          Authentication authentication,
                          RedirectAttributes flash) {
        try {
            agendamentos.excluir(id, personal(authentication).getId());
            flash.addFlashAttribute("sucesso", "Agendamento excluído.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/agenda?data=" + (data == null || data.isBlank() ? LocalDate.now() : data);
    }

    private LocalDate parseData(String valor) {
        if (valor == null || valor.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(valor);
        } catch (Exception ex) {
            return LocalDate.now();
        }
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return personals.buscarPorEmail(authentication.getName());
    }
}
