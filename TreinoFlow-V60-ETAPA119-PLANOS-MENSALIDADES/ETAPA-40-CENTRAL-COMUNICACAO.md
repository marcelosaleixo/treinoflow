# Etapa 40 — Central de Comunicação e Templates

## Objetivo
Permitir que o administrador Master altere os textos das comunicações do TreinoFlow sem precisar alterar código.

## O que foi implementado
- Nova entidade `TemplateNotificacao`.
- Templates por evento e canal: `INTERNA`, `EMAIL` e `WHATSAPP`.
- Eventos: cobrança vencendo em 7 dias, vencimento hoje, cobrança atrasada, pagamento confirmado, assinatura próxima do vencimento e assinatura bloqueada.
- Tela Master em `/admin/comunicacao`.
- Edição de cada template em `/admin/comunicacao/templates/editar`.
- Ativação/desativação individual do template.
- Variáveis suportadas: `{{nome}}`, `{{plano}}`, `{{valor}}`, `{{vencimento}}`, `{{link_pagamento}}`.
- Se um template externo não existir ou estiver inativo, o sistema usa o texto da notificação interna como fallback.
- Credenciais SMTP e WhatsApp continuam fora do banco, configuradas por variáveis de ambiente.

## Banco

Aplicar manualmente `src/main/resources/db/migration/V38__templates_comunicacao.sql` no PostgreSQL existente.

O projeto continua com `spring.jpa.hibernate.ddl-auto=update` no ambiente atual, mas a migration fica versionada para produção e para futuras mudanças com `validate`.

## Fluxo

1. O sistema gera uma `Notificacao` interna.
2. O `TemplateNotificacaoService` procura o template `INTERNA` do evento.
3. As variáveis são substituídas.
4. Se e-mail/WhatsApp estiverem habilitados, as entregas são criadas.
5. Na hora do envio, o sistema procura o template específico do canal.
6. Se não houver template ativo para o canal, usa o conteúdo da notificação interna.

## Observação

A tela mostra se e-mail e WhatsApp estão habilitados, mas não altera credenciais do provedor. Isso evita guardar senhas SMTP/API no banco.
