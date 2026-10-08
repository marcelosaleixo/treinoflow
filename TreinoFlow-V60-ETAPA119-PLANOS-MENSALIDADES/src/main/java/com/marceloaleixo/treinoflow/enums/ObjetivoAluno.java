package com.marceloaleixo.treinoflow.enums;

/** Opções padronizadas para o objetivo principal do aluno. */
public enum ObjetivoAluno {
    EMAGRECIMENTO("Emagrecimento"),
    HIPERTROFIA("Hipertrofia"),
    CONDICIONAMENTO_FISICO("Condicionamento físico"),
    SAUDE_QUALIDADE_VIDA("Saúde e qualidade de vida"),
    RESISTENCIA_MUSCULAR("Resistência muscular"),
    PERFORMANCE_ESPORTIVA("Performance esportiva"),
    MOBILIDADE_FLEXIBILIDADE("Mobilidade e flexibilidade"),
    OUTRO("Outro");

    private final String descricao;

    ObjetivoAluno(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
