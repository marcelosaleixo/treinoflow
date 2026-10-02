# TreinoFlow — Etapa 21: Reordenar exercícios do treino

## Implementado
- Controles para mover cada exercício uma posição para cima ou para baixo no editor.
- A ordem é persistida e refletida na prescrição pública do treino.
- Os controles são desativados nos limites da lista e para treinos liberados.
- A operação valida propriedade do treino pelo personal autenticado e impede alterações em treinos liberados.
- Renumeração em duas fases evita colisão na constraint única `(treino_id, ordem)`.

## Como testar
1. Abra um treino em rascunho com pelo menos três exercícios.
2. Use ↑ e ↓ e confirme que a numeração e a sequência mudam após recarregar.
3. Libere o treino e confirme que os controles de reorganização não aparecem.
4. Teste com outro personal e confirme que não consegue reorganizar treino alheio.

## Validação
A alteração foi aplicada ao código-fonte. Execute `mvnw.cmd clean test` no Windows e valide com o PostgreSQL configurado; compilação integrada não executada neste ambiente.
