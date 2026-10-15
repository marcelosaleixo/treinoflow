# Etapa 195 — Processamento seguro da fila de e-mails

## Entregas
- Novo status `PROCESSANDO` e campo `reservado_ate` para lease temporária.
- Reivindicação atômica com UPDATE condicional: somente uma instância consegue alterar `PENDENTE` para `PROCESSANDO`.
- SMTP executado fora da transação de reivindicação, evitando manter lock do banco durante a chamada externa.
- Finalização transacional em `ENVIADO`, `PENDENTE` ou `FALHA_FINAL`.
- Recuperação agendada de reservas expiradas, devolvendo mensagens à fila ou encerrando após o limite de tentativas.

## Configuração
- `TREINOFLOW_ALERTAS_EMAIL_INTERVALO_MS` / propriedade `treinoflow.notificacoes.alertas.email.intervalo-ms`: intervalo de processamento (padrão 30000 ms).
- `treinoflow.notificacoes.alertas.email.recuperacao-ms`: intervalo de recuperação de reservas (padrão 60000 ms).
- A reserva de processamento dura 5 minutos; deve ser superior ao timeout SMTP configurado.

## Banco
Hibernate `ddl-auto=update` adiciona a coluna `reservado_ate` quando permitido pela configuração do ambiente. Em ambientes com `validate` ou migrações controladas, criar a coluna manualmente antes de subir a versão.

## Limitação operacional
A reivindicação atômica evita que duas instâncias processem simultaneamente a mesma linha. Porém, se o processo cair depois que o servidor SMTP aceitou a mensagem e antes de gravar `ENVIADO`, a recuperação poderá reenviar o e-mail. SMTP não oferece garantia de exactly-once; usar uma chave idempotente no provedor ou reconciliar entregas é uma etapa futura.
