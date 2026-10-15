# Etapa 204 — Integração idempotente de incidentes e fila de e-mails

## Objetivo
Garantir que a criação de um incidente operacional não gere múltiplas linhas de entrega para o mesmo incidente quando a rotina for repetida ou houver concorrência entre instâncias.

## Alterações
- `EntregaAlertaEmail`: restrição única para `incidente_id`.
- `EntregaAlertaEmailRepository`: consulta `existsByIncidenteId`.
- `FilaAlertaEmailService.enfileirar`: valida o identificador, evita duplicação em chamadas repetidas e trata a corrida entre instâncias usando a restrição única do banco.
- Reenvio manual continua reutilizando a entrega existente por meio do método `reenviar`, sem criar uma nova linha.

## Configuração e banco
O projeto utiliza `spring.jpa.hibernate.ddl-auto=update` no perfil atual. Em ambientes com `validate` ou migrações controladas, crie previamente o índice único, após verificar se há dados duplicados:

```sql
SELECT incidente_id, COUNT(*)
FROM entregas_alertas_email
GROUP BY incidente_id
HAVING COUNT(*) > 1;

CREATE UNIQUE INDEX uk_entrega_email_incidente
ON entregas_alertas_email (incidente_id);
```

Se a consulta encontrar duplicatas, faça uma reconciliação manual antes de criar o índice. Não apague entregas sem verificar o histórico.

## Como validar
1. Crie um incidente com destinatário de alertas configurado.
2. Verifique que há uma linha em `entregas_alertas_email` para o `incidente_id`.
3. Repita a chamada de enfileiramento para o mesmo incidente e confirme que a contagem continua em 1.
4. Dispare chamadas concorrentes para o mesmo incidente e confirme que a restrição única impede duplicação.
5. Use o reenvio administrativo e confirme que a linha existente volta a `PENDENTE`.

## Limitações
- A garantia de uma linha por incidente não equivale a entrega SMTP exatamente uma vez. Uma falha após o SMTP aceitar a mensagem e antes da gravação de `ENVIADO` ainda pode ocasionar reenvio após expiração da reserva.
- A restrição única pode falhar ao ser criada em bases com duplicatas existentes; verifique os dados antes da implantação.
- Compilação e testes automatizados precisam ser executados no ambiente do projeto.
