# Etapa 179 — Limite individual de alertas CSV

- Cada personal pode definir seu percentual de alerta entre 0,1% e 100%.
- Quando não há valor individual salvo, usa-se `TREINOFLOW_IMPORTACAO_LIMITE_PROBLEMAS_PERCENTUAL` (padrão 10%).
- Dashboard, relatório mensal e CSV mensal usam o limite individual do personal autenticado.
- A coluna `limite_problemas_importacao_percentual` é adicionada pelo Hibernate com `ddl-auto=update`; revise migrações antes de produção.
- Tela: `/alunos/importacao/configuracoes-alertas`.
