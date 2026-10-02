# TreinoFlow — Etapa 10: Dashboard de evolução física

## Implementado
- Página de evolução por aluno com gráficos de peso, cintura/quadril e percentual de gordura ao longo das avaliações.
- Dados ordenados cronologicamente; valores não registrados permanecem como lacunas, sem interpolação.
- A rota valida a propriedade do aluno usando o personal autenticado antes de carregar as avaliações.
- Link **Ver evolução** adicionado à listagem de avaliações.
- Injeção de dependências por atributo com `@Autowired`, conforme o padrão do projeto.
- Gráficos responsivos usando Chart.js via CDN (a visualização dos gráficos requer conexão com a internet no navegador).

## Rota
- `GET /alunos/{alunoId}/avaliacoes/evolucao`

## Teste manual
1. Inicie a aplicação e entre como personal.
2. Abra um aluno e acesse Avaliações → Ver evolução.
3. Registre avaliações em datas diferentes com alguns campos preenchidos e outros vazios.
4. Confirme que os gráficos refletem os valores e que campos ausentes aparecem como lacunas.
5. Teste o acesso com um `alunoId` pertencente a outro personal; a consulta deve ser negada pela validação de propriedade.

## Observações
Os gráficos são informativos e apresentam os dados inseridos pelo profissional; não calculam diagnóstico nem recomendam condutas. O projeto deve ser compilado e testado com PostgreSQL no ambiente local. Para uso sem internet, hospede Chart.js localmente em `static/js` e ajuste o caminho do script.
