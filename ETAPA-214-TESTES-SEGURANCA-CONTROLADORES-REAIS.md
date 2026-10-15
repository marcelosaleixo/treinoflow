# Etapa 214 — Testes de segurança dos controladores reais

## Objetivo

Validar as regras de autenticação, autorização e CSRF diretamente nas rotas dos controladores administrativos de observabilidade, sem substituir as rotas por controladores fictícios.

## Testes adicionados

Classe `Etapa214SegurancaControladoresReaisTest`:

- visitante é redirecionado ao painel administrativo de e-mails;
- usuário `ROLE_PERSONAL` recebe 403 ao acessar o painel administrativo de notificações;
- usuário `ROLE_PERSONAL` não pode acionar a rota administrativa de reenvio;
- usuário `ROLE_MASTER` sem token CSRF recebe 403 ao tentar resolver um incidente.

Os serviços são simulados para manter os testes focados no filtro de segurança e nas rotas reais. Isso não comprova regras de negócio, persistência nem o resultado de uma ação autorizada.

## Execução

```bash
mvn -Dtest=Etapa214SegurancaControladoresReaisTest test
```

No Windows com Maven Wrapper:

```powershell
.\mvnw.cmd -Dtest=Etapa214SegurancaControladoresReaisTest test
```

## Limites

Os testes ainda precisam ser executados no ambiente do projeto. Uma etapa complementar deve testar o acesso autorizado com CSRF válido e verificar o comportamento de negócio dos serviços reais.
