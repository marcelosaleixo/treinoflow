# Etapa 218 — Testes do fluxo HTTP e persistência do reenvio administrativo

## Objetivo

Validar separadamente as duas fronteiras críticas do reenvio de alertas: a requisição HTTP real atendida pelo controlador e a transição de estado persistida no PostgreSQL pelo serviço.

## Testes adicionados

- `Etapa218FluxoHttpReenvioTest`: usa MockMvc e o controlador real; verifica redirecionamento para visitante, bloqueio de `ROLE_PERSONAL`, rejeição de POST sem CSRF, reenvio autorizado com CSRF, feedback para repetição recusada e normalização dos filtros do painel.
- `Etapa218PersistenciaReenvioPostgresIT`: usa PostgreSQL 16 via Testcontainers; verifica a transição `FALHA_FINAL` → `PENDENTE`, a recusa de uma segunda solicitação e a preservação de uma única entrega para o incidente.

**Limite importante:** são testes complementares, não um único teste que atravessa a requisição HTTP até o PostgreSQL na mesma execução. O teste HTTP simula o serviço; o teste PostgreSQL chama o serviço diretamente. SMTP real não é acionado nesta etapa.

## Execução

```bash
mvn -Dtest=Etapa218FluxoHttpReenvioTest test
mvn -Dtest=Etapa218PersistenciaReenvioPostgresIT test
```

No Windows com Maven Wrapper:

```powershell
.\mvnw.cmd -Dtest=Etapa218FluxoHttpReenvioTest test
.\mvnw.cmd -Dtest=Etapa218PersistenciaReenvioPostgresIT test
```

O teste de integração PostgreSQL requer Docker. Se Docker não estiver disponível, Testcontainers está configurado para desabilitar essa classe, sem representar aprovação do teste de integração.
