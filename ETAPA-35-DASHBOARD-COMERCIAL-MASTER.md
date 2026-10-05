# ETAPA 35 — Dashboard Comercial do Master

## Objetivo
Transformar o painel Master em uma visão comercial do SaaS, usando as assinaturas já existentes.

## Indicadores
- Personais cadastrados e contas ativas.
- MRR estimado: soma do valor mensal dos planos das assinaturas ATIVA e EM_TESTE.
- Quantidade por status: ativa, em teste, inadimplente, vencida, suspensa e cancelada.
- Assinaturas vencendo em 7 e 30 dias.
- Assinaturas que exigem atenção.
- Distribuição de assinaturas ativas por plano.
- Cadastros recentes.

## Segurança e consistência
- O dashboard usa o `AssinaturaService.atualizarVencidas()` antes dos indicadores.
- As consultas de assinatura usam EntityGraph ou join fetch quando exibem Personal e Plano, compatível com `spring.jpa.open-in-view=false`.
- MRR é uma estimativa contratual mensal; não representa pagamentos efetivamente recebidos.

## Observação
A etapa não implementa gateway de pagamento, cobrança automática ou conciliação financeira. Esses pontos devem ser tratados em uma etapa futura de faturamento.
