# ETAPA 83 — Inteligência de Progressão de Carga

## Objetivo
Transformar o histórico real de séries da Etapa 82 em uma recomendação simples, explicável e conservadora para o próximo treino.

## Regras
- Sem histórico: orientar a começar pela prescrição do Personal.
- RPE >= 9: não recomendar aumento; priorizar técnica e consolidação.
- Repetições abaixo do mínimo prescrito: consolidar a carga atual.
- Repetições no topo da faixa e RPE <= 8: sinalizar possibilidade de pequeno aumento, sempre condicionado à técnica e ao Personal.
- Demais cenários: manter e coletar mais dados.

## Segurança
O sistema NÃO altera a prescrição automaticamente e NÃO escolhe uma nova carga numérica sem evidência confiável. A recomendação é informativa.

## Integração
A recomendação aparece no Portal do Aluno, por exercício, junto ao histórico da última execução.

## Banco
Nenhuma tabela ou coluna nova é necessária. A etapa usa `execucoes_series` criada na Etapa 82.
