# ETAPA 34 — Controle de acesso baseado na assinatura

## Objetivo
Aplicar no acesso do Personal as regras comerciais definidas na assinatura.

## Regras
- MASTER não sofre bloqueio por assinatura.
- ATIVA: acesso normal.
- EM_TESTE: acesso normal.
- INADIMPLENTE: acesso bloqueado.
- VENCIDA: acesso bloqueado.
- SUSPENSA: acesso bloqueado.
- CANCELADA: acesso bloqueado.
- Assinatura inexistente: acesso bloqueado.
- Uma assinatura ATIVA/EM_TESTE com vencimento anterior a hoje passa para VENCIDA.

## Fluxo
Login -> tentativa de acessar o sistema -> validação da assinatura -> acesso normal ou página de acesso limitado.

## Rotas preservadas
`/assinatura` e `/assinatura/bloqueada` permanecem acessíveis para consulta da situação e orientação ao Personal.

## Arquivos principais
- `AssinaturaAcessoInterceptor`
- `WebMvcConfig`
- `AssinaturaController`
- `AssinaturaService`
- `templates/assinatura/status.html`
- `templates/assinatura/bloqueada.html`
