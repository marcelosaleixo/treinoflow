# Etapa 89 — Histórico e Auditoria das Decisões da IA

## Objetivo
Registrar de forma rastreável toda prescrição assistida que foi efetivamente aplicada pelo Personal.

## Implementação
- Nova entidade `PrescricaoAuditoria` e tabela `prescricoes_auditoria`.
- Snapshot da prescrição antes e depois da aplicação.
- Registro do nível e confiança da IA, justificativa, Personal, aluno, treino e exercício.
- Data/hora e decisão `APLICADA`.
- Histórico protegido pelo vínculo aluno → Personal.
- Painel de evolução mostra até 30 decisões recentes do aluno.

## Fluxo
IA sugere → Personal aprova → treino é alterado → auditoria registra ANTES x DEPOIS.

## Segurança
A auditoria só é gravada após a alteração ser validada e salva. Não existe aplicação automática pela IA.
