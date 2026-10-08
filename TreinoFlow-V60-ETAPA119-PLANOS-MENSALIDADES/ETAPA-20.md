# TreinoFlow — Etapa 20: duplicar treino para outro aluno

## Implementado
- A partir da lista de treinos, o personal pode abrir a opção **Para outro aluno**.
- O sistema apresenta somente alunos ativos pertencentes ao personal autenticado.
- A cópia preserva nome, descrição, exercícios, ordem, séries, repetições, carga, descanso e observações.
- A cópia é sempre criada como **RASCUNHO**, sem token público, validade, data de liberação ou visualizações da origem.
- O backend valida que o treino de origem e o aluno de destino pertencem ao mesmo personal.
- Não é permitido escolher o mesmo aluno como destino nessa operação; para isso permanece disponível a ação **Duplicar**.
- Após a cópia, o sistema encaminha o personal diretamente para o editor do novo treino.
- Não exige alteração de banco de dados.

## Como testar
1. Abra os treinos de um aluno que tenha pelo menos um treino com exercícios.
2. Clique em **Para outro aluno**.
3. Escolha outro aluno ativo e confirme a duplicação.
4. Verifique que o novo treino aparece para o aluno de destino como **RASCUNHO**.
5. Confirme que os exercícios e parâmetros da prescrição foram copiados.
6. Confirme que o link público, visualizações e validade do treino original não foram copiados.
7. Tente acessar a URL de duplicação usando um treino de outro personal; a validação de propriedade deve impedir o acesso.
8. Use **Duplicar** na lista para confirmar que a cópia para o mesmo aluno continua funcionando.

## Validação
Execute `mvnw.cmd clean test` no Windows ou `./mvnw clean test` em ambiente Unix com acesso ao repositório Maven e valide o fluxo com PostgreSQL.
