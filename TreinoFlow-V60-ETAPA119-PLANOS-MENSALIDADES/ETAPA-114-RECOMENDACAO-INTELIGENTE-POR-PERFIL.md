# ETAPA 114 — Recomendação Inteligente por Perfil + Histórico

## Objetivo
Usar o histórico real de ações do próprio Personal para sugerir o tipo de ação com melhor evidência para o aluno atual, combinando faixa de risco, objetivo e faixa etária quando houver dados suficientes.

## Implementação
- Novo `RecomendacaoAcaoPerfilService`.
- Novo `RecomendacaoAcaoPerfilView`.
- O Radar calcula o risco atual e consulta a recomendação antes da execução da ação.
- A recomendação aparece na tela de execução da ação do Radar.
- Hierarquia de evidência:
  1. mesmo objetivo + mesma faixa etária + mesma faixa de risco;
  2. mesmo objetivo + mesma faixa de risco;
  3. mesma faixa de risco;
  4. histórico geral do Personal.
- São considerados os últimos 180 dias.
- A efetividade é calculada por recuperação/renovação entre resultados finais.
- Mínimo de 3 resultados finais para considerar a indicação como evidência suficiente.
- Com amostra pequena, o sistema mostra a indicação como sinal inicial e não como regra.

## Segurança e limites
- Não envia WhatsApp automaticamente.
- Não executa a ação automaticamente.
- Não altera treino, carga ou prescrição.
- Não cria nova tabela nem migration.
- O isolamento por `personalId` é mantido.
- A recomendação é apoio à decisão do Personal.

## Fluxo
`Radar → risco atual → perfil do aluno → histórico do Personal → recomendação → decisão humana → CRM → resultado`

## Benefício
O Radar deixa de oferecer apenas uma ação genérica e passa a mostrar qual tipo de abordagem possui melhor evidência dentro do contexto do aluno e do histórico daquela carteira.
