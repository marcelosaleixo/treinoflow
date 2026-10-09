# Etapa 145 — Notificações persistentes de metas de receita

- Reutiliza a tabela existente `notificacoes_treinoflow` e o enum `TipoNotificacao`; não cria uma tabela nova.
- Ao abrir `/financeiro/alertas-metas-receita`, sincroniza a condição do mês consultado e cria notificação persistente para meta atingida, abaixo do ritmo, meta não definida (mês atual) ou mês anterior encerrado sem atingir a meta.
- A chave única `META_RECEITA:{personalId}:{yyyy-MM}:{tipo}` impede duplicações ao recarregar a tela.
- As notificações ficam disponíveis em `/notificacoes`, com os controles existentes de leitura.
- Não envia e-mail ou WhatsApp e não usa tarefa agendada; a geração ocorre quando a tela de alertas é acessada.
- A mesma condição/tipo é registrada no máximo uma vez por personal e mês. O aviso de ritmo pode ficar desatualizado após ser criado; reavaliar em uma etapa futura se for necessário histórico de eventos por mudança de estado.

## Validação

ZIP integrity checked after packaging. Compilation and PostgreSQL runtime have not been verified in this environment.
