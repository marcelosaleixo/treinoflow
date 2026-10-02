package com.marceloaleixo.treinoflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CadastroPersonalForm {
    @NotBlank @Size(max = 120)
    private String nome;
    @NotBlank @Email @Size(max = 150)
    private String email;
    @Size(max = 20)
    private String telefone;
    @NotBlank @Size(min = 8, max = 72)
    private String senha;
    @NotBlank
    private String confirmarSenha;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getConfirmarSenha() { return confirmarSenha; }
    public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }
}
