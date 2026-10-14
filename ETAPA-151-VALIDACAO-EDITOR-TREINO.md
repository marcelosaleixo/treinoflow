# Etapa 151 — Validação e feedback do editor de treino

## Alterações
- Confirmação antes de remover um exercício do treino.
- Validação no navegador para ordem (mínimo 1), séries (mínimo 1) e descanso (mínimo 0), respeitando campos opcionais.
- Prevenção de submissões repetidas enquanto o formulário está sendo enviado.
- Feedback visual “Processando...” e `aria-busy` durante o envio.

## Arquivo alterado
- `src/main/resources/templates/treino/editor.html`

A alteração foi replicada também na cópia interna do projeto V60 que já existe no pacote legado.

## Validação
A validação é complementar à validação do servidor; regras de negócio e autorização devem continuar sendo verificadas no backend. Testar manualmente adicionar, editar, mover e remover exercícios, além de formulários com campos opcionais. A compilação Maven não foi executada neste ambiente.
