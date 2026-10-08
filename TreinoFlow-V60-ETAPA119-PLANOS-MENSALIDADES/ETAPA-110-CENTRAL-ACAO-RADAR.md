# ETAPA 110 — Central de Ação do Radar

## Objetivo
Transformar cada prioridade do Radar Diário em uma ação humana executável e registrável no CRM, fechando o ciclo entre sinal, decisão, contato, resultado e próximo passo.

## Implementação
- `RadarAcaoService`: valida propriedade do aluno, prepara contexto e registra a ação no CRM.
- `RadarAcaoController`: GET `/performance/radar-diario/acao/{alunoId}` e POST da mesma rota.
- `RadarAcaoView`: contexto seguro da ação.
- `performance/radar-acao.html`: tela responsiva para executar e registrar a ação.
- `performance/radar-diario.html`: botão `Executar ação` na prioridade #1 e em cada item da fila.

## Fluxo
Radar -> Executar ação -> WhatsApp/CRM/Perfil -> contato humano -> resultado -> próxima ação -> CRM -> Follow-up.

## Regras
- A carteira é validada pelo `personalId` autenticado.
- O sistema não envia WhatsApp automaticamente.
- O sistema não altera exercícios, cargas, treinos ou prescrição.
- `EM_ACOMPANHAMENTO` exige próxima ação.
- Próxima ação não pode ficar no passado.
- O registro usa `InteracaoCrm`/`CrmService`; não cria tabela nova.
- Dor e demais sinais são tratados como alertas operacionais, não como diagnóstico.
