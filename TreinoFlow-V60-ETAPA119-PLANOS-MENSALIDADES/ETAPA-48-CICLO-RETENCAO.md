# ETAPA 48 — Ciclo de Retenção

## Objetivo
Transformar o Plano de Ação da Etapa 47 em uma agenda operacional de follow-ups, usando o CRM existente como fonte oficial do histórico e do resultado de cada contato.

## Funcionalidades
- Nova rota `/retencao`.
- Fila de follow-ups vencidos.
- Fila de follow-ups previstos para hoje.
- Fila de follow-ups dos próximos 7 dias.
- Contadores de vencidos, hoje e próximos 7 dias.
- Indicadores dos últimos 30 dias para recuperados, renovados e cancelamentos.
- Atalho para abrir o CRM do aluno.
- Atalho para WhatsApp quando o telefone estiver cadastrado.
- Histórico resumido de recuperações, renovações e cancelamentos.
- Links para Retenção adicionados ao CRM e às telas de Performance/Plano de Ação.
- Layout responsivo para desktop e celular.

## Regras
1. Só entram na fila os registros com `resultado = EM_ACOMPANHAMENTO` e `dataProximaAcao` preenchida.
2. Data anterior a hoje = `VENCIDO`.
3. Data igual a hoje = `HOJE`.
4. Data posterior a hoje e até 7 dias = `PRÓXIMOS 7 DIAS`.
5. Resultados `RECUPERADO`, `RENOVADO` e `CANCELAMENTO` são apresentados separadamente quando registrados nos últimos 30 dias.
6. A fila é sempre filtrada pelo `personalId` do usuário autenticado.

## Persistência
Não cria tabelas nem migrations. A etapa reutiliza `interacoes_crm`, especialmente `dataProximaAcao` e `resultado`.

## Fluxo operacional
1. O Personal identifica um aluno no Plano de Ação.
2. Abre o CRM e registra o contato.
3. Define uma `Próxima ação`.
4. O follow-up aparece automaticamente em `/retencao`.
5. Ao realizar o novo contato, o Personal registra outra interação no CRM e informa o resultado (`RECUPERADO`, `RENOVADO`, `CANCELAMENTO` ou `EM_ACOMPANHAMENTO`).
6. O ciclo passa a aparecer no histórico de resultados.

## Segurança
Nenhuma rota recebe `personalId` do navegador. O Personal é resolvido pela autenticação e todas as consultas usam o ID do usuário autenticado.
