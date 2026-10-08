# ETAPA 108 — Follow-up Inteligente

## Objetivo
Transformar a próxima ação registrada no CRM em uma fila operacional para o Personal, priorizando follow-ups vencidos, de hoje e dos próximos 7 dias.

## Entregas
- Nova rota `GET /performance/follow-up-inteligente`.
- Novo `FollowUpInteligenteController`.
- Novo `FollowUpInteligenteService`.
- Novo DTO `FollowUpInteligenteView`.
- Priorização por prazo: ATENÇÃO, ALTA e MÉDIA.
- Abertura do WhatsApp quando houver telefone.
- Acesso direto ao CRM e Perfil Inteligente do aluno.
- Formulário para registrar resultado do follow-up.
- Possibilidade de definir uma nova próxima ação.
- Quando uma nova data é informada, o sistema encerra o follow-up atual e cria uma nova interação de acompanhamento, preservando o ciclo operacional.
- A próxima ação nunca é criada no passado.
- Links adicionados ao painel de Performance e ao Plano de Ação Inteligente.

## Segurança
- A interação é localizada por `id + personalId`.
- O aluno permanece vinculado ao Personal autenticado.
- Nenhuma mensagem é enviada automaticamente.
- Nenhum treino, carga, exercício ou prescrição é alterado automaticamente.
- O módulo é operacional e não realiza diagnóstico clínico.

## Banco
Não foi criada nova tabela ou migration. A etapa reutiliza `interacoes_crm` e os mecanismos existentes do CRM.

## Fluxo
`Ação registrada -> próxima ação -> fila de follow-up -> contato -> resultado -> novo prazo (opcional) -> novo follow-up`
