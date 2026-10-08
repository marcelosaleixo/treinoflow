# Etapa 13 — Paginação reutilizável e modernização das listagens

## Implementado
- Fragmento Thymeleaf reutilizável `templates/fragments/paginacao.html`.
- Paginação no servidor com Spring Data `Page`/`Pageable` para alunos, avaliações físicas, exercícios e treinos por aluno.
- Navegação primeira/anterior/próxima/última, indicador da página e contagem de registros.
- Tamanho padrão de 10 registros; parâmetro `size` limitado entre 5 e 50 para evitar consultas excessivas.
- Mantido isolamento por personal/aluno nas consultas paginadas.
- Layout das quatro listagens modernizado, responsivo, com cabeçalhos, painéis, tabelas e botões consistentes.
- CSS compartilhado em `static/css/paginacao.css`.
- Injeção de dependências preservada com `@Autowired`.

## URLs
- `/alunos?page=0&size=10`
- `/alunos/{alunoId}/avaliacoes?page=0&size=10`
- `/exercicios?page=0&size=10`
- `/alunos/{alunoId}/treinos?page=0&size=10`

## Teste
Inicie a aplicação e cadastre mais de 10 registros em uma listagem. Use Próxima/Anterior e confirme que a listagem mantém o aluno correto e que o total de registros é exibido.

Compilação e testes integrados precisam ser executados no ambiente local com Maven e PostgreSQL.
