# Etapa 144 — Alertas de metas de receita atribuída

## Entrega
- Nova rota autenticada: `/financeiro/alertas-metas-receita`.
- Mostra status de meta não definida, meta atingida, abaixo do ritmo esperado, no ritmo/acima ou período encerrado.
- Compara receita atribuída com uma referência proporcional aos dias transcorridos do mês.
- Permite consultar mês/ano e lista os pagamentos vinculados considerados.
- Usa `MetaReceitaAcoesRepository` e `InteracaoRecebimentoVinculoRepository`, sem nova tabela ou migração.
- Os dados consultados são limitados ao personal autenticado.

## Limites
Os alertas são calculados ao abrir a página; não há envio de push, e-mail ou WhatsApp nem execução agendada. A referência proporcional é indicativa e não representa previsão de receita.

## Validação
O ZIP pode ser verificado estruturalmente, mas a compilação Maven e os testes integrados devem ser executados no ambiente do projeto com acesso às dependências e ao PostgreSQL.
