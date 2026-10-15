# Etapa 213 — Testes automatizados de segurança dos endpoints

## Objetivo

Criar testes MVC focados nas regras declaradas em `SecurityConfig`, sem exigir banco de dados ou SMTP para esses cenários.

## Cenários cobertos

- visitante redirecionado ao tentar acessar uma rota protegida;
- usuário `ROLE_PERSONAL` impedido de acessar `/admin/**`;
- usuário `ROLE_MASTER` autorizado em `/admin/**`;
- requisição POST sem token CSRF rejeitada;
- requisição POST com token CSRF válido aceita.

O teste usa um controlador de prova isolado, não substitui testes de integração dos controladores reais nem valida configuração de proxy/rede do ambiente publicado.

## Execução

```bash
mvn -Dtest=Etapa213SegurancaEndpointsTest test
```

No Windows, se o Maven Wrapper estiver disponível:

```powershell
./mvnw.cmd -Dtest=Etapa213SegurancaEndpointsTest test
```

## Verificações manuais complementares

1. Confirmar que `/actuator/health` não exibe componentes ou detalhes internos.
2. Confirmar que somente `ROLE_MASTER` acessa os painéis `/admin/**`.
3. Confirmar que formulários POST da aplicação enviam token CSRF.
4. Validar HTTPS, segredos e regras de firewall no ambiente real.

## Limites

A criação do teste não significa que ele foi executado. Execute o comando acima e a suíte completa no ambiente de desenvolvimento/CI antes de publicar. A regra `permitAll` de `/webhooks/mercadopago/**` deve continuar validando a assinatura do provedor no próprio webhook; autorização HTTP pública não substitui autenticação criptográfica do evento.
