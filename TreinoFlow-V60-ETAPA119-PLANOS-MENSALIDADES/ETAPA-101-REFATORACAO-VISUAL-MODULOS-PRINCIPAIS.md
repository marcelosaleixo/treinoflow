# ETAPA 101 — Refatoração Visual dos Módulos Principais

## Objetivo
Aplicar o Design System criado na Etapa 100 às telas principais sem alterar regras de negócio, banco ou autenticação.

## Telas padronizadas
- Dashboard
- Lista de alunos
- Perfil inteligente do aluno
- Performance
- CRM
- Retenção

## Implementação
- Novo `static/css/tf-pages.css` com escopo por página usando classes `tf-page-*`.
- Cabeçalho continua sendo o fragmento único da Etapa 99.
- Cards, estatísticas, tabelas, formulários, botões, badges, alertas e grids passam a seguir as variáveis `--tf-*` do Design System.
- Responsividade revisada para 950px e 650px.
- CSS legado não foi removido; os overrides são escopados para reduzir risco de regressão.

## Segurança
Nenhuma alteração em autenticação, autorização, persistência ou banco de dados.

## Próximo passo
Revisar visualmente as telas de treino, agenda, portal do aluno e módulos secundários usando o mesmo padrão antes de remover CSS legado.
