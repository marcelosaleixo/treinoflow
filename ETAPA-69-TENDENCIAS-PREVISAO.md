# ETAPA 69 — Tendências e Previsão de Performance

## Objetivo
Transformar o histórico de performance em uma visão proativa, com tendência, projeção simples do próximo mês, alerta e recomendação.

## Rota
- GET `/gamificacao/tendencias`

## Regra de projeção
A projeção usa a média das variações de pontos dos últimos três intervalos disponíveis e soma essa variação ao resultado atual. O valor é limitado a zero e não representa garantia de resultado.

## Alertas
- Tendência negativa de pontos.
- Queda de recuperações por três meses consecutivos.
- Queda de treinos por três meses consecutivos.
- Mensagem de manutenção quando não houver alerta crítico.

## Segurança
O dashboard utiliza o `UsuarioPersonal` autenticado e calcula o histórico somente para o respectivo Personal.

## Banco
Nenhuma tabela ou migration nova. A etapa reutiliza o histórico existente.
