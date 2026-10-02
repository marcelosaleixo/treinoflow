# TreinoFlow — Etapa 24: Acompanhamento de visualizações

## Implementado
- Dashboard lista até cinco treinos liberados do personal autenticado.
- Exibe aluno, quantidade acumulada de visualizações e data/hora do último acesso.
- Ordena os treinos pela visualização mais recente e, em seguida, pela liberação.
- Mantém o escopo por personal na consulta ao banco.
- Reutiliza os campos de visualização já existentes; não exige migration.

## Teste manual
1. Libere um treino para um aluno e abra o link público.
2. Atualize o dashboard do personal e confira a contagem e o último acesso.
3. Abra novamente o link e confirme que a contagem aumenta.
4. Confira que outro personal não visualiza os registros do primeiro.

## Validação
Execute `mvnw.cmd clean test` no Windows ou `./mvnw clean test` em ambiente com acesso ao Maven.
