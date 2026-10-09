# Correção — Menu global nas páginas autenticadas

## Problema
Algumas páginas do módulo financeiro e telas operacionais renderizavam conteúdo sem o cabeçalho/menu principal do TreinoFlow, causando navegação inconsistente.

## Alterações
- Reutilizado o fragmento compartilhado `fragments/tf-header :: header`.
- Adicionada a folha responsiva `/css/tf-header.css` às páginas que não a carregavam.
- Aplicado o menu em páginas financeiras: conciliação, previsão, resumo por aluno, fechamento mensal, indicadores, metas, despesas, receita em risco e DRE.
- Aplicado também em cobrança, formulário de exercício, telas de modelos de treino, jornada do assistente e notificações.
- Repetidas as mesmas alterações na cópia aninhada do projeto incluída no ZIP.
- Páginas de login/cadastro, portal público do aluno, fragmentos e administração foram mantidas fora desta alteração para não substituir seus fluxos de navegação específicos.

## Validação
Verificar visualmente as páginas após iniciar a aplicação. A compilação Maven não foi confirmada nesta execução.
