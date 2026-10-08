# Etapa 92 — Perfil Inteligente do Aluno

## Objetivo
Criar uma visão consolidada do aluno usando apenas dados já existentes no TreinoFlow, sem alterar automaticamente prescrição ou treino.

## Dados considerados
- Data de nascimento e idade calculada.
- Objetivo e status cadastral.
- Data de início e tempo de relacionamento.
- Quantidade de treinos cadastrados e liberados.
- Sessões concluídas e sessões dos últimos 30 dias.
- Data da última sessão registrada.

## Classificação
O perfil gera um resumo descritivo e um nível de engajamento observado (ALTO, MODERADO ou BAIXO). As regras são transparentes e determinísticas.

## Segurança
A rota valida que o aluno pertence ao Personal autenticado. Nenhum dado novo é persistido e nenhuma alteração de treino é feita pela análise.

## Rota
`GET /alunos/{id}/perfil-inteligente`

## Banco
Não há nova tabela ou migration nesta etapa. Apenas foram adicionadas consultas de contagem ao `TreinoRepository`.
