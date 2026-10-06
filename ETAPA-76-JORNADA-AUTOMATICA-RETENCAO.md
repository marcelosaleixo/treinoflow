# ETAPA 76 — Jornada Automática de Retenção

## Objetivo
Transformar uma ação automática isolada em uma jornada controlada de retenção, com próximos passos, encerramento por resultado e proteção contra insistência.

## Fluxo
1. A Etapa 75 cria uma ação automática.
2. A ação recebe um identificador de jornada e fica em `AGUARDANDO_RESPOSTA`.
3. Após alguns dias, se não houver resposta ou resultado final, a jornada pode executar o segundo contato.
4. Após nova janela sem resposta, a jornada executa o último contato e encerra a sequência.
5. Recuperação, renovação, cancelamento ou resposta registrada no CRM encerram a jornada.

## Segurança operacional
- Reutiliza a configuração de automação da Etapa 75.
- Respeita horário permitido.
- Respeita score mínimo.
- Não continua quando existe resultado final.
- Ignora os próprios contatos gerados pela jornada ao procurar resposta do aluno.
- Mantém isolamento pelo `personal_id`.

## Banco
Migration `V47__jornada_automatica_retencao.sql` adiciona à tabela `acoes_assistente`:
- `jornada_id`
- `etapa_jornada`
- `proxima_acao_em`
- `jornada_status`

## Tela
`GET /assistente/jornada`

Mostra jornadas ativas, recuperadas, renovadas, canceladas e taxa de recuperação. Também possui processamento manual controlado para testes.
