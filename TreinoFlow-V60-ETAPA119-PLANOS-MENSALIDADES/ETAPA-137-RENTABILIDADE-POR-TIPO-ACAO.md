# Etapa 137 — Rentabilidade por tipo de ação

## Objetivo
Comparar resultados registrados no CRM por tipo de ação, com filtros mensais e isolamento por Personal.

## Implementação
- Rota GET `/financeiro/rentabilidade-acoes`.
- Agregação de ações por tipo: total, recuperações, renovações, cancelamentos, ações em acompanhamento, sem resposta e taxa positiva entre registros encerrados.
- Histórico recente com aluno, tipo, assunto e resultado.
- Atalho no dashboard financeiro e na tela de resultados de ações.
- Reutiliza `InteracaoCrmRepository` e não requer nova tabela ou migração.

## Limitação importante
Os registros CRM não vinculam uma ação a um pagamento específico. Portanto, a tela não estima lucro ou receita atribuída por ação; apresenta resultados descritivos dos registros.

## Validação
Verificar compilação Maven, acesso autenticado, filtro por período e responsividade no ambiente de desenvolvimento antes de publicar.
