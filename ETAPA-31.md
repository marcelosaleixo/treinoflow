# Etapa 31 — Administração Master, personais e planos

- Cadastro e ativação/desativação de contas de personal (cada conta representa um tenant/empresa).
- Cadastro de planos com preço mensal e limite de alunos; associação de plano no cadastro do personal.
- Área `/admin` exclusiva do perfil `MASTER`, painel e listagens responsivas.
- O perfil é carregado no Spring Security. Personal continua no dashboard regular; master é redirecionado ao painel administrativo.
- Provisionamento inicial opcional por variáveis `TREINOFLOW_MASTER_EMAIL` e `TREINOFLOW_MASTER_PASSWORD` (senha mínima 12 caracteres). Configure-as no ambiente de implantação; não use credenciais fixas no código.
- SQL em `db/migration/V31__admin_master_planos.sql` para ambientes com `ddl-auto=validate`.

Observação: esta etapa administra cadastro/planos e status. Cobrança recorrente, faturamento, bloqueio automático por inadimplência e aplicação efetiva do limite de alunos ficam para etapas próprias. O limite é armazenado no plano, ainda não aplicado nas operações de criação de alunos.
