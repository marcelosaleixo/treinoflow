# Etapa 185 — Preferências de notificações em tempo real

## Objetivo
Permitir que cada Personal escolha quais categorias geram avisos instantâneos no navegador, preservando o histórico persistido.

## Implementação
- Entidade `PreferenciaNotificacao`, vinculada ao Personal e ao tipo de notificação.
- Repositório `PreferenciaNotificacaoRepository` com unicidade por Personal + tipo.
- Serviço `PreferenciaNotificacaoService`: todas as categorias ficam ativas por padrão para manter o comportamento atual; preferências salvas passam a prevalecer.
- Tela autenticada `GET /notificacoes/preferencias` e gravação via `POST /notificacoes/preferencias`, com proteção CSRF do Spring Security.
- O `NotificacaoSseService` verifica a preferência depois do commit antes de emitir o evento SSE.
- O histórico continua guardando as notificações mesmo quando o aviso instantâneo está desativado.
- Link para preferências na Central de Notificações.
- Script SQL de referência em `src/main/resources/db/migration/V185__preferencias_notificacao_tempo_real.sql`.

## Rotas
- `GET /notificacoes/preferencias`: exibe os controles.
- `POST /notificacoes/preferencias`: grava todas as categorias selecionadas.

## Segurança e isolamento
As preferências são sempre consultadas pelo ID do Personal obtido da autenticação atual. A requisição não aceita um ID de Personal enviado pelo navegador. A tabela tem chave única composta por `personal_id` e `tipo`.

## Comportamento
- Usuários sem linhas de preferência continuam recebendo todas as categorias em tempo real.
- Categorias desativadas continuam registradas na central/histórico, mas não disparam evento SSE para aquele Personal.
- As preferências não desligam e-mail, WhatsApp ou outros canais externos; o escopo desta etapa é exclusivamente o aviso instantâneo SSE.

## Validação manual
1. Entre com um Personal.
2. Acesse Central de Notificações → Preferências.
3. Desative uma categoria e salve.
4. Gere uma notificação dessa categoria; confirme que ela permanece no histórico, mas não provoca atualização instantânea via SSE.
5. Ative a categoria novamente e repita o teste; o evento deve atualizar a central.
6. Entre com outro Personal e confirme que suas preferências são independentes.
7. Teste em viewport móvel e confirme que a tela e os controles cabem na largura.

## Observação de validação
O ambiente de criação não possui Maven instalado e não conseguiu executar o Maven Wrapper sem baixar a distribuição. O ZIP foi verificado estruturalmente, mas compilação, testes automatizados e integração com PostgreSQL precisam ser confirmados no ambiente de desenvolvimento/deploy antes da produção.
