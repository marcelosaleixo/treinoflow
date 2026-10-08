# ETAPA 66 — Gamificação do Personal

## Objetivo
Criar uma camada de gamificação sobre os indicadores já existentes no TreinoFlow, sem criar nova tabela ou exigir cadastro adicional.

## Rota
- `GET /gamificacao`

## Pontuação
A pontuação do mês atual é calculada a partir de:
- faixas de atingimento das metas comerciais;
- treinos concluídos;
- alunos recuperados/renovados;
- carteira ativa.

## Níveis
- Iniciante: 0–99 pontos
- Em evolução: 100–249
- Alta performance: 250–499
- Elite: 500+

## Conquistas
São derivadas automaticamente dos indicadores atuais: meta geral atingida, recuperação em meta, execução de treinos, carteira em meta e alta performance.

## Banco de dados
Nenhuma migration ou tabela nova. O cálculo reutiliza `MetaComercialService`, `MetaComercial`, `InteracaoCrmRepository` e `RegistroTreinoAlunoRepository`.

## Observação
A pontuação é um indicador operacional e não representa receita, comissão ou ranking entre pessoas. O objetivo é incentivar consistência e acompanhamento da carteira.
