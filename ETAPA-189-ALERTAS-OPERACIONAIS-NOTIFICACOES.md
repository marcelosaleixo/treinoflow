# Etapa 189 — Alertas operacionais automáticos

## Entregas
- Detecta falhas repetidas no envio SSE: padrão de 5 falhas em 10 minutos.
- Registra incidente de retenção quando a limpeza agendada falha e encerra o alerta aberto quando uma execução posterior é bem-sucedida.
- Adiciona alertas ao painel administrativo `/admin/observabilidade/notificacoes`, restrito às permissões administrativas já existentes.
- Permite marcar um incidente como resolvido, com proteção CSRF do Spring Security.
- Limita o histórico a 200 incidentes em memória e mostra os 50 mais recentes.
- Não armazena conteúdo de notificações nem IDs de usuários nos incidentes.

## Configuração opcional
```properties
TREINOFLOW_ALERTAS_SSE_LIMITE_FALHAS=5
TREINOFLOW_ALERTAS_SSE_JANELA_MINUTOS=10
```
O limite é no mínimo 2 e a janela no mínimo 1 minuto. Os valores padrão são 5 falhas em 10 minutos.

## Limitações importantes
Os incidentes são mantidos em memória: reiniciar a aplicação limpa o histórico. Nesta etapa os alertas são exibidos no painel, não enviados por e-mail, SMS ou WhatsApp. A ausência de conexões SSE não gera alerta automaticamente, pois o sistema não possui nesta camada um indicador confiável de quantos usuários deveriam estar conectados.

## Validação
Verifique no ambiente de desenvolvimento: `/admin/observabilidade/notificacoes`; provoque falhas de envio SSE em sequência para conferir o alerta; habilite a retenção e valide que uma falha aparece no painel; depois confirme que uma execução bem-sucedida resolve o alerta de retenção. O painel continua protegido pela regra administrativa existente.
