# Etapa 188 — Painel de Saúde das Notificações

## Objetivo
Expor um painel administrativo com indicadores básicos do canal SSE e da rotina de retenção do histórico de auditoria.

## Implementação
- Rota `GET /admin/observabilidade/notificacoes`, protegida pela regra existente `/admin/**` (`ROLE_MASTER`).
- Métricas em memória sem conteúdo de notificações nem identificadores pessoais: conexões SSE ativas, eventos enviados e falhas de envio.
- Registro da última execução da limpeza e quantidade removida, quando a rotina estiver habilitada.
- Interface responsiva e indicação explícita de que as métricas SSE reiniciam com a aplicação.

## Limitações conhecidas
- Métricas são locais à instância e voláteis; em múltiplas réplicas cada instância terá seu próprio contador.
- A limpeza desabilitada por padrão não registra execução, pois a rotina não é instanciada.
- Não existe endpoint público de métricas; o painel é acessível somente pela rota administrativa protegida.

## Validação
1. Compilar e executar testes com Maven.
2. Acessar como MASTER `/admin/observabilidade/notificacoes`.
3. Confirmar que usuário PERSONAL recebe acesso negado.
4. Abrir/fechar conexão SSE e verificar contagem ativa.
5. Gerar notificação e conferir incremento de eventos enviados.
6. Habilitar limpeza apenas em homologação e verificar data/quantidade da execução.
