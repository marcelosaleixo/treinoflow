# Etapa 79 — IA de Otimização → Mensagem Inteligente → Ação Automática

## Objetivo
Evoluir a automação de retenção para utilizar três sinais aprendidos no histórico do próprio personal:

1. melhor ação por faixa de risco;
2. melhor horário por faixa de risco;
3. melhor mensagem entre ações que terminaram em recuperação ou renovação.

A execução continua transparente, isolada por personal e sem dependência de serviço externo de IA.

## Mensagem inteligente
- Analisa os últimos 180 dias.
- Considera somente mensagens associadas a `RECUPERADO` ou `RENOVADO`.
- Separa por faixa `CRÍTICO`, `ALTO` e `MÉDIO` e pelo tipo de ação.
- Normaliza o primeiro nome do aluno para evitar que a comparação trate nomes diferentes como mensagens diferentes.
- Exige pelo menos 3 ocorrências do mesmo modelo normalizado.
- Confiança `MÉDIA` para 3–9 ocorrências e `ALTA` para 10+.
- Sem amostra mínima, usa fallback seguro e marca a confiança como `BAIXA`.

## Melhor horário aplicado
A automação horária respeita o melhor horário estatístico encontrado quando houver pelo menos 3 resultados finais na faixa de risco. Se o horário aprendido estiver fora da janela configurada pelo personal, a configuração de horário continua prevalecendo.

A execução manual mantém o comportamento de teste imediato e não precisa esperar o horário aprendido; somente a rotina agendada respeita o horário vencedor.

## Ação automática
A rotina mantém as proteções anteriores:
- automação precisa estar ativa;
- score mínimo;
- cooldown por aluno;
- ausência de follow-up pendente;
- limite diário;
- janela de execução;
- WhatsApp somente quando permitido e com telefone.

Quando executa:
- WhatsApp recebe a mensagem inteligente personalizada;
- Follow-up recebe a mensagem sugerida no CRM e na ação registrada;
- a ação é marcada como automática;
- a jornada automática é iniciada.

## Limites desta etapa
O histórico atual registra a jornada na mesma ação, portanto ainda não existe um histórico confiável por etapa da jornada para aprender separadamente a melhor mensagem do 1º, 2º e último contato. Por isso, a Etapa 79 aprende a mensagem por faixa de risco + tipo de ação, sem inventar aprendizado por etapa.

## Tela
Nova tela: `/assistente/mensagens`.
Ela apresenta a mensagem vencedora por faixa de risco, amostra e confiança.
