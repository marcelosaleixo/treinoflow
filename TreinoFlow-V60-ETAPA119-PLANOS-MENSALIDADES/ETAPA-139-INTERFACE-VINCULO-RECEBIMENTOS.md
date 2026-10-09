# Etapa 139 — Interface de vínculo entre ações de CRM e recebimentos

## Entregue
- Tela `/financeiro/vinculos-recebimentos` para consultar ações e recebimentos recentes, criar vínculos e removê-los.
- Service transacional com validação de titularidade do personal, aluno correspondente e duplicidade de atribuição.
- Um pagamento só pode ser atribuído a uma única ação, evitando dupla contagem de receita.
- Exclusão remove somente o vínculo; o lançamento financeiro permanece intacto.
- Atalho adicionado à página principal do Financeiro.

## Segurança e regras
A ação é carregada por `id + personalId`, e o recebimento por `id + personalId`; IDs enviados manualmente não permitem acessar registros de outro personal. A associação só é aceita se os alunos forem iguais.

## Validação
O pacote foi verificado quanto à integridade ZIP. Não foi possível afirmar compilação Maven nem teste integrado com PostgreSQL nesta execução; executar `mvn test` e validar o fluxo no ambiente do projeto antes de publicar.
