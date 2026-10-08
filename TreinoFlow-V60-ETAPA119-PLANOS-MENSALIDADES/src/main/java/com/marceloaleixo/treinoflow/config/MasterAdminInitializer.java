package com.marceloaleixo.treinoflow.config;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Locale;

@Configuration
public class MasterAdminInitializer {
 @Bean CommandLineRunner provisionarMaster(UsuarioPersonalRepository repo, PasswordEncoder encoder,
    @Value("${TREINOFLOW_MASTER_EMAIL:}") String email,
    @Value("${TREINOFLOW_MASTER_PASSWORD:}") String senha) {
  return args -> {
   if(email==null || email.isBlank() || senha==null || senha.length()<12) return;
   String normalizado=email.trim().toLowerCase(Locale.ROOT);
   UsuarioPersonal master=repo.findByEmail(normalizado).orElseGet(()->{
    UsuarioPersonal u=new UsuarioPersonal(); u.setNome("Administrador Master"); u.setEmail(normalizado); u.setSenha(encoder.encode(senha)); return u;
   });
   master.setPerfil("MASTER"); master.setAtivo(true); repo.save(master);
  };
 }
}
