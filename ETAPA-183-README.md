# TreinoFlow — Etapa 183: notificações em tempo real (SSE)

## O que foi implementado
- Endpoint autenticado `GET /notificacoes/eventos` com `text/event-stream`.
- Conexões SSE isoladas por ID do Personal autenticado; o stream não transmite conteúdo privado, apenas sinaliza atualização.
- Eventos publicados após o commit da transação quando notificações financeiras ou alertas persistentes de meta são criados/atualizados.
- Heartbeat a cada 25 segundos para manter conexões abertas atrás de proxies.
- Cabeçalho atualizado para usar `EventSource`; o contador consulta o endpoint autenticado apenas na conexão/evento/reconexão, sem polling periódico de 30 segundos.
- O navegador usa a reconexão automática nativa do EventSource e fecha a conexão ao sair da página.
- Removido o valor de senha padrão do datasource: configure `SPRING_DATASOURCE_PASSWORD` (ou a variável compatível já usada no ambiente) no EasyPanel.

## Arquivos principais
- `src/main/java/com/marceloaleixo/treinoflow/service/NotificacaoSseService.java`
- `src/main/java/com/marceloaleixo/treinoflow/controller/NotificacaoController.java`
- `src/main/java/com/marceloaleixo/treinoflow/service/NotificacaoService.java`
- `src/main/java/com/marceloaleixo/treinoflow/service/AlertaMetaReceitaPersistenteService.java`
- `src/main/resources/templates/fragments/tf-header.html`

## Validação
A suíte Maven não pôde ser executada neste ambiente porque o Maven Wrapper não conseguiu baixar Maven de `repo.maven.apache.org`. Execute localmente `./mvnw test` (Linux/macOS) ou `mvnw.cmd test` (Windows) e valide o deploy no EasyPanel.

## Teste manual recomendado
1. Entre com um Personal em duas abas/navegadores.
2. Abra DevTools > Network e confirme a requisição `/notificacoes/eventos` como `text/event-stream`.
3. Gere uma notificação por um fluxo já existente; o contador deve mudar sem esperar um intervalo fixo.
4. Confirme que outro Personal não recebe eventos nem contagem do primeiro.
5. Reinicie a aplicação e confira o comportamento de reconexão automática.
