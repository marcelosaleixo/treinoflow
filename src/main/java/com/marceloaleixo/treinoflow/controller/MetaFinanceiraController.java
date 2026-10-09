package com.marceloaleixo.treinoflow.controller;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.MetaFinanceiraService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.YearMonth;

@Controller @RequestMapping("/financeiro/metas")
public class MetaFinanceiraController {
 private final UsuarioPersonalService personals; private final MetaFinanceiraService service;
 public MetaFinanceiraController(UsuarioPersonalService personals,MetaFinanceiraService service){this.personals=personals;this.service=service;}
 @GetMapping public String index(Authentication auth,@RequestParam(required=false) Integer ano,@RequestParam(required=false) Integer mes,Model model){UsuarioPersonal p=personals.buscarPorEmail(auth.getName());YearMonth periodo=(ano!=null&&mes!=null&&mes>=1&&mes<=12)?YearMonth.of(ano,mes):YearMonth.now();var meta=service.buscar(p.getId(),periodo);BigDecimal receita=service.receitaRealizada(p.getId(),periodo),gasto=service.despesasRealizadas(p.getId(),periodo);int progresso=meta.getMetaReceita().signum()==0?0:receita.multiply(BigDecimal.valueOf(100)).divide(meta.getMetaReceita(),0,java.math.RoundingMode.HALF_UP).min(BigDecimal.valueOf(100)).intValue();model.addAttribute("personal",p);model.addAttribute("periodo",periodo);model.addAttribute("meta",meta);model.addAttribute("receitaRealizada",receita);model.addAttribute("despesasRealizadas",gasto);model.addAttribute("resultado",receita.subtract(gasto));model.addAttribute("progresso",progresso);model.addAttribute("excessoDespesas",meta.getLimiteDespesas().signum()>0&&gasto.compareTo(meta.getLimiteDespesas())>0);return "financeiro/metas";}
 @PostMapping public String salvar(Authentication auth,@RequestParam Integer ano,@RequestParam Integer mes,@RequestParam BigDecimal metaReceita,@RequestParam BigDecimal limiteDespesas,@RequestParam(required=false) String observacao,RedirectAttributes redirect){try{if(mes<1||mes>12)throw new IllegalArgumentException("Mês inválido.");service.salvar(personals.buscarPorEmail(auth.getName()).getId(),YearMonth.of(ano,mes),metaReceita,limiteDespesas,observacao);redirect.addFlashAttribute("sucesso","Metas financeiras salvas.");}catch(Exception e){redirect.addFlashAttribute("erro",e.getMessage());}return "redirect:/financeiro/metas?ano="+ano+"&mes="+mes;}
}
