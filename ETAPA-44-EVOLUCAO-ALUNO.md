# Etapa 44 — Evolução e Acompanhamento do Aluno

## Objetivo
Transformar o Portal do Aluno em uma ferramenta de acompanhamento contínuo: além de marcar o treino como concluído, o aluno registra carga, repetições e observações por exercício.

## Entregas
- Registro real da execução de cada exercício.
- Carga realizada e repetições realizadas.
- Observação por exercício.
- Resumo de frequência em 7 e 30 dias.
- Média das avaliações dos treinos.
- Histórico por exercício.
- Tela de evolução para o Personal em `/alunos/{id}/evolucao`.
- Resumo de evolução no Portal do Aluno.
- Correção preventiva do carregamento dos exercícios do portal com `JOIN FETCH`.

## Banco
Migration: `V42__evolucao_execucao_exercicios.sql`
Tabela: `execucoes_exercicios`.

Cada registro representa a execução de um exercício dentro de uma sessão (`registros_treino_aluno`). Há unicidade por sessão + exercício, permitindo que o aluno retorne ao formulário no mesmo dia sem duplicar a execução.

## Segurança
A tela do Personal valida o aluno pelo `personalId`. O portal continua sendo protegido pelo token individual do aluno e só permite treinos liberados e válidos.

## Observação
O projeto usa `spring.jpa.open-in-view=false`, portanto as consultas usadas no Portal carregam explicitamente os relacionamentos necessários para a renderização.
