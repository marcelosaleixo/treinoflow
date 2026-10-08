# Etapa 81 — Experimentos A/B de Retenção

## Objetivo
Transformar o aprendizado contínuo em aprendizado experimental: o TreinoFlow passa a comparar duas mensagens elegíveis, distribuir variantes A/B de forma determinística por aluno e registrar a variante usada na ação automática.

## Regras
- mínimo de duas mensagens elegíveis;
- cada mensagem precisa ter pelo menos 3 resultados vencedores no histórico;
- experimento é isolado por Personal + faixa de risco + tipo de ação;
- distribuição A/B é determinística por aluno e experimento, evitando troca de variante entre execuções;
- o resultado final continua sendo registrado em `acoes_assistente`;
- quando um experimento é encerrado, o motor volta ao aprendizado contínuo normal até surgir novo conjunto elegível;
- não há envio de mensagem sem a automação já estar habilitada.

## Fluxo
Risco → Aprendizado → duas mensagens → experimento A/B → execução → resultado → próxima decisão.

## Tela
`/assistente/experimentos` mostra os experimentos do Personal e permite encerrar os ativos.

## Métrica
A tela compara cada variante por número de ações, recuperações/renovações e taxa de sucesso. O vencedor não é promovido automaticamente durante o experimento: o objetivo é observar evidência suficiente antes de uma futura etapa de promoção.
