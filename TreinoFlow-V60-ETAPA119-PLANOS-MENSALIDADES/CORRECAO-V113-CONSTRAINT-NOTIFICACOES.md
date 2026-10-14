# Correção V113 — Constraint de tipos de notificação

O banco de dados rejeitava `META_RECEITA_NAO_DEFINIDA` porque a constraint `notificacoes_treinoflow_tipo_check` não contemplava os novos tipos de alertas de meta de receita.

Foi adicionada uma migration Flyway que remove a constraint antiga e a recria com todos os valores atualmente declarados em `TipoNotificacao`.

A migration preserva os registros existentes e não desativa a validação de tipos; apenas sincroniza os valores permitidos com o enum da aplicação.
