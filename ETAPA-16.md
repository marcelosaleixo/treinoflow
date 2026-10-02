# TreinoFlow — Etapa 16: editar a prescrição de exercícios

## Implementado
- Cada exercício adicionado ao treino pode ter séries, repetições, carga, descanso e observações ajustados diretamente no editor.
- A atualização preserva exercício, ordem e vínculo com o treino.
- Validação no serviço impede séries menores que 1 e descanso negativo.
- O serviço confirma que o treino pertence ao personal autenticado e que o item pertence ao treino informado.
- Treinos liberados permanecem bloqueados para edição; é necessário revogar o acesso antes de alterar a prescrição.
- Campos de texto são normalizados, removendo espaços excedentes e convertendo valores vazios em nulos.

## Como testar
1. Abra um treino em rascunho e expanda **Editar prescrição** em um exercício.
2. Altere séries, repetições, carga, descanso ou observações e salve.
3. Confirme que os valores atualizados aparecem no editor e na página pública após liberar o treino.
4. Teste séries iguais a zero e descanso negativo; o sistema deve rejeitar os valores.
5. Libere um treino e confirme que os controles de edição ficam indisponíveis.
6. Revogue o acesso e confirme que a edição volta a ficar disponível.

## Validação
Execute `./mvnw test` (Linux/macOS) ou `mvnw.cmd test` (Windows) e teste com o PostgreSQL da aplicação.
