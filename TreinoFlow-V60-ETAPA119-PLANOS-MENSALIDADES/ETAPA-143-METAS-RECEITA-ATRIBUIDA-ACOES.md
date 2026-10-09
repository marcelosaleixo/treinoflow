# Etapa 143 — Metas de receita atribuída às ações de CRM

## Entrega
- Tela `/financeiro/metas-receita-acoes` para consultar mês/ano, definir ou atualizar meta e acompanhar receita vinculada.
- Meta individual por personal e por mês.
- Indicadores de meta, realizado, saldo restante e percentual de atingimento.
- Lista dos pagamentos vinculados considerados no mês selecionado.
- Atalho adicionado ao painel financeiro.
- Entidade, repositório e migration SQL `V48__metas_receita_acoes_crm.sql`.

## Regra de cálculo
O realizado soma o valor dos pagamentos vinculados às ações do personal autenticado, usando a data de pagamento para determinar o mês. A meta é salva com o primeiro dia do mês como referência.

## Instalação/validação
Aplicar a migration V48 antes de executar se o projeto usa migrations SQL controladas. Confirmar o nome real da tabela de usuários no banco: a migration referencia `usuarios_personal(id)`; ajuste a FK se o schema instalado usar outro nome. Executar testes de compilação e integração no ambiente local.
