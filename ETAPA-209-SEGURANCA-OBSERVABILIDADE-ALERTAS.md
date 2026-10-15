# Etapa 209 — Revisão de segurança e observabilidade dos alertas

## Alterações

- O painel `/admin/observabilidade/emails` continua coberto pela regra `/admin/**` do Spring Security, que exige `ROLE_MASTER`.
- A ação de reenvio permanece como `POST`, respeitando a proteção CSRF do Spring Security.
- O parâmetro de paginação agora é normalizado para zero quando chega negativo, evitando falhas por URL inválida.
- O filtro de status aceita somente os estados previstos; valores desconhecidos voltam para `TODOS`.
- As datas inicial e final são normalizadas quando informadas em ordem inversa.
- O detalhe de erro dos incidentes é limitado e as quebras de linha são removidas antes de entrar nos logs.

## Checklist de implantação

1. Configure SMTP por variáveis de ambiente; nunca versionar credenciais.
2. Confirme que `ROLE_MASTER` é atribuído apenas a administradores autorizados.
3. Confirme que CSRF permanece habilitado para operações administrativas POST.
4. Não registre senha SMTP, tokens, corpo de mensagens ou dados pessoais desnecessários nos logs.
5. Monitore a fila por status, tentativas, último erro e idade da entrega pendente.
6. Execute a compilação e a suíte de testes antes de publicar.

## Validação

A alteração de paginação é uma correção defensiva localizada. O ZIP precisa ser compilado e testado no ambiente do projeto. Esta etapa não declara que os testes de produção, SMTP real ou concorrência entre instâncias foram executados.
