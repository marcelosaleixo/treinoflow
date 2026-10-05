package com.marceloaleixo.treinoflow.dto;

import com.marceloaleixo.treinoflow.enums.CanalNotificacao;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;

public class TemplateNotificacaoForm {
    private Long id;
    private TipoNotificacao tipo;
    private CanalNotificacao canal;
    private String titulo;
    private String mensagem;
    private boolean ativo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }
    public CanalNotificacao getCanal() { return canal; }
    public void setCanal(CanalNotificacao canal) { this.canal = canal; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
