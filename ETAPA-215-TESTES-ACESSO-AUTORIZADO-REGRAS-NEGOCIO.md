# Etapa 215 — Testes de acesso autorizado e regras de negócio

## Objetivo

Complementar a Etapa 214 verificando o caminho permitido para `ROLE_MASTER`, incluindo POST com CSRF válido e a forma como os controladores reais apresentam os resultados retornados pelos serviços.

## Cobertura adicionada

- `ROLE_MASTER` abre o painel de e-mails e o painel de notificações.
- Reenvio com CSRF válido delega a decisão ao `FilaAlertaEmailService`.
- O controlador exibe mensagens distintas quando o serviço aceita ou recusa o reenvio.
- A resolução de incidente com CSRF válido delega a decisão ao serviço e exibe sucesso/erro conforme o retorno.
- Verificações `verify(...)` confirmam que os serviços reais são chamados com o identificador solicitado.

## Execução

```bash
mvn -Dtest=Etapa215AcessoAutorizadoRegrasNegocioTest test
```

Windows com Maven Wrapper:

```powershell
.\mvnw.cmd -Dtest=Etapa215AcessoAutorizadoRegrasNegocioTest test
```

## Limites

Os serviços são simulados com Mockito. Portanto, esta etapa valida o comportamento HTTP dos controladores e a delegação, mas não substitui testes de integração da persistência, do SMTP, nem prova isoladamente que o serviço de reenvio só reabre entregas em `FALHA_FINAL`. Essa regra permanece coberta por testes próprios do serviço/entidade e deve ser confirmada na execução da suíte completa.

A compilação e a execução Maven precisam ser realizadas no ambiente do projeto antes de publicar.
