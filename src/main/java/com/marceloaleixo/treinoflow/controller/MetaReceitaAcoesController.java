package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.MetaReceitaAcoes;
import com.marceloaleixo.treinoflow.entity.InteracaoRecebimentoVinculo;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.MetaReceitaAcoesRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoRecebimentoVinculoRepository;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import com.marceloaleixo.treinoflow.event.SincronizarMetaReceitaEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/financeiro/metas-receita-acoes")
public class MetaReceitaAcoesController {
 private final UsuarioPersonalService personals; private final MetaReceitaAcoesRepository metas; private final InteracaoRecebimentoVinculoRepository vinculos; private final ApplicationEventPublisher eventos;
 public MetaReceitaAcoesController(UsuarioPersonalService personals, MetaReceitaAcoesRepository metas, InteracaoRecebimentoVinculoRepository vinculos, ApplicationEventPublisher eventos){this.personals=personals;this.metas=metas;this.vinculos=vinculos;this.eventos=eventos;}
 @GetMapping public String index(@RequestParam(required=false) Integer ano,@RequestParam(required=false) Integer mes, Authentication auth, Model model){
  UsuarioPersonal personal=personals.buscarPorEmail(auth.getName()); YearMonth periodo;
  try{periodo=YearMonth.of(ano==null?LocalDate.now().getYear():ano,mes==null?LocalDate.now().getMonthValue():mes);}catch(RuntimeException ex){periodo=YearMonth.now();}
  final YearMonth periodoSelecionado=periodo; LocalDate referencia=periodo.atDay(1); MetaReceitaAcoes meta=metas.findByPersonalIdAndMesReferencia(personal.getId(),referencia).orElse(null);
  List<InteracaoRecebimentoVinculo> pagamentos=vinculos.findByInteracaoPersonalIdOrderByDataVinculoDesc(personal.getId()).stream().filter(v->v.getPagamento()!=null&&v.getPagamento().getDataPagamento()!=null&&YearMonth.from(v.getPagamento().getDataPagamento()).equals(periodoSelecionado)).toList();
  BigDecimal realizado=pagamentos.stream().map(v->v.getPagamento().getValor()==null?BigDecimal.ZERO:v.getPagamento().getValor()).reduce(BigDecimal.ZERO,BigDecimal::add);
  BigDecimal alvo=meta==null||meta.getValorMeta()==null?BigDecimal.ZERO:meta.getValorMeta(); BigDecimal restante=alvo.subtract(realizado).max(BigDecimal.ZERO);
  BigDecimal percentual=alvo.signum()<=0?BigDecimal.ZERO:realizado.multiply(BigDecimal.valueOf(100)).divide(alvo,2,RoundingMode.HALF_UP);
  model.addAttribute("periodo",periodo);model.addAttribute("meta",meta);model.addAttribute("metaValor",alvo);model.addAttribute("realizado",realizado);model.addAttribute("restante",restante);model.addAttribute("percentual",percentual);model.addAttribute("pagamentos",pagamentos);model.addAttribute("metaAtingida",alvo.signum()>0&&realizado.compareTo(alvo)>=0);return "financeiro/metas-receita-acoes";
 }
 @PostMapping public String salvar(@RequestParam Integer ano,@RequestParam Integer mes,@RequestParam BigDecimal valorMeta,@RequestParam(required=false) String observacao,Authentication auth,RedirectAttributes flash){
  UsuarioPersonal personal=personals.buscarPorEmail(auth.getName()); YearMonth periodo;
  try{periodo=YearMonth.of(ano,mes);}catch(RuntimeException ex){flash.addFlashAttribute("erro","Informe um mês e ano válidos.");return "redirect:/financeiro/metas-receita-acoes";}
  if(valorMeta==null||valorMeta.signum()<0||valorMeta.scale()>2){flash.addFlashAttribute("erro","A meta deve ser um valor não negativo com até duas casas decimais.");return "redirect:/financeiro/metas-receita-acoes?ano="+ano+"&mes="+mes;}
  MetaReceitaAcoes meta=metas.findByPersonalIdAndMesReferencia(personal.getId(),periodo.atDay(1)).orElseGet(MetaReceitaAcoes::new);meta.setPersonal(personal);meta.setMesReferencia(periodo.atDay(1));meta.setValorMeta(valorMeta);meta.setObservacao(observacao==null?null:observacao.trim());metas.save(meta);eventos.publishEvent(new SincronizarMetaReceitaEvent(personal.getId(), periodo));flash.addFlashAttribute("sucesso","Meta mensal salva.");return "redirect:/financeiro/metas-receita-acoes?ano="+ano+"&mes="+mes;
 }
}
