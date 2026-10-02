# TreinoFlow — Etapa 23: Impressão e exportação para PDF

## Implementado
- Nova página de impressão da ficha de treino, acessível a partir do editor.
- Apresenta aluno, nome e status do treino, descrição e exercícios na ordem cadastrada.
- Inclui séries, repetições, carga, descanso e observações por exercício.
- Layout responsivo e regras de impressão em formato A4; o navegador permite imprimir ou salvar como PDF.
- Rota protegida pelo login e validação de que o treino pertence ao personal autenticado.
- Não adiciona dependência ou alteração de banco de dados.

## Teste manual
1. Entre no editor de um treino e clique em “Imprimir / salvar PDF”.
2. Confira os dados e a ordem dos exercícios.
3. Clique em “Imprimir / Salvar PDF” e selecione a opção de salvar em PDF no navegador.
4. Tente acessar a URL de impressão de um treino pertencente a outro personal; o acesso deve ser negado pela validação existente.

## Validação
Executar `mvnw.cmd clean test` no Windows ou `./mvnw clean test` em ambiente com acesso ao repositório Maven.
