# Etapa 192 — Fila, retentativas e histórico de e-mails de alertas

## Entregas
- Persistência das mensagens na tabela `entregas_alertas_email` via JPA/PostgreSQL.
- Processamento agendado em lotes de até 20 mensagens pendentes a cada 30 segundos (configurável).
- Retentativas automáticas com intervalo crescente e limite configurável.
- Estados `PENDENTE`, `ENVIADO` e `FALHA_FINAL`.
- Histórico administrativo em `/admin/observabilidade/emails`, protegido pelas regras existentes de `/admin/**`.
- Falhas de SMTP não apagam incidentes operacionais nem interrompem a aplicação.

## Configuração
- `TREINOFLOW_EMAIL_ENABLED=true`
- `TREINOFLOW_ALERTAS_EMAIL_DESTINO=admin@seudominio.com`
- `TREINOFLOW_SMTP_HOST`, `TREINOFLOW_SMTP_PORT`, `TREINOFLOW_SMTP_USERNAME`, `TREINOFLOW_SMTP_PASSWORD`
- `TREINOFLOW_ALERTAS_EMAIL_MAX_TENTATIVAS=4`
- `TREINOFLOW_ALERTAS_EMAIL_INTERVALO_MS=30000`

A fila só é criada quando há destinatário configurado. A entrega requer SMTP válido e `TREINOFLOW_EMAIL_ENABLED=true`. Use `spring.jpa.hibernate.ddl-auto=update` para criar a nova tabela automaticamente; em ambientes com `validate`/migrações controladas, crie a tabela por migration antes de iniciar.

## Validação manual
1. Inicie com PostgreSQL e SMTP de teste configurados.
2. Gere um incidente operacional e confira o registro em `entregas_alertas_email`.
3. Confirme que o estado passa de `PENDENTE` para `ENVIADO`.
4. Teste credenciais SMTP inválidas e confirme a contagem de tentativas e o estado `FALHA_FINAL` após atingir o limite.
5. Acesse `/admin/observabilidade/emails` com usuário autorizado.

## Limitações
- Não há exclusão automática do histórico nesta etapa.
- Em múltiplas instâncias, o agendador pode selecionar a mesma mensagem simultaneamente; para alta disponibilidade, recomenda-se implementar lock distribuído/`SKIP LOCKED`.
- A compilação e o envio real precisam ser validados no ambiente com dependências e SMTP configurados.
