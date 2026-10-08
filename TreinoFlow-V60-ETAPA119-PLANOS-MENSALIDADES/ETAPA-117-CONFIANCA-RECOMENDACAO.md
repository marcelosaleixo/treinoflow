# ETAPA 117 — Confiança da Recomendação

## Objetivo
Transformar a recomendação contextual do TreinoFlow em uma indicação acompanhada por um índice operacional de confiança explicável.

## Implementação
- cálculo de confiança baseado em três sinais: tamanho da amostra, separação entre a melhor e a segunda estratégia e especificidade do contexto;
- níveis: Insuficiente, Baixa, Moderada e Alta;
- exibição percentual, barra visual, resumo e motivos;
- aviso explícito de que o índice não representa probabilidade estatística nem garantia de resultado;
- preservado o controle humano do Personal;
- nenhuma nova tabela ou migration.

## Regra
A confiança é um índice operacional de apoio à decisão. Quanto maior a amostra, maior a diferença observada entre estratégias e mais específico o contexto histórico, maior a confiança.

## Segurança
A recomendação não executa contato, não envia WhatsApp e não altera treino, carga ou prescrição.
