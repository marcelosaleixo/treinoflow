# Etapa 190 — Persistência dos incidentes operacionais

## Objetivo
Manter o histórico de alertas operacionais mesmo após reinicializações do TreinoFlow.

## Alterações
- Nova entidade `IncidenteOperacionalNotificacao`.
- Novo repositório `IncidenteOperacionalNotificacaoRepository`.
- `AlertaOperacionalNotificacaoService` passa a persistir e consultar incidentes no PostgreSQL.
- O painel `/admin/observabilidade/notificacoes` informa que os incidentes são persistentes, enquanto as métricas SSE permanecem em memória.
- A resolução manual e a resolução automática após recuperação da retenção atualizam os registros existentes.

## Banco de dados
A tabela `incidentes_operacionais_notificacao` é gerenciada pelo Hibernate conforme a configuração atual do projeto (`spring.jpa.hibernate.ddl-auto=update`). Se o ambiente utilizar `validate` ou migrations controladas, crie a tabela e índices antes de publicar a versão.

Campos: `id` (UUID textual), `codigo`, `severidade`, `titulo`, `detalhe`, `ocorrido_em`, `resolvido` e `resolvido_em`.

## Testes recomendados
1. Iniciar com um banco de desenvolvimento e confirmar a criação da tabela.
2. Simular o limite de falhas SSE configurado e verificar a criação do incidente.
3. Reiniciar a aplicação e confirmar que o incidente continua visível.
4. Resolver o incidente pelo painel e confirmar `resolvido=true` e `resolvido_em` no banco.
5. Executar a rotina de retenção com sucesso e confirmar a resolução dos incidentes `RETENCAO_AUDITORIA` abertos.

## Limites
As ocorrências e a resolução são persistidas, mas a janela móvel usada para contar falhas SSE ainda fica em memória. O envio por e-mail/WhatsApp e alertas entre múltiplas instâncias não fazem parte desta etapa.
