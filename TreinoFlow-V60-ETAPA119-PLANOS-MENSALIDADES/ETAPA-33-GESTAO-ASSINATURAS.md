# ETAPA 33 — Gestão de assinatura e status do plano

Implementada a gestão administrativa de assinaturas do TreinoFlow.

## Recursos
- Assinatura vinculada a um Personal e a um plano.
- Status: Em teste, Ativa, Inadimplente, Vencida, Suspensa e Cancelada.
- Data de início, vencimento, próxima cobrança e cancelamento.
- Observação administrativa.
- Tela Master para listar e editar assinaturas.
- Vencimento automático: assinaturas ativas/em teste com vencimento anterior a hoje passam para VENCIDA quando a tela é acessada.
- Migração dos Personais existentes para uma assinatura inicial.
- Plano do Personal é sincronizado quando a assinatura é salva.

## Segurança de negócio
A assinatura é individual por Personal (`personal_id` UNIQUE). O cadastro do plano continua sendo responsabilidade do Master.

## Migração
`db/migration/V33__gestao_assinaturas.sql`

A migração cria a tabela e cria assinaturas iniciais para Personais existentes que já possuem plano.
