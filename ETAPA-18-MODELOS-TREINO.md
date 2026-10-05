# TreinoFlow — Etapa 18: Modelos de treino

## Implementado
- Criar modelo a partir de um treino existente.
- Copiar exercícios e toda a prescrição para o modelo.
- Listar e buscar modelos por nome.
- Aplicar um modelo a qualquer aluno ativo do mesmo personal.
- A aplicação cria sempre um treino RASCUNHO para revisão antes da liberação.
- Isolamento por personal no backend.
- Exclusão do modelo com seus itens.
- Botões no editor: Salvar como modelo e Meus modelos.

## Banco
- `modelos_treino`
- `modelos_treino_exercicios`
- Migration `V32__modelos_treino.sql`.

## Regra de segurança
Um personal só acessa seus próprios modelos e só pode aplicá-los aos seus próprios alunos ativos.

## Validação
O código-fonte foi atualizado. O build Maven não pôde ser concluído neste ambiente porque o Maven Wrapper precisou baixar o Maven de `repo.maven.apache.org`, sem acesso externo disponível.
