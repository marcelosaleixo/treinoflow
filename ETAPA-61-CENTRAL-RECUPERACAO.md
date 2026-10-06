# ETAPA 61 — Central de Recuperação Inteligente

## Objetivo
Transformar o Score de Risco da Etapa 60 em uma fila operacional de retenção.

## Funcionalidades
- Nova rota `/retencao/central`.
- Lista alunos com score a partir de 25.
- Exibe sinais que justificam o risco e a ação recomendada.
- Permite criar follow-up interno no CRM.
- Permite enviar WhatsApp pela integração já existente.
- Após envio do WhatsApp, registra automaticamente a interação no CRM como `WHATSAPP / RETENCAO / EM_ACOMPANHAMENTO`.
- Define próxima ação para dois dias após o contato.
- Mantém o isolamento pelo Personal autenticado.
- Não cria nova tabela ou migration.

## Fluxo
Score de risco → Central de recuperação → WhatsApp ou Follow-up → CRM → próxima ação.

## Observação
O envio real depende da configuração existente da integração WhatsApp. Quando a integração estiver desativada ou incompleta, a aplicação retorna uma mensagem de erro e não registra o envio como realizado.
