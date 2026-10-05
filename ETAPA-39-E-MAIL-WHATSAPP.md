# Etapa 39 — E-mail transacional + WhatsApp

## Objetivo
Adicionar canais externos à central de notificações financeiras sem acoplar o domínio a um provedor específico.

## Arquitetura
- `Notificacao` continua representando o evento exibido dentro do sistema.
- `NotificacaoEntrega` representa cada tentativa de entrega externa.
- Canais: `EMAIL` e `WHATSAPP`.
- Estados: `PENDENTE`, `ENVIADO`, `ERRO`.
- Até 5 tentativas por entrega, com backoff simples.
- Canais ficam desativados por padrão.

## E-mail
Usa `spring-boot-starter-mail` e SMTP configurável por ambiente.

Variáveis:
- `TREINOFLOW_EMAIL_ENABLED=true`
- `TREINOFLOW_SMTP_HOST`
- `TREINOFLOW_SMTP_PORT=587`
- `TREINOFLOW_SMTP_USERNAME`
- `TREINOFLOW_SMTP_PASSWORD`
- `TREINOFLOW_SMTP_AUTH=true`
- `TREINOFLOW_SMTP_STARTTLS=true`

## WhatsApp
O adaptador usa HTTP e é compatível com o padrão de endpoint de envio de texto da Evolution API, mas não obriga o sistema a utilizar esse provedor.

Variáveis:
- `TREINOFLOW_WHATSAPP_ENABLED=true`
- `TREINOFLOW_WHATSAPP_BASE_URL`
- `TREINOFLOW_WHATSAPP_API_KEY`
- `TREINOFLOW_WHATSAPP_INSTANCE`
- `TREINOFLOW_WHATSAPP_ENDPOINT=/message/sendText/{instance}`

O telefone é normalizado para dígitos; números brasileiros com 11 dígitos recebem o prefixo `55`.

## Fluxo
1. `NotificacaoService` cria a notificação interna.
2. Se o canal estiver habilitado e houver destino, cria uma `NotificacaoEntrega`.
3. O agendador processa entregas a cada 5 minutos.
4. Sucesso: `ENVIADO`.
5. Falha: `ERRO`, registra erro e agenda nova tentativa.
6. Após 5 tentativas, a entrega permanece em `ERRO` e não é reenviada automaticamente.

## Segurança
Credenciais não ficam no código. Use variáveis de ambiente/EasyPanel. O envio externo não bloqueia a criação da notificação interna: falhas são registradas na entrega e tratadas pelo retry.
