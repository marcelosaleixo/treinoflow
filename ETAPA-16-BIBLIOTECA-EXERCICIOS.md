# TreinoFlow — Etapa 16: Biblioteca inteligente de exercícios

## Objetivo
Tornar a biblioteca de exercícios mais útil agora que o sistema possui exercícios globais pré-cadastrados.

## Implementado
- Busca por nome do exercício.
- Filtro por grupo muscular.
- Combinação de nome + grupo muscular na mesma consulta.
- Paginação preservando os filtros selecionados.
- Identificação visual entre exercício global da biblioteca TreinoFlow e exercício particular do personal.
- Exibição opcional da descrição do exercício na listagem.
- Ações de edição continuam disponíveis apenas para exercícios pertencentes ao personal.
- Exercícios globais continuam somente leitura para o personal.
- Isolamento por personal mantido no backend: a consulta retorna apenas exercícios globais ou do personal autenticado.

## Regras
- Exercício global: `personal_id IS NULL` e disponível para todos os personais.
- Exercício particular: pertence a um único personal.
- O filtro de grupo usa o enum `GrupoMuscular` já existente no projeto.
- Nenhuma alteração de banco é necessária.

## Como testar
1. Acesse `/exercicios`.
2. Pesquise `supino` e confirme que somente exercícios relacionados ao termo aparecem.
3. Selecione `Peito` e confirme o filtro.
4. Combine `supino` + `Peito`.
5. Navegue entre páginas e confirme que os filtros são preservados.
6. Confirme que exercícios globais não exibem o botão Editar.
7. Crie um exercício próprio e confirme que ele aparece como `Meu exercício` e pode ser editado.
