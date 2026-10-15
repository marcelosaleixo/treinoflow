# Etapa 194 — Paginação, filtros por período e concorrência da fila de e-mails

## Entregas
- Histórico de e-mails paginado no servidor (20 registros por página; tamanho limitado no serviço).
- Filtros por status, data inicial e data final, mantendo os filtros ao navegar entre páginas.
- Consulta com limites de data inclusivos para o dia final e exclusivos para o dia seguinte.
- Bloqueio pessimista da linha durante o processamento de cada entrega, para evitar que duas instâncias processem simultaneamente o mesmo registro.
- Índices existentes para status/próxima tentativa e data de criação permanecem em uso.

## Como validar
1. Configure PostgreSQL e SMTP e inicie a aplicação.
2. Acesse `/admin/observabilidade/emails` com usuário MASTER.
3. Teste status e intervalo de datas, navegue para próxima/anterior e confirme a preservação dos filtros.
4. Em ambiente de teste, execute duas instâncias contra o mesmo banco e confirme que a mesma linha não é processada simultaneamente.

## Observações técnicas
- O bloqueio utiliza `PESSIMISTIC_WRITE` do JPA e depende de um banco que suporte bloqueio de linha, como PostgreSQL.
- A transação permanece aberta durante a chamada SMTP. Isso evita a concorrência da mesma entrega, mas pode manter o bloqueio durante a latência do provedor. Uma futura evolução pode usar reivindicação atômica com estado `PROCESSANDO` e lease/timeout.
- Não foi possível confirmar compilação ou execução nesta etapa; validar em ambiente Maven/PostgreSQL antes de produção.
