# Etapa 149 — Testes automatizados dos alertas financeiros

## Objetivo
Adicionar testes unitários para validar cenários críticos de sincronização das notificações de metas de receita sem exigir uma conexão real com PostgreSQL.

## Cobertura incluída
- Personal sem ID é ignorado sem consultar repositórios.
- Meta não definida no mês atual gera uma notificação com chave estável.
- Notificação existente é atualizada em vez de criar uma segunda notificação para a mesma chave.
- Meta não definida em um mês passado não gera alerta de meta ausente.

## Execução local (Windows)
Na raiz do projeto:

```powershell
.\mvnw.cmd -Dtest=AlertaMetaReceitaPersistenteServiceTest test
```

Para executar a suíte completa:

```powershell
.\mvnw.cmd test
```

## Observações
Os testes usam mocks dos repositórios e não substituem testes de integração com PostgreSQL. A execução dos testes precisa ser confirmada no ambiente de desenvolvimento, com o Maven Wrapper e dependências disponíveis.
