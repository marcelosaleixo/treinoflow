# ETAPA 116 — Comparador de Estratégias

## Objetivo
Adicionar uma comparação transparente entre as estratégias de retenção disponíveis no histórico do próprio Personal.

## O que foi implementado
- Comparação lado a lado entre WhatsApp e Follow-up.
- Janela histórica de 180 dias.
- Mesma hierarquia contextual da recomendação adaptativa:
  1. mesmo perfil + faixa de risco;
  2. mesmo objetivo + faixa de risco;
  3. mesma faixa de risco;
  4. histórico geral do Personal.
- Métricas por estratégia:
  - total de ações;
  - resultados finais;
  - resultados positivos (recuperado/renovado);
  - taxa de recuperação/renovação;
  - força da evidência;
  - leitura contextual.
- Destaque da maior taxa observada, quando houver resultado final.
- Amostras pequenas continuam explicitamente sinalizadas.
- Nenhuma execução automática, envio ou alteração de treino.
- Sem nova tabela e sem migration.
