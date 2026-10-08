# ETAPA 98 — Registro da Ação de Retenção

## Objetivo
Fechar o ciclo entre o alerta **Atenção hoje** e o registro operacional da ação realizada pelo Personal.

## Implementação
- O alerta do dashboard agora abre diretamente a ficha CRM com uma ação de retenção pré-preenchida.
- O aluno continua sendo validado pelo `personalId`, preservando isolamento entre personais.
- Tipo: `RETENCAO`.
- Resultado inicial: `EM_ACOMPANHAMENTO`.
- Canal sugerido: WhatsApp quando há telefone; caso contrário, Telefone.
- Assunto, motivo do alerta e orientação de registro são pré-preenchidos.
- Próxima ação sugerida para 3 dias, podendo ser alterada antes do salvamento.
- O Personal precisa revisar e salvar explicitamente; nenhuma mensagem é enviada automaticamente.
- A timeline do CRM continua sendo a fonte do histórico da ação.

## Segurança
Não foi criada rota que aceite `personalId` do navegador. O aluno é recuperado pelo vínculo autenticado do Personal.

## Banco
Nenhuma nova tabela ou coluna. A funcionalidade reutiliza `InteracaoCrm`.
