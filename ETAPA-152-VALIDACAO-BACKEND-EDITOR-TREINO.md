# Etapa 152 — Validação no backend do editor de treino

## Alterações
- Validação no serviço para ordem, séries, descanso, repetições, carga e observações.
- Limites compatíveis com as colunas da entidade `TreinoExercicio` e com valores operacionais razoáveis.
- Verificação prévia de ordem duplicada dentro do mesmo treino, além da constraint única do banco.
- Normalização de campos textuais antes de persistir.
- As validações são aplicadas tanto ao adicionar quanto ao atualizar uma prescrição; não dependem do JavaScript do navegador.
- Mantidas as verificações de propriedade do treino, do personal e do exercício, bem como a proteção de treinos liberados.

## Teste sugerido
Na raiz do projeto, execute `./mvnw test` (Windows: `\.mvnw.cmd test`) e teste adição/edição com valores válidos e inválidos. Confirme também a mensagem amigável quando a ordem já estiver ocupada.

## Observação
O ZIP foi verificado quanto à integridade. Compilação e execução contra PostgreSQL devem ser confirmadas no ambiente de desenvolvimento.
