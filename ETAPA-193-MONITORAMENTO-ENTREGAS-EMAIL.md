# Etapa 193 — Monitoramento de entregas de e-mail

## Entregas
- Indicadores globais de e-mails pendentes, enviados e em falha final.
- Filtro por status na rota `/admin/observabilidade/emails`.
- Ação POST para reprocessar uma entrega com status `FALHA_FINAL`.
- Reenvio reinicia o contador de tentativas e coloca a mensagem na fila para o próximo ciclo do processador.
- O botão de reenvio só aparece para falhas finais; mensagens enviadas ou pendentes não podem ser reenfileiradas por essa ação.

## Validação manual
1. Configure PostgreSQL e SMTP e inicie a aplicação.
2. Acesse `/admin/observabilidade/emails` com uma conta autorizada de administrador MASTER.
3. Confira os indicadores e aplique os filtros de status.
4. Para testar reenvio, provoque falhas SMTP até uma entrega ficar em `FALHA_FINAL`.
5. Clique em **Reenviar**. A entrega deve voltar a `PENDENTE`, com tentativas zeradas, e o processador agendado fará nova tentativa.

## Limitações
- Os indicadores são totais globais; a lista mostra no máximo 100 entregas recentes antes da aplicação do filtro.
- Não há paginação nesta etapa.
- A operação é segura por POST e deve continuar protegida pelo Spring Security/CSRF.
- Não foi possível confirmar compilação ou execução com banco e SMTP neste ambiente.
