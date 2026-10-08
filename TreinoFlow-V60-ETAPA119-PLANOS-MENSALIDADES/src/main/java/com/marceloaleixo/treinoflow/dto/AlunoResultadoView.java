package com.marceloaleixo.treinoflow.dto;

import java.time.LocalDateTime;

public record AlunoResultadoView(Long alunoId, String nome, String assunto, LocalDateTime dataContato) {
}
