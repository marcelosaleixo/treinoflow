package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.CadastroPersonalForm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AutenticacaoService {
    @Autowired
    private UsuarioPersonalRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioPersonal cadastrar(CadastroPersonalForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!form.getSenha().equals(form.getConfirmarSenha()))
            throw new IllegalArgumentException("As senhas informadas não conferem.");
        if (usuarioRepository.existsByEmail(email))
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");

        UsuarioPersonal personal = new UsuarioPersonal();
        personal.setNome(form.getNome().trim());
        personal.setEmail(email);
        personal.setTelefone(form.getTelefone());
        personal.setSenha(passwordEncoder.encode(form.getSenha()));
        personal.setAtivo(true);
        return usuarioRepository.save(personal);
    }
}
