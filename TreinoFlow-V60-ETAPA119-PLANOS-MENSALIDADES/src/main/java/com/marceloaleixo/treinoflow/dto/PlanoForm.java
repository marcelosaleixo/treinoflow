package com.marceloaleixo.treinoflow.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class PlanoForm {
 @NotBlank @Size(max=80) private String nome;
 @Size(max=500) private String descricao;
 @NotNull @DecimalMin("0.00") private BigDecimal valorMensal;
 @NotNull @Min(1) @Max(100000) private Integer limiteAlunos;
 private boolean ativo=true;
 private boolean padrao=false;
 public String getNome(){return nome;} public void setNome(String v){nome=v;}
 public String getDescricao(){return descricao;} public void setDescricao(String v){descricao=v;}
 public BigDecimal getValorMensal(){return valorMensal;} public void setValorMensal(BigDecimal v){valorMensal=v;}
 public Integer getLimiteAlunos(){return limiteAlunos;} public void setLimiteAlunos(Integer v){limiteAlunos=v;}
 public boolean isAtivo(){return ativo;} public void setAtivo(boolean v){ativo=v;}
 public boolean isPadrao(){return padrao;} public void setPadrao(boolean v){padrao=v;}
}
