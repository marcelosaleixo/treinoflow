# Etapa 191 — E-mail de alertas operacionais

## Objetivo
Enviar por e-mail os incidentes operacionais recém-registrados, aproveitando o serviço de e-mail já existente no TreinoFlow.

## Configuração no EasyPanel
Configure as variáveis:

- `TREINOFLOW_EMAIL_ENABLED=true`
- `TREINOFLOW_ALERTAS_EMAIL_DESTINO=admin@seudominio.com`
- `TREINOFLOW_SMTP_HOST=smtp.seuprovedor.com`
- `TREINOFLOW_SMTP_PORT=587`
- `TREINOFLOW_SMTP_USERNAME=usuario-smtp`
- `TREINOFLOW_SMTP_PASSWORD=senha-smtp`
- `TREINOFLOW_SMTP_AUTH=true`
- `TREINOFLOW_SMTP_STARTTLS=true`

Use o host, a porta e as credenciais SMTP fornecidos pelo seu provedor. Não coloque senhas diretamente no código ou no repositório.

## Comportamento
- Envia e-mail após persistir um novo incidente operacional.
- O destinatário é configurável por variável de ambiente; vazio significa que não há destinatário configurado.
- A integração utiliza `EmailNotificacaoService` e respeita `TREINOFLOW_EMAIL_ENABLED`.
- Falhas de envio são registradas no log e não apagam o incidente persistido.
- A deduplicação de incidentes abertos existente continua valendo, reduzindo disparos repetidos para o mesmo tipo de falha.

## Validação manual
1. Configure SMTP e habilite `TREINOFLOW_EMAIL_ENABLED`.
2. Configure `TREINOFLOW_ALERTAS_EMAIL_DESTINO` com um endereço de teste.
3. Gere um incidente operacional em ambiente de teste.
4. Confirme a chegada do e-mail e verifique os logs em caso de falha.
5. Repita com e-mail desabilitado para confirmar que o incidente continua sendo registrado.

## Limitações
O envio é síncrono no fluxo de criação do incidente. Esta etapa não implementa fila de e-mails, retentativas automáticas nem histórico persistente de entregas. A compilação e o envio real precisam ser validados no ambiente com dependências e SMTP configurados.
