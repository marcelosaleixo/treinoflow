# Etapa 188 — Painel de Saúde das Notificações

**Versão:** V25  
**Stack:** Java 21, Spring Boot, Thymeleaf, PostgreSQL

## O que mudou
- Novo painel administrativo em `/admin/observabilidade/notificacoes`.
- Métricas operacionais do SSE: conexões ativas, eventos enviados e falhas de envio.
- Registro da última execução e quantidade removida pela rotina de retenção da auditoria de preferências.
- Página responsiva, sem expor mensagens, identificadores de Personal ou conteúdo privado.
- A rota fica sob `/admin/**`, já restrita a `ROLE_MASTER` na configuração de segurança existente.

## Observações
As métricas de SSE são mantidas em memória e reiniciam com a aplicação; em ambiente com múltiplas réplicas, cada instância apresenta seus próprios números. A retenção continua desativada por padrão.

## Validar
1. Execute `mvnw.cmd test` no Windows ou `./mvnw test` no Linux.
2. Entre com usuário MASTER e acesse `/admin/observabilidade/notificacoes`.
3. Confirme que um usuário PERSONAL não acessa a rota.
4. Abra a central em outra aba e confira conexões ativas; gere uma notificação e observe o contador de eventos.
5. Só habilite a limpeza de auditoria em homologação antes de validar a última execução.
