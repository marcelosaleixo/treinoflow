# Etapa 210 — Checklist final de implantação dos alertas operacionais

## Objetivo

Consolidar os critérios de liberação das etapas 188–209. Este documento não substitui a execução dos testes nem afirma que o ambiente de produção foi validado.

## 1. Antes de implantar

- [ ] Gerar backup consistente do PostgreSQL e testar o procedimento de restauração.
- [ ] Confirmar Java 21 no build e no runtime do EasyPanel.
- [ ] Conferir as migrations/estratégia de schema. Se `DDL_AUTO=validate`, aplicar previamente todas as migrations necessárias; não confiar em `update` como estratégia permanente de evolução de schema em produção.
- [ ] Confirmar que o usuário da aplicação tem apenas as permissões necessárias no banco.
- [ ] Configurar `TREINOFLOW_MASTER_EMAIL` e uma senha forte fora do repositório.
- [ ] Não publicar `.env`, credenciais SMTP, senha do banco nem dados pessoais em logs ou artefatos.
- [ ] Confirmar que `/admin/**` exige `ROLE_MASTER` e que formulários POST continuam protegidos por CSRF.

## 2. Configuração dos alertas

Defina as variáveis no painel de ambiente do EasyPanel; `.env.example` serve apenas como referência:

| Variável | Valor recomendado para início |
|---|---|
| `TREINOFLOW_EMAIL_ENABLED` | `false` até validar o SMTP em homologação |
| `TREINOFLOW_ALERTAS_EMAIL_DESTINO` | caixa monitorada da equipe |
| `TREINOFLOW_SMTP_HOST` | host SMTP fornecido pelo provedor |
| `TREINOFLOW_SMTP_PORT` | `587` quando o provedor usar STARTTLS |
| `TREINOFLOW_SMTP_USERNAME` | usuário SMTP dedicado |
| `TREINOFLOW_SMTP_PASSWORD` | segredo armazenado no painel, nunca no Git |
| `TREINOFLOW_SMTP_AUTH` | `true` conforme o provedor |
| `TREINOFLOW_SMTP_STARTTLS` | `true` quando suportado pelo provedor |
| `TREINOFLOW_ALERTAS_EMAIL_MAX_TENTATIVAS` | `4` |
| `TREINOFLOW_ALERTAS_EMAIL_INTERVALO_MS` | `30000` |

Habilite o envio (`TREINOFLOW_EMAIL_ENABLED=true`) somente após enviar e receber um e-mail de teste. Os nomes das variáveis devem coincidir com `application.properties`.

## 3. Portão de qualidade (Go/No-Go)

Execute na raiz do projeto com Docker disponível para os testes Testcontainers:

```bash
./mvnw -DskipTests package
./mvnw test
./mvnw -Dtest=FilaAlertaEmailIdempotenciaTest test
./mvnw -Dtest=EntregaAlertaEmailConcorrenciaIT test
./mvnw -Dtest=Etapa208RecuperacaoFilaPostgresIT test
```

No Windows, use `mvnw.cmd` no lugar de `./mvnw`. Confirme os nomes exatos dos testes existentes antes de executar comandos seletivos. Se o projeto usa perfis Maven para testes de integração, utilize o perfil definido no `pom.xml`.

**NO-GO:** qualquer falha de compilação, migration pendente, teste de concorrência falhando, credencial inválida ou envio SMTP sem confirmação. Não marque implantação como aprovada até investigar e corrigir a causa.

## 4. Verificação pós-implantação

- [ ] Aplicação inicia sem erros de schema e permanece saudável após reiniciar o container.
- [ ] Usuário sem `ROLE_MASTER` recebe acesso negado ao painel `/admin/observabilidade/emails`.
- [ ] Usuário autorizado consulta histórico, pagina, filtra e reenvia somente itens em `FALHA_FINAL`.
- [ ] Incidente operacional gera no máximo uma entrega inicial por `incidente_id`.
- [ ] A fila transita por `PENDENTE` → `PROCESSANDO` → `ENVIADO` ou fluxo de retentativa/falha final esperado.
- [ ] Uma reserva abandonada é recuperada depois de expirar; confirme o comportamento no banco e no painel.
- [ ] O e-mail de teste chega ao destinatário; assunto e conteúdo não incluem segredos.
- [ ] Logs permitem localizar a entrega pelo identificador sem imprimir senha SMTP ou conteúdo sensível.
- [ ] O painel `/admin/observabilidade/emails` mostra indicadores coerentes com as linhas do banco.

## 5. Operação e rollback

1. Durante a primeira implantação, monitore o painel e os logs após cada deploy.
2. Se o SMTP falhar, defina `TREINOFLOW_EMAIL_ENABLED=false` para interromper tentativas de envio enquanto a causa é investigada; a aplicação pode continuar registrando entregas pendentes/falhas conforme o fluxo implementado.
3. Não apague manualmente registros `PROCESSANDO` sem confirmar se existe worker ativo e se a reserva expirou.
4. Para rollback de aplicação, use a imagem anterior e preserve o banco. Não reverta schema destrutivamente sem um plano testado de compatibilidade/restauração.
5. Documente horário, versão, incidente e ação corretiva.

## Limitações conhecidas

- Os contadores de SSE mantidos em memória reiniciam quando o processo é reiniciado; não são métricas históricas distribuídas.
- Testcontainers precisa de Docker funcional. Testes de integração não são substituídos por testes unitários ou H2.
- Este checklist é um roteiro operacional, não um certificado de segurança nem evidência de que os comandos foram executados no ambiente de produção.
