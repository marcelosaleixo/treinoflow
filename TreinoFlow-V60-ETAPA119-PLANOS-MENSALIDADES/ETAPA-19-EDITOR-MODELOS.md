# Etapa 19 — Editor de Modelos de Treino

O TreinoFlow agora permite criar modelos do zero e editá-los sem precisar criar primeiro um treino de aluno.

## Fluxo
1. Modelos de treino → Novo modelo.
2. Informe nome e descrição.
3. Adicione exercícios disponíveis para o personal.
4. Configure séries, repetições, carga, descanso e observações.
5. Reordene os exercícios com ↑/↓ ou remova itens.
6. Salve as alterações e aplique o modelo a um aluno.

## Segurança
Todas as operações validam o `personalId` do usuário autenticado. Exercícios globais ou pertencentes ao próprio personal podem ser usados; exercícios de outro personal não podem ser adicionados.

## Banco
A estrutura da Etapa 18 continua sendo utilizada. Não foi criada nova tabela nesta etapa.
