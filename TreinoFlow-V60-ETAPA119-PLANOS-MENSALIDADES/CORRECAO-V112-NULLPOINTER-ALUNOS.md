# Correção V112 — NullPointerException na listagem de alunos

Causa: `List.of("ATIVO", "INATIVO").contains(filtroStatus)` lança NullPointerException quando `filtroStatus` é null, pois listas imutáveis criadas com `List.of` não aceitam `contains(null)`.

Correção: verificar `filtroStatus == null` antes de chamar `contains`. Quando o filtro estiver ausente ou inválido, ele é normalizado para null e a listagem prossegue sem filtro de status.

Arquivos corrigidos:
- `src/main/java/com/marceloaleixo/treinoflow/service/AlunoService.java`
- cópia equivalente dentro de `TreinoFlow-V60-ETAPA119-PLANOS-MENSALIDADES/`

Validação realizada: verificação textual das duas cópias e integridade do arquivo ZIP. A compilação Maven e a execução da aplicação precisam ser confirmadas no ambiente do projeto.
