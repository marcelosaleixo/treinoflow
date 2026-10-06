# ETAPA 56 — Agenda do Personal

## Objetivo
Adicionar uma agenda operacional ao TreinoFlow para que o Personal organize sessões, avaliações e compromissos da própria carteira.

## Entregas
- Nova rota `GET /agenda` com visão diária.
- Navegação entre dia anterior, dia atual e próximo dia.
- Cadastro e edição de agendamentos.
- Aluno limitado aos alunos ativos do Personal autenticado.
- Tipos: `TREINO`, `AVALIACAO`, `CONSULTA` e `OUTRO`.
- Status: `AGENDADO`, `CONFIRMADO`, `REALIZADO`, `CANCELADO` e `FALTOU`.
- Ações rápidas para confirmar, marcar como realizado e cancelar.
- Exclusão com confirmação.
- Bloqueio de conflito de horário para o mesmo Personal, exceto compromissos cancelados.
- Layout responsivo para celular e desktop.
- Migration `V43__agenda_agendamentos.sql`.

## Segurança e isolamento
Todas as operações usam o `personalId` obtido do usuário autenticado. O aluno também é validado por `aluno.personal.id`, impedindo que um Personal agende um aluno de outra carteira.

## Banco
Nova tabela `agendamentos`, com índices por Personal/data e aluno/data. A constraint `fim > inicio` impede intervalos inválidos.

## Próxima evolução recomendada
Integrar a agenda ao Portal do Aluno e às notificações para lembretes de sessão, confirmação automática e registro de presença.

## Validação
Foi feita validação estrutural dos arquivos e referências. O build Maven completo depende do ambiente possuir acesso aos artefatos do Maven Wrapper.
