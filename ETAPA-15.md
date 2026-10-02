# TreinoFlow — Etapa 15: dashboard com indicadores reais

## Implementado
- Dashboard passa a exibir a quantidade de alunos ativos do personal autenticado.
- Exibe total de treinos cadastrados e total de treinos com status `LIBERADO`.
- Exibe a quantidade de exercícios disponíveis ao personal (exercícios próprios e globais).
- Consultas de contagem no banco são executadas por repositórios, sem carregar listas completas em memória.
- Mantida injeção de dependências com `@Autowired`, conforme padrão solicitado.
- Atualizado o layout do painel com cartões responsivos, hierarquia visual e atalhos para alunos e exercícios.
- Consultas de treinos são filtradas pelo personal proprietário por meio do relacionamento `Treino -> Aluno -> Personal`.

## Como testar
1. Inicie a aplicação e entre com um usuário personal.
2. Confira os indicadores no dashboard.
3. Cadastre um aluno e confirme que o total de alunos ativos é atualizado.
4. Crie treinos para esse aluno e confira o total de treinos.
5. Libere um treino e confirme que o indicador de liberados aumenta.
6. Compare a contagem de exercícios com os exercícios globais e os próprios do personal.
7. Entre com outro personal e confirme que os números de alunos e treinos são isolados por conta; exercícios globais aparecem para ambos.

## Validação
A estrutura e os arquivos foram atualizados. Execute `./mvnw test` (Linux/macOS) ou `mvnw.cmd test` (Windows) no ambiente do projeto e valide com o PostgreSQL utilizado pela aplicação.
