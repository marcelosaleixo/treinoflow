# Etapa 211 — Monitoramento pós-implantação

## Entregas

- Adicionado Spring Boot Actuator.
- Exposto somente o endpoint de saúde (`/actuator/health`).
- Detalhes internos do health check não são exibidos publicamente.
- Criados scripts PowerShell e shell para verificar a disponibilidade após o deploy.
- Verificação do banco de dados usa o health indicator padrão do Spring Boot quando a conexão JPA/DataSource está configurada.
- Health check de e-mail foi desativado para não tentar validar SMTP como parte da disponibilidade HTTP; o envio SMTP deve ser validado separadamente por teste controlado.

## Como testar

PowerShell:

```powershell
$env:TREINOFLOW_BASE_URL = "https://seu-dominio.com"
./scripts/health-check.ps1
```

Linux/Docker:

```bash
export TREINOFLOW_BASE_URL="https://seu-dominio.com"
./scripts/health-check.sh
```

O padrão local é `http://localhost:8080`. Ajuste a URL para a porta/domínio real do ambiente.

## Códigos de saída

- `0`: health check respondeu `UP`.
- `1`: falha HTTP/rede/timeout.
- `2`: resposta não saudável ou ferramenta ausente.

## Segurança

- Não expor `/actuator/**` inteiro na internet.
- Não habilitar `show-details=always` em produção.
- Não incluir senhas, tokens ou variáveis de ambiente em logs de diagnóstico.
- Restringir `/actuator/health` na camada de proxy/firewall quando possível, especialmente em ambientes sensíveis.

## Critérios de liberação

1. Compilar o projeto com Java 21 e Maven.
2. Executar testes automatizados, incluindo os testes PostgreSQL/Testcontainers.
3. Confirmar `GET /actuator/health` retorna HTTP 200 e `status=UP`.
4. Confirmar o indicador de banco fica `UP` com banco conectado e que a aplicação fica não saudável quando o banco está indisponível, conforme a configuração do ambiente.
5. Validar login, acesso de MASTER, CSRF no reenvio de alertas e envio SMTP controlado.
6. Validar o script de health check no ambiente de destino.

> A criação dos scripts e configuração não significa que testes ou implantação em produção foram executados. Execute os passos acima antes de liberar.
