package com.marceloaleixo.treinoflow.security;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class PersonalUserDetailsService implements UserDetailsService {
    @Autowired
    private UsuarioPersonalRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioPersonal personal = usuarioRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
        return User.withUsername(personal.getEmail())
                .password(personal.getSenha())
                .roles(personal.getPerfil() == null ? "PERSONAL" : personal.getPerfil())
                .disabled(!personal.isAtivo())
                .build();
    }
}
