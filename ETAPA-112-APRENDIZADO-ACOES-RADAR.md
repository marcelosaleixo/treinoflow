# ETAPA 112 — Aprendizado das Ações de Retenção

## Objetivo
Fazer o Radar Diário alimentar o histórico estruturado de ações para que o TreinoFlow consiga aprender quais tipos de ação apresentam melhor resultado em cada faixa de risco.

## Implementação
- Toda ação executada pelo Radar continua sendo registrada no CRM como fonte operacional principal.
- A mesma ação é registrada no histórico `AcaoAssistente` com:
  - score de risco capturado antes da ação;
  - tipo de ação derivado do canal (WhatsApp ou Follow-up);
  - resultado informado pelo Personal;
  - origem `RADAR_DIARIO`;
  - indicação de que a ação foi humana, não automática.
- O painel de aprendizado existente passa a contabilizar essas ações no histórico de efetividade.
- O painel mostra quantas ações analisadas vieram do Radar.

## Regras
- Não cria tabela nem migration.
- Não duplica o registro operacional do CRM; a entrada em `AcaoAssistente` é uma trilha estruturada para aprendizado.
- O score usado para aprendizado é o snapshot anterior à ação, evitando contaminar o histórico com o resultado posterior.
- O resultado `EM_ACOMPANHAMENTO` permanece como não-final até uma conclusão posterior.
- Nenhuma mensagem é enviada automaticamente por esta etapa.
- Nenhum treino, carga ou prescrição é alterado automaticamente.

## Fluxo
`Radar → execução humana → CRM → histórico estruturado → aprendizado → recomendação futura`

## Benefício
O TreinoFlow começa a aprender com o próprio comportamento operacional do Personal, reduzindo a distância entre recomendação genérica e estratégia baseada no histórico real da carteira.
