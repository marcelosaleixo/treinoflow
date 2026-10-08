# ETAPA 99 — Padronização visual global

## Objetivo
Eliminar a inconsistência de cabeçalhos entre telas do TreinoFlow e evitar colisões com classes genéricas como `.bar`, `.top` e `.topbar`.

## Implementação
- Criado fragmento reutilizável `fragments/tf-header.html`.
- Criado `static/css/tf-header.css` com classes exclusivas `tf-*`.
- Padronizado o cabeçalho das telas do Personal.
- Mantidos separados os layouts de administração, portal do aluno e impressão de treino.
- Navegação principal padronizada: Dashboard, Alunos, Agenda, Performance, CRM, Retenção, Assistente, Exercícios e Sair.

## Segurança
Nenhuma regra de autenticação ou autorização foi alterada. O logout continua usando POST com CSRF do Spring Security via formulário Thymeleaf.

## Banco
Nenhuma alteração de banco de dados.
