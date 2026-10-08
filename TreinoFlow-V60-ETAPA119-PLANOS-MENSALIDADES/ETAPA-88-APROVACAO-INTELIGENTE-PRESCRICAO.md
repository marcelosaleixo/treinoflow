# Etapa 88 — Aprovação Inteligente da Prescrição

## Objetivo
Transformar a sugestão da Etapa 87 em uma ação controlada pelo Personal, sem alterar automaticamente o treino.

## O que foi implementado
- Botão **Aplicar sugestão** somente quando a IA classifica a prescrição como `PROGRESSAO` com confiança `ALTA`.
- Confirmação antes da alteração.
- Endpoint POST protegido pelo fluxo normal do Spring Security/CSRF.
- Validação de ownership: exercício → treino → aluno → Personal.
- Treino liberado não pode receber alteração; é necessário revogar o acesso antes.
- A aplicação altera apenas séries, repetições e carga sugeridas.
- Após aplicar, o Personal recebe feedback e deve revisar o treino antes de liberá-lo novamente.
- Sugestões de baixa/média confiança continuam apenas como recomendação.

## Endpoint
`POST /performance/alunos/{alunoId}/evolucao/exercicios/{itemId}/aplicar-prescricao`

## Segurança de decisão
A IA nunca aplica uma sugestão automaticamente. A alteração exige ação explícita do Personal e passa pelas validações de autorização do domínio.
