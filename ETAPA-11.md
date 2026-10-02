# TreinoFlow — Etapa 11

## Relatório de avaliações e correção de precisão numérica

- Adicionada página de relatório por aluno com histórico de avaliações.
- Botão **Imprimir / Salvar em PDF** utiliza a impressão do navegador (destino: Salvar como PDF), sem dependência externa ou serviço de terceiros.
- Relatório inclui identificação do aluno, objetivo, contato, avaliações, medidas e observações.
- Adicionada migration PostgreSQL para aumentar precisão das colunas de peso e medidas que poderiam causar `numeric field overflow`.
- Entidade `AvaliacaoFisica` atualizada para refletir os tipos SQL.
- Mantida injeção de dependências com `@Autowired`.

## Aplicação da migration

Execute `src/main/resources/db/migration/V11__aumenta_precisao_medidas_avaliacao.sql` uma vez no banco PostgreSQL existente. Se o projeto não usa Flyway, o arquivo é uma migration SQL manual; ele não será executado automaticamente. Faça backup antes de alterar o schema.

## Exportar PDF

Na lista de avaliações do aluno, clique em **Relatório / PDF** e depois em **Imprimir / Salvar em PDF**. No diálogo do navegador, selecione **Salvar como PDF**.

A compilação e os testes integrados não foram executados neste ambiente.
