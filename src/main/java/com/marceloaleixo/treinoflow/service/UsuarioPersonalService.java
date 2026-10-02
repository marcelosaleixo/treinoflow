package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
@Transactional
public class UsuarioPersonalService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    @Autowired
    private UsuarioPersonalRepository repository;

    @Transactional(readOnly = true)
    public UsuarioPersonal buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail do personal não informado.");
        }
        return repository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("Personal autenticado não encontrado."));
    }

    @Transactional(readOnly = true)
    public UsuarioPersonal buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Personal não encontrado: " + id));
    }

    public UsuarioPersonal salvar(UsuarioPersonal personal) {
        if (personal.getNome() == null || personal.getNome().isBlank() || personal.getNome().trim().length() > 120)
            throw new IllegalArgumentException("Nome é obrigatório (até 120 caracteres).");
        if (personal.getEmail() == null || !EMAIL.matcher(personal.getEmail().trim()).matches() || personal.getEmail().trim().length() > 150)
            throw new IllegalArgumentException("Informe um e-mail válido.");
        personal.setEmail(personal.getEmail().trim().toLowerCase(Locale.ROOT));
        if (personal.getSenha() == null || personal.getSenha().isBlank())
            throw new IllegalArgumentException("Senha é obrigatória.");
        if (repository.findByEmail(personal.getEmail()).filter(existing -> !existing.getId().equals(personal.getId())).isPresent())
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        return repository.save(personal);
    }
}
