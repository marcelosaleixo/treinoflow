# TreinoFlow — Etapa 18: treinos recentes no dashboard

## Implementado
- Dashboard exibe os cinco treinos mais recentes do personal autenticado.
- Cada linha mostra nome do treino, aluno, status e atalho para a lista de treinos do aluno.
- A consulta filtra pelo personal autenticado e faz `JOIN FETCH` do aluno para evitar acesso lazy à relação na renderização.
- Estado vazio orienta o usuário a cadastrar um treino.
- Layout responsivo para telas menores.

## Como testar
1. Entre como personal e abra `/dashboard`.
2. Cadastre treinos para mais de um aluno e confirme que aparecem no dashboard, do mais novo para o mais antigo.
3. Entre com outro personal e confirme que os treinos de terceiros não aparecem.
4. Confirme que o dashboard funciona sem treinos cadastrados e em largura mobile.

## Validação
A alteração foi aplicada ao código-fonte do projeto. Execute `mvnw.cmd test` no Windows e valide com o PostgreSQL configurado. A compilação integrada não foi executada neste ambiente.
