package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.ExercicioRepository;
import com.marceloaleixo.treinoflow.repository.TreinoRepository;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import com.marceloaleixo.treinoflow.service.NotificacaoService;
import com.marceloaleixo.treinoflow.service.ScoreRiscoAlunoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioPersonalService usuarioPersonalService;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private ExercicioRepository exercicioRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private ScoreRiscoAlunoService scoreRiscoAlunoService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Authentication authentication, Model model) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER"))) return "redirect:/admin";
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        Long personalId = personal.getId();

        model.addAttribute("personal", personal);
        model.addAttribute("notificacoesNaoLidas", notificacaoService.contarNaoLidas(personalId));
        model.addAttribute("ultimasNotificacoes", notificacaoService.ultimas(personalId));
        model.addAttribute("totalAlunos", alunoRepository.countByPersonalIdAndStatus(personalId, "ATIVO"));
        model.addAttribute("totalTreinos", treinoRepository.contarTodosDoPersonal(personalId));
        model.addAttribute("treinosLiberados", treinoRepository.contarPorPersonalEStatus(personalId, "LIBERADO"));
        model.addAttribute("totalExercicios", exercicioRepository.contarDisponiveisParaPersonal(personalId));

        var riscos = scoreRiscoAlunoService.listar(personalId);
        var atencaoHoje = riscos.stream().filter(r -> r.score() >= 25).limit(5).toList();
        long criticosHoje = riscos.stream().filter(r -> r.score() >= 75).count();
        long altosHoje = riscos.stream().filter(r -> r.score() >= 50 && r.score() < 75).count();
        model.addAttribute("atencaoHoje", atencaoHoje);
        model.addAttribute("criticosHoje", criticosHoje);
        model.addAttribute("altosHoje", altosHoje);
        model.addAttribute("treinosRecentes", treinoRepository.buscarRecentesDoPersonal(personalId, PageRequest.of(0, 5)));
        model.addAttribute("treinosCompartilhados", treinoRepository.buscarTreinosLiberadosComVisualizacoes(personalId, PageRequest.of(0, 5)));
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        model.addAttribute("linksExpirando", treinoRepository.buscarLinksExpirando(personalId, agora, agora.plusDays(7)));
        return "dashboard/index";
    }
}
