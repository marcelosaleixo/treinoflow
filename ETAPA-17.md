# TreinoFlow — Etapa 17: duplicar programas de treinamento

## Implementado
- A listagem de treinos ganhou a ação **Duplicar**.
- A cópia preserva nome (com prefixo `Cópia -`), descrição, exercícios, ordem e parâmetros da prescrição (séries, repetições, carga, descanso e observações).
- A cópia é sempre criada como **RASCUNHO**, sem token público, validade, data de liberação ou visualizações anteriores.
- A operação verifica a propriedade do treino pelo personal autenticado e executa em transação.
- Após duplicar, o personal é encaminhado diretamente ao editor para revisar a nova prescrição.

## Como testar
1. Abra a listagem de treinos de um aluno e clique em **Duplicar** em um treino existente.
2. Confirme que o novo treino abre no editor com os mesmos exercícios e parâmetros.
3. Confirme que o nome começa com `Cópia -` e o status é `RASCUNHO`.
4. Se a origem estiver liberada, confirme que a cópia não reutiliza o link público.
5. Edite a cópia e confirme que o treino original não foi alterado.

## Validação
Execute `./mvnw test` (Linux/macOS) ou `mvnw.cmd test` (Windows) e valide o fluxo com o PostgreSQL da aplicação. A compilação e a execução integrada não foram confirmadas neste ambiente.
