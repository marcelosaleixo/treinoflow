package com.marceloaleixo.treinoflow.enums;

public enum TipoNotificacao {
    VENCIMENTO_7_DIAS("Cobrança vencendo em 7 dias"),
    VENCIMENTO_HOJE("Cobrança vence hoje"),
    COBRANCA_ATRASADA("Cobrança em atraso"),
    PAGAMENTO_CONFIRMADO("Pagamento confirmado"),
    ASSINATURA_VENCENDO("Assinatura próxima do vencimento"),
    ASSINATURA_BLOQUEADA("Assinatura bloqueada"),
    INADIMPLENCIA_3_DIAS("Inadimplência há 3 dias"),
    INADIMPLENCIA_7_DIAS("Inadimplência há 7 dias"),
    META_RECEITA_ATINGIDA("Meta de receita atribuída atingida"),
    META_RECEITA_ABAIXO_RITMO("Meta de receita atribuída abaixo do ritmo"),
    META_RECEITA_NAO_DEFINIDA("Meta de receita atribuída não definida"),
    META_RECEITA_MES_ENCERRADO("Meta de receita atribuída não atingida");

    private final String descricao;

    TipoNotificacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
