# Etapa 181 — Contador de notificações não lidas

## Implementação
- O cabeçalho compartilhado mostra um badge vermelho junto ao item Notificações quando há avisos não lidos.
- O contador é calculado para o personal autenticado usando `countByPersonalIdAndLidaFalse`; os dados de outros personais não são expostos.
- Quando a contagem é zero, o badge não aparece. Acima de 99, a exibição é `99+`.
- A contagem é atualizada em cada nova requisição/renderização do cabeçalho; não há polling em tempo real nesta etapa.
- Falhas na consulta da contagem não bloqueiam a renderização das demais páginas.

## Validação
O pacote foi verificado quanto à integridade ZIP. Compilação Maven e testes de execução devem ser realizados no ambiente do projeto.
