# ETAPA 80 — IA de Aprendizado Contínuo da Retenção

## Objetivo

Fechar o ciclo iniciado nas Etapas 78 e 79 fazendo o motor aprender continuamente com o resultado real das mensagens.

### Fluxo

Risco → ação → horário → mensagem → execução → resultado → aprendizado → próxima decisão.

## O que mudou

- A escolha de mensagem deixou de privilegiar simplesmente a maior quantidade de ocorrências.
- O motor calcula taxa de sucesso por modelo normalizado de mensagem.
- Exige no mínimo 3 resultados vencedores.
- Usa suavização de Laplace para evitar 100% artificial em amostras pequenas.
- Usa bônus UCB de exploração controlada para evitar que um modelo antigo domine indefinidamente.
- Mantém isolamento por `personalId`.
- Mantém janela de 180 dias.
- Continua sem dependência de IA externa.
- Fallback permanece disponível quando não existe amostra mínima.

## Nova tela

`/assistente/aprendizado`

Exibe por faixa de risco:
- modelo vencedor;
- amostra;
- sucessos;
- taxa de sucesso;
- confiança.

## Banco

Nenhuma coluna nova é necessária. A migration `V49__aprendizado_continuo_retencao.sql` marca a etapa sem alterar a estrutura existente.

## Segurança

A Etapa 80 não envia mensagens por conta própria. Ela apenas fornece a decisão estatística ao `MensagemInteligenteService`, que continua sujeito às proteções da automação: score mínimo, cooldown, limite diário, janela de execução, follow-up pendente e WhatsApp configurado.
