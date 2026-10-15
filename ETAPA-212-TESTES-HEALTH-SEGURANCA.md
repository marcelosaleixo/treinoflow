# Etapa 212 — Testes do health check e revisão de segurança

## Objetivo
Validar operacionalmente o endpoint de saúde e impedir que os scripts de verificação aceitem uma resposta que exponha `components` ou `details`.

## Implementado
- `scripts/health-check.sh`: valida resposta HTTP, estado `UP` com espaços JSON permitidos e ausência de campos `components`/`details`.
- `scripts/health-check.ps1`: valida estado `UP` e rejeita exposição de componentes/detalhes.
- Ambos retornam código diferente de zero em falhas para integração com pipeline de deploy.

## Execução
Linux/Docker:
```bash
TREINOFLOW_BASE_URL=http://localhost:8080 ./scripts/health-check.sh
```
Windows PowerShell:
```powershell
$env:TREINOFLOW_BASE_URL = 'http://localhost:8080'
./scripts/health-check.ps1
```

## Revisão de segurança obrigatória
1. Confirmar `management.endpoints.web.exposure.include=health`.
2. Confirmar `management.endpoint.health.show-details=never`.
3. Confirmar que `/actuator/health` é público apenas porque retorna estado agregado; endpoints administrativos continuam sob `/admin/**` e `ROLE_MASTER`.
4. Não expor `/actuator/env`, `/actuator/configprops`, `/actuator/beans` ou `/actuator/heapdump` na internet.
5. Executar o health check contra uma instância real e confirmar que retorna código `0` somente com `status=UP` sem detalhes internos.

## Limites
Esses scripts são smoke tests HTTP, não substituem testes automatizados do filtro Spring Security, testes de login/CSRF nem validação de todos os componentes. A execução dos scripts e a compilação Maven devem ser realizadas no ambiente de desenvolvimento/deploy.
