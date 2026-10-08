# ETAPA 84 — Análise Multi-Série e Fadiga

A Etapa 84 evolui a inteligência da Etapa 83 para analisar o conjunto completo de séries e as últimas sessões do mesmo exercício.

## Objetivo

Identificar tendências de progressão, estabilidade, queda de desempenho e fadiga intra-sessão sem alterar automaticamente a prescrição do Personal.

## O que foi implementado

- análise das últimas 6 sessões disponíveis;
- soma de repetições por sessão;
- média de RPE;
- RPE da primeira e última série;
- volume estimado (carga numérica × repetições) quando a carga é interpretável;
- comparação da sessão atual com a anterior;
- sinalização de PROGRESSAO, ESTAVEL, QUEDA, FADIGA ou ATENCAO;
- painel visual no portal do aluno;
- nenhum campo novo no banco.

## Regra de segurança

A análise é somente recomendatória. Ela não altera a carga, séries ou repetições prescritas pelo Personal.

## Próxima evolução

A Etapa 85 pode transformar essa análise em uma sugestão de progressão baseada em tendência de várias sessões, volume e RPE, ainda mantendo aprovação do Personal.
