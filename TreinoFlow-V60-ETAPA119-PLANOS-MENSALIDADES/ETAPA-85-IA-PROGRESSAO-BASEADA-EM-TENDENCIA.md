# ETAPA 85 — IA de Progressão Baseada em Tendência

## Objetivo

Evoluir a Etapa 84 para que o TreinoFlow não tome uma decisão com base em uma única sessão. A análise agora considera até seis sessões anteriores do mesmo exercício e combina volume, repetições, RPE, consistência e alcance do topo da faixa de repetições.

## O que foi implementado

- `TendenciaProgressaoService` para análise determinística e explicável;
- `AnaliseTendenciaProgressaoView` para exibição no portal;
- comparação entre a sessão mais antiga e a mais recente do recorte;
- variação percentual de volume quando a carga é numérica;
- variação de RPE;
- contagem de sessões que atingiram o topo da faixa prescrita;
- score de tendência de 0 a 100;
- níveis `PROGRESSAO`, `ESTAVEL`, `ATENCAO`, `AGUARDAR` e `SEM_DADOS`;
- confiança `ALTA`, `MEDIA` ou `BAIXA`;
- recomendação conservadora ao Personal;
- painel visual integrado à tela de execução do aluno.

## Regra de segurança

A Etapa 85 não altera automaticamente carga, séries ou repetições. A recomendação de progressão é apresentada como apoio à decisão do Personal.

Alta confiança exige pelo menos quatro sessões e evidência consistente de melhora, com esforço controlado. Queda relevante de volume/repetições ou aumento de RPE gera alerta.

## Banco de dados

Nenhuma migration nova. A análise utiliza a tabela `execucoes_series` criada na Etapa 82.

## Fluxo

```text
Execução por série
        ↓
Histórico de até 6 sessões
        ↓
Volume + Repetições + RPE + Faixa
        ↓
Tendência
        ↓
Confiança
        ↓
Recomendação ao Personal
```
