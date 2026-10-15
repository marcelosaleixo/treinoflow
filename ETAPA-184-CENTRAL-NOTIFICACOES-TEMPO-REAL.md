# Etapa 184 — Central de notificações em tempo real

## Objetivo
Evoluir a central para reagir aos eventos SSE, atualizar a lista de notificações sem recarregar a página e permitir marcar uma notificação como lida ou marcar todas como lidas por requisições HTTP autenticadas.

## Implementação
- `GET /notificacoes/recentes`: retorna DTOs seguros das dez notificações mais recentes e a contagem de não lidas. Não serializa entidades JPA.
- `POST /notificacoes/{id}/ler-ajax`: marca como lida apenas se a notificação pertencer ao Personal autenticado; retorna a contagem atualizada.
- `POST /notificacoes/ler-todas-ajax`: marca as notificações do Personal autenticado como lidas.
- A central conecta-se ao canal SSE existente e atualiza a lista e o contador quando recebe o evento `notificacao`.
- Ações AJAX enviam o token CSRF. Se a requisição AJAX falhar, os formulários usam o fluxo POST tradicional como fallback.
- A lista é criada com `textContent`, sem inserir títulos/mensagens como HTML, evitando injeção de conteúdo.
- `NotificacaoService` publica o sinal SSE após o commit ao criar notificações de cobrança/pagamento. O evento transmite apenas um sinal, não o conteúdo privado.
- Quando há filtros ativos, a interface preserva os filtros e mostra um aviso em vez de substituir silenciosamente os resultados filtrados.

## Rotas
- `GET /notificacoes`
- `GET /notificacoes/eventos` (SSE)
- `GET /notificacoes/recentes` (JSON)
- `POST /notificacoes/{id}/ler-ajax` (JSON)
- `POST /notificacoes/ler-todas-ajax` (JSON)

## Testes recomendados
1. Autenticar como Personal A e manter `/notificacoes/eventos` aberto no DevTools.
2. Criar uma notificação usando um fluxo real e confirmar que o evento chega após o commit.
3. Confirmar que a lista e o contador são atualizados sem reload.
4. Marcar uma notificação como lida e verificar o contador e a remoção/atualização do item.
5. Marcar todas como lidas e confirmar contador zero.
6. Autenticar como Personal B e confirmar que ele não recebe eventos nem dados de A.
7. Testar com filtros ativos e confirmar que os resultados filtrados não são substituídos silenciosamente.
8. Testar CSRF inválido e sessão expirada; a operação não deve alterar dados sem autenticação.

## Validação
A compilação Maven não pôde ser executada neste ambiente: o Maven Wrapper não conseguiu baixar Maven 3.9.16 de `repo.maven.apache.org`. Executar `mvnw.cmd test` no Windows ou `./mvnw test` em um ambiente com acesso ao repositório Maven antes de publicar.
