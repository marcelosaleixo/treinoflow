# Correções TreinoFlow v11 — erros do log de 02/10/2026

## 1. `LazyInitializationException` ao listar personais

O repositório agora possui `findAllComPlano()`, usando `@EntityGraph(attributePaths = "plano")` para buscar os planos na mesma consulta. As telas administrativas de listagem e painel recente usam esse método. Isso resolve o acesso a `p.plano.nome` com `spring.jpa.open-in-view=false`.

## 2. Coluna `perfil` com registros preexistentes

Hibernate não consegue adicionar diretamente uma coluna `NOT NULL` a uma tabela já populada sem valor para as linhas antigas. Foi incluído o SQL `db/patches/20261002_corrigir_perfil_usuario_personal.sql`, que cria a coluna se necessário, preenche valores nulos/vazios com `PERSONAL`, define default e então aplica `NOT NULL`.

### Aplicação
1. Faça backup do banco.
2. Execute o SQL acima uma única vez no banco TreinoFlow.
3. Reinicie a aplicação.

O SQL preserva perfis já preenchidos, inclusive `MASTER`. A entidade continua declarando `perfil` como obrigatório e inicializando novos usuários como `PERSONAL`.
