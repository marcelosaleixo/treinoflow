package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.dto.PlanoForm;
import com.marceloaleixo.treinoflow.entity.Plano;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.PlanoRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Locale;

@Controller
@RequestMapping("/admin")
public class AdminMasterController {
 @Autowired private UsuarioPersonalRepository personais;
 @Autowired private PlanoRepository planos;
 @Autowired private PasswordEncoder encoder;

 @GetMapping public String dashboard(Model model){
  model.addAttribute("totalPersonais",personais.countByPerfil("PERSONAL"));
  model.addAttribute("personaisAtivos",personais.findAllComPlano().stream().filter(p->"PERSONAL".equals(p.getPerfil())&&p.isAtivo()).count());
  model.addAttribute("totalPlanos",planos.count());
  model.addAttribute("personaisRecentes",personais.findAllComPlano().stream().filter(p->"PERSONAL".equals(p.getPerfil())).sorted((a,b)->b.getDataCriacao().compareTo(a.getDataCriacao())).limit(8).toList());
  return "admin/dashboard";
 }
 @GetMapping("/personais") public String listarPersonais(Model model){model.addAttribute("personais",personais.findAllComPlano().stream().filter(p->"PERSONAL".equals(p.getPerfil())).toList()); return "admin/personais";}
 @GetMapping("/personais/novo") public String novoPersonal(Model model){
  UsuarioPersonal personal=new UsuarioPersonal();
  planos.findFirstByPadraoTrue().filter(Plano::isAtivo).ifPresent(personal::setPlano);
  model.addAttribute("personal",personal);
  model.addAttribute("planos",planos.findAll());
  return "admin/personal-form";
 }
 @GetMapping("/personais/{id}/editar") public String editarPersonal(@PathVariable Long id,Model model){model.addAttribute("personal",personais.findByIdComPlano(id).orElseThrow());model.addAttribute("planos",planos.findAll());return "admin/personal-form";}
 @PostMapping("/personais/salvar") public String salvarPersonal(@RequestParam(required=false) Long id,@RequestParam String nome,@RequestParam String email,@RequestParam(required=false) String telefone,@RequestParam(required=false) String senha,@RequestParam(required=false) Long planoId,RedirectAttributes ra){
  String e=email.trim().toLowerCase(Locale.ROOT);
  if(nome.isBlank()||e.length()>150||!e.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")){ra.addFlashAttribute("erro","Informe nome e e-mail válido.");return "redirect:/admin/personais/novo";}
  UsuarioPersonal p=id==null?new UsuarioPersonal():personais.findById(id).orElseThrow();
  if(personais.findByEmail(e).filter(x->!x.getId().equals(p.getId())).isPresent()){ra.addFlashAttribute("erro","E-mail já cadastrado.");return "redirect:/admin/personais/novo";}
  p.setNome(nome.trim());p.setEmail(e);p.setTelefone(telefone);p.setPerfil("PERSONAL");
  if(senha!=null&&!senha.isBlank()){if(senha.length()<8){ra.addFlashAttribute("erro","Senha deve ter no mínimo 8 caracteres.");return "redirect:/admin/personais/novo";}p.setSenha(encoder.encode(senha));}
  if(id==null&&(senha==null||senha.isBlank())){ra.addFlashAttribute("erro","Informe uma senha inicial.");return "redirect:/admin/personais/novo";}
  Plano planoSelecionado;
  if(planoId==null){
   planoSelecionado=planos.findFirstByPadraoTrue().filter(Plano::isAtivo).orElse(null);
  }else{
   planoSelecionado=planos.findById(planoId).orElse(null);
   if(planoSelecionado==null || !planoSelecionado.isAtivo()){
    ra.addFlashAttribute("erro","Selecione um plano ativo.");
    return "redirect:/admin/personais/novo";
   }
  }
  if(planoSelecionado==null){
   ra.addFlashAttribute("erro","Não há plano ativo disponível para este personal.");
   return "redirect:/admin/personais/novo";
  }
  p.setPlano(planoSelecionado);p.setAtivo(true);personais.save(p);ra.addFlashAttribute("sucesso","Cadastro do personal salvo com plano "+planoSelecionado.getNome()+".");return "redirect:/admin/personais";
 }
 @PostMapping("/personais/{id}/status") public String alternarStatus(@PathVariable Long id,RedirectAttributes ra){UsuarioPersonal p=personais.findById(id).orElseThrow();if("MASTER".equals(p.getPerfil())){ra.addFlashAttribute("erro","Não é permitido desativar o administrador master por esta tela.");}else{p.setAtivo(!p.isAtivo());personais.save(p);ra.addFlashAttribute("sucesso","Status atualizado.");}return "redirect:/admin/personais";}
 @GetMapping("/planos") public String listarPlanos(Model model){model.addAttribute("planos",planos.findAll());return "admin/planos";}
 @GetMapping("/planos/novo") public String novoPlano(Model model){model.addAttribute("form",new PlanoForm());return "admin/plano-form";}
 @GetMapping("/planos/{id}/editar") public String editarPlano(@PathVariable Long id,Model model){Plano p=planos.findById(id).orElseThrow();PlanoForm f=new PlanoForm();f.setNome(p.getNome());f.setDescricao(p.getDescricao());f.setValorMensal(p.getValorMensal());f.setLimiteAlunos(p.getLimiteAlunos());f.setAtivo(p.isAtivo());f.setPadrao(p.isPadrao());model.addAttribute("form",f);model.addAttribute("planoId",id);return "admin/plano-form";}
 @PostMapping("/planos/salvar") public String salvarPlano(@RequestParam(required=false) Long id,@Valid @ModelAttribute("form") PlanoForm form,BindingResult result,Model model,RedirectAttributes ra){
  if(result.hasErrors())return "admin/plano-form";
  if(planos.findAll().stream().anyMatch(x->x.getNome().equalsIgnoreCase(form.getNome().trim())&&(id==null||!x.getId().equals(id)))){model.addAttribute("erro","Já existe um plano com esse nome.");model.addAttribute("planoId",id);return "admin/plano-form";}
  Plano p=id==null?new Plano():planos.findById(id).orElseThrow();
  p.setNome(form.getNome().trim());p.setDescricao(form.getDescricao());p.setValorMensal(form.getValorMensal());p.setLimiteAlunos(form.getLimiteAlunos());p.setAtivo(form.isAtivo());
  boolean deveSerPadrao=form.isPadrao() || (id==null && planos.count()==0);
  if(deveSerPadrao && !p.isAtivo()){
   model.addAttribute("erro","O plano padrão precisa estar ativo.");
   model.addAttribute("planoId",id);
   return "admin/plano-form";
  }
  if(deveSerPadrao){
   planos.findAll().forEach(outro->{if(!outro.getId().equals(p.getId()) && outro.isPadrao()){outro.setPadrao(false);planos.save(outro);}});
  }
  p.setPadrao(deveSerPadrao);
  planos.save(p);
  if(!p.isAtivo() && p.isPadrao()){
   p.setPadrao(false);planos.save(p);
  }
  if(!planos.findFirstByPadraoTrue().isPresent()){
   planos.findFirstByAtivoTrueOrderByIdAsc().ifPresent(primeiro->{primeiro.setPadrao(true);planos.save(primeiro);});
  }
  ra.addFlashAttribute("sucesso","Plano salvo.");return "redirect:/admin/planos";
 }
 @PostMapping("/planos/{id}/status") public String statusPlano(@PathVariable Long id,RedirectAttributes ra){
  Plano p=planos.findById(id).orElseThrow();
  if(p.isAtivo() && p.isPadrao()){
   Plano outro=planos.findAll().stream().filter(x->!x.getId().equals(id)&&x.isAtivo()).findFirst().orElse(null);
   if(outro==null){ra.addFlashAttribute("erro","Não é possível desativar o único plano ativo e padrão.");return "redirect:/admin/planos";}
   p.setAtivo(false);p.setPadrao(false);planos.save(p);outro.setPadrao(true);planos.save(outro);
  }else{
   p.setAtivo(!p.isAtivo());planos.save(p);
   if(p.isAtivo() && !planos.findFirstByPadraoTrue().isPresent()){p.setPadrao(true);planos.save(p);}
  }
  ra.addFlashAttribute("sucesso","Status do plano atualizado.");return "redirect:/admin/planos";
 }
}
