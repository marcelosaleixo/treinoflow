# ETAPA 107 — Ação Inteligente Executável

## Objetivo
Transformar a recomendação da Etapa 106 em uma ação operacional que o Personal consegue executar e registrar no CRM sem sair da Central de Plano Inteligente.

## Entregas
- Botão para copiar a mensagem sugerida pelo TreinoFlow.
- Acesso direto ao WhatsApp quando houver telefone.
- Bloco expansível "Registrar ação executada no CRM" por aluno.
- Seleção de canal, resultado e próxima ação.
- Campo opcional para registrar o que aconteceu no contato.
- Novo POST `/performance/plano-inteligente/{alunoId}/executar`.
- Validação de carteira pelo Personal autenticado antes do registro.
- Registro reutilizando `CrmService` e `InteracaoCrm` existentes.
- Tipo CRM definido como RETENCAO para prioridades ATENÇÃO/ALTA e CONTATO para demais prioridades.
- Feedback visual de sucesso/erro após o registro.

## Fluxo
1. TreinoFlow identifica um aluno que precisa de ação.
2. Personal copia ou abre a mensagem sugerida.
3. Personal realiza o contato pelo canal escolhido.
4. Personal registra resultado e próxima ação.
5. TreinoFlow grava a interação na timeline do CRM.

## Segurança
- Não há envio automático de mensagens.
- A ação só é registrada após submissão explícita do Personal.
- O aluno é recuperado a partir da carteira do Personal autenticado.
- Nenhuma carga, exercício ou prescrição é alterada automaticamente.
- A recomendação de IA permanece como apoio operacional, não como diagnóstico ou decisão clínica.

## Banco
Nenhuma tabela ou migration nova.

## Validação
- ZIP íntegro.
- Verificação estrutural de controller/template realizada.
- Build Maven não confirmado neste ambiente porque o Maven Wrapper não conseguiu baixar o Maven 3.9.16 do Maven Central.
