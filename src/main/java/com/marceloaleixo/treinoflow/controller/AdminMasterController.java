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
 @GetMapping("/personais/novo") public String novoPersonal(Model model){model.addAttribute("personal",new UsuarioPersonal());model.addAttribute("planos",planos.findAll());return "admin/personal-form";}
 @GetMapping("/personais/{id}/editar") public String editarPersonal(@PathVariable Long id,Model model){model.addAttribute("personal",personais.findById(id).orElseThrow());model.addAttribute("planos",planos.findAll());return "admin/personal-form";}
 @PostMapping("/personais/salvar") public String salvarPersonal(@RequestParam(required=false) Long id,@RequestParam String nome,@RequestParam String email,@RequestParam(required=false) String telefone,@RequestParam(required=false) String senha,@RequestParam(required=false) Long planoId,RedirectAttributes ra){
  String e=email.trim().toLowerCase(Locale.ROOT);
  if(nome.isBlank()||e.length()>150||!e.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")){ra.addFlashAttribute("erro","Informe nome e e-mail válido.");return "redirect:/admin/personais/novo";}
  UsuarioPersonal p=id==null?new UsuarioPersonal():personais.findById(id).orElseThrow();
  if(personais.findByEmail(e).filter(x->!x.getId().equals(p.getId())).isPresent()){ra.addFlashAttribute("erro","E-mail já cadastrado.");return "redirect:/admin/personais/novo";}
  p.setNome(nome.trim());p.setEmail(e);p.setTelefone(telefone);p.setPerfil("PERSONAL");
  if(senha!=null&&!senha.isBlank()){if(senha.length()<8){ra.addFlashAttribute("erro","Senha deve ter no mínimo 8 caracteres.");return "redirect:/admin/personais/novo";}p.setSenha(encoder.encode(senha));}
  if(id==null&&(senha==null||senha.isBlank())){ra.addFlashAttribute("erro","Informe uma senha inicial.");return "redirect:/admin/personais/novo";}
  p.setPlano(planoId==null?null:planos.findById(planoId).orElse(null));p.setAtivo(true);personais.save(p);ra.addFlashAttribute("sucesso","Cadastro do personal salvo.");return "redirect:/admin/personais";
 }
 @PostMapping("/personais/{id}/status") public String alternarStatus(@PathVariable Long id,RedirectAttributes ra){UsuarioPersonal p=personais.findById(id).orElseThrow();if("MASTER".equals(p.getPerfil())){ra.addFlashAttribute("erro","Não é permitido desativar o administrador master por esta tela.");}else{p.setAtivo(!p.isAtivo());personais.save(p);ra.addFlashAttribute("sucesso","Status atualizado.");}return "redirect:/admin/personais";}
 @GetMapping("/planos") public String listarPlanos(Model model){model.addAttribute("planos",planos.findAll());return "admin/planos";}
 @GetMapping("/planos/novo") public String novoPlano(Model model){model.addAttribute("form",new PlanoForm());return "admin/plano-form";}
 @GetMapping("/planos/{id}/editar") public String editarPlano(@PathVariable Long id,Model model){Plano p=planos.findById(id).orElseThrow();PlanoForm f=new PlanoForm();f.setNome(p.getNome());f.setDescricao(p.getDescricao());f.setValorMensal(p.getValorMensal());f.setLimiteAlunos(p.getLimiteAlunos());f.setAtivo(p.isAtivo());model.addAttribute("form",f);model.addAttribute("planoId",id);return "admin/plano-form";}
 @PostMapping("/planos/salvar") public String salvarPlano(@RequestParam(required=false) Long id,@Valid @ModelAttribute("form") PlanoForm form,BindingResult result,Model model,RedirectAttributes ra){
  if(result.hasErrors())return "admin/plano-form";
  if(planos.findAll().stream().anyMatch(x->x.getNome().equalsIgnoreCase(form.getNome().trim())&&(id==null||!x.getId().equals(id)))){model.addAttribute("erro","Já existe um plano com esse nome.");model.addAttribute("planoId",id);return "admin/plano-form";}
  Plano p=id==null?new Plano():planos.findById(id).orElseThrow();p.setNome(form.getNome().trim());p.setDescricao(form.getDescricao());p.setValorMensal(form.getValorMensal());p.setLimiteAlunos(form.getLimiteAlunos());p.setAtivo(form.isAtivo());planos.save(p);ra.addFlashAttribute("sucesso","Plano criado.");return "redirect:/admin/planos";
 }
 @PostMapping("/planos/{id}/status") public String statusPlano(@PathVariable Long id,RedirectAttributes ra){Plano p=planos.findById(id).orElseThrow();p.setAtivo(!p.isAtivo());planos.save(p);ra.addFlashAttribute("sucesso","Status do plano atualizado.");return "redirect:/admin/planos";}
}
