package com.marceloaleixo.treinoflow.enums;

/**
 * Grupos musculares disponíveis para classificação dos exercícios. Os nomes das
 * constantes são persistidos no banco; os rótulos são exibidos na interface.
 */
public enum GrupoMuscular {
    PEITO("Peito"),
    COSTAS("Costas"),
    OMBROS("Ombros"),
    BICEPS("Bíceps"),
    TRICEPS("Tríceps"),
    ANTEBRACOS("Antebraços"),
    ABDOMEN("Abdômen"),
    LOMBAR("Lombar"),
    GLUTEOS("Glúteos"),
    QUADRICEPS("Quadríceps"),
    POSTERIORES_COXA("Posteriores de coxa"),
    ADUTORES("Adutores"),
    ABDUTORES("Abdutores"),
    PANTURRILHAS("Panturrilhas"),
    TRAPEZIO("Trapézio"),
    CORPO_TODO("Corpo todo"),
    OUTRO("Outro");

    private final String rotulo;

    GrupoMuscular(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
