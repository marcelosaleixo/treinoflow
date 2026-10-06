# ETAPA 55 — Progressão de Carga

## Objetivo
Transformar o histórico de execução do treino em um comparativo simples entre a carga atual e a carga anterior de cada aluno por exercício.

## Funcionalidades
- Nova tela `/performance/progressao`.
- Compara as duas execuções concluídas mais recentes por aluno + exercício dentro dos últimos 30 dias.
- Identifica quatro situações: `PROGRESSO`, `ESTAVEL`, `QUEDA` e `SEM_COMPARACAO`.
- Exibe carga anterior, carga atual, repetições anteriores/atuais, variação percentual e data.
- Mostra uma sugestão operacional para o Personal.
- Ordena quedas primeiro para facilitar acompanhamento.
- Isola os dados pelo Personal autenticado.
- Não altera automaticamente o treino.

## Regra de comparação
Quando as duas cargas possuem números identificáveis:
- variação >= 2%: PROGRESSO;
- variação <= -2%: QUEDA;
- entre -2% e +2%: ESTAVEL.

Quando uma das cargas não é numérica ou não existe uma segunda execução: SEM_COMPARACAO.

A leitura numérica é uma aproximação para formatos de carga já existentes, como `20 kg`, `20kg` ou `3x20kg`; o sistema não assume unidade específica.

## Arquitetura
- `ProgressaoTreinoView` — DTO da tela.
- `ProgressaoTreinoService` — comparação e regras.
- `PerformanceController` — endpoint `/performance/progressao`.
- `performance/progressao.html` — interface responsiva.

## Banco de dados
Nenhuma migration nova. A funcionalidade reutiliza `execucoes_exercicios` e `registros_treino_aluno` já existentes.

## Segurança
A tela usa o `personalId` obtido do usuário autenticado e consulta apenas execuções pertencentes à carteira daquele Personal.

## Observação
A sugestão é apoio à tomada de decisão do profissional. O TreinoFlow não modifica automaticamente carga, séries ou repetições.
