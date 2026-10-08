# TreinoFlow — Etapa 6: Gestão de treinos e biblioteca de exercícios

## Entregas
- Listagem de treinos por aluno, com validação de propriedade do aluno pelo personal autenticado.
- Criação de treino (nome e descrição) e editor para adicionar/remover exercícios.
- Configuração por exercício: ordem, séries, repetições, carga, descanso e observações.
- Biblioteca de exercícios com cadastro e edição de exercícios particulares e consulta aos exercícios globais.
- Liberação de treino após adicionar pelo menos um exercício. A tela informa o token gerado; a página pública do aluno fica para etapa posterior.
- Ações POST para alteração/exclusão e navegação responsiva.
- Injeção de dependências por atributo com `@Autowired`, conforme solicitado.

## Segurança e limites
- O personal é resolvido pelo usuário autenticado; não há `personalId` vindo de formulário ou URL.
- Serviços existentes validam a relação treino-aluno-personal e a propriedade dos exercícios particulares.
- Treinos liberados ficam bloqueados para alterações conforme regra já existente.
- O token é exibido no editor, mas ainda não existe rota pública de acesso para aluno.

## Como testar
1. Configure PostgreSQL em `application.properties`/variáveis de ambiente.
2. Execute `mvnw.cmd spring-boot:run` no Windows ou `./mvnw spring-boot:run` no Linux/macOS.
3. Faça login, cadastre um aluno, abra **Treinos**, crie um treino, cadastre exercícios na biblioteca e adicione-os ao treino.
4. Libere o treino depois de incluir pelo menos um exercício.

## Validação
O build Maven foi tentado, mas o Wrapper não conseguiu baixar Maven 3.9.16 de `repo.maven.apache.org` neste ambiente. Portanto, a compilação e o funcionamento integrado ainda precisam ser verificados localmente.

## Próxima etapa sugerida
Etapa 7: página pública do aluno usando token de acesso, visualização mobile do treino liberado e fluxo para revogar/atualizar uma versão de treino.
