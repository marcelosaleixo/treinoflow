# Etapa 187 — Retenção do histórico de auditoria de preferências

## Objetivo
Controlar o crescimento da tabela `auditoria_preferencias_notificacao` com uma política de retenção configurável, preservando o histórico por padrão.

## Implementação
- Repositório: consulta de exclusão em lote por data (`alteradoEm < limite`).
- Serviço agendado: `RetencaoAuditoriaPreferenciasService`.
- Configuração por variáveis de ambiente, sem credenciais no código.
- Limite mínimo de retenção de 30 dias para evitar configurações acidentais muito curtas.
- Execução diária padrão às 03:30, somente se a rotina estiver explicitamente habilitada.
- Log de quantidade removida, sem expor dados pessoais.

## Configuração
Por padrão a limpeza fica desligada:

```env
TREINOFLOW_AUDITORIA_PREFERENCIAS_ENABLED=false
TREINOFLOW_AUDITORIA_PREFERENCIAS_RETENCAO_DIAS=365
TREINOFLOW_AUDITORIA_PREFERENCIAS_CRON=0 30 3 * * *
```

Para habilitar a limpeza automática, defina `TREINOFLOW_AUDITORIA_PREFERENCIAS_ENABLED=true` após validar a política de retenção e os requisitos legais/contratuais. A rotina remove registros anteriores ao período configurado; não é uma ferramenta de restauração.

## Validação recomendada
1. Executar `mvnw.cmd test` no Windows ou `./mvnw test` no Linux.
2. Com a rotina desabilitada, confirmar que nenhum registro é removido.
3. Em banco de homologação, criar registros com datas antigas e recentes e habilitar a rotina.
4. Confirmar que somente registros anteriores ao limite são removidos.
5. Verificar a integridade da consulta paginada do histórico após a limpeza.

## Segurança operacional
Não habilitar automaticamente em produção sem confirmar a política de retenção aplicável. Fazer backup antes do primeiro teste em ambiente real.
