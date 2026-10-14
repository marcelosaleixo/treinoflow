# Etapa 182 - Contador de notificacoes em tempo real

- Adicionado endpoint autenticado GET `/notificacoes/contagem-nao-lidas`, que retorna `{ "total": N }` para o personal da sessao.
- O cabecalho consulta o endpoint a cada 30 segundos, quando a aba esta visivel, e tambem ao voltar para a aba.
- O contador e atualizado sem recarregar a pagina; o badge some quando o total e zero e limita a exibicao a `99+`.
- Requisicoes usam cookies da sessao (`same-origin`), `cache: no-store` e nao incluem identificadores fornecidos pelo cliente.
- Falhas temporarias da consulta nao interrompem a navegacao.

Validar em ambiente local: entrar com um personal, abrir duas abas, criar/marcar notificacoes como lidas e aguardar ate 30 segundos ou alternar de volta para a aba.
