# Etapa 146 — Central de notificações com filtros e histórico

## Entregas
- Filtro por estado (todas, lidas e não lidas).
- Filtro por mês de criação.
- Filtro por tipo de notificação.
- Contadores de histórico total, não lidas e itens após filtro.
- Layout responsivo e integração com os endpoints existentes de leitura.

## Segurança e comportamento
- A consulta continua limitada ao personal autenticado por `NotificacaoService.listar(personalId)`.
- Os filtros são aplicados sobre a lista daquele personal; não alteram nem excluem notificações.
- A marcação como lida continua validando o proprietário no serviço existente.
- Não foi adicionada migração de banco de dados.

## Validação pendente
Executar `./mvnw clean compile` ou `mvn clean compile` e testar a página com dados reais em PostgreSQL. A compilação/runtime não são declarados como confirmados neste pacote.
