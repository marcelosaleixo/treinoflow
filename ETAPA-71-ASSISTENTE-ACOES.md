# ETAPA 71 — Assistente de Ações do Personal

## Objetivo
Transformar os sinais do Motor de Risco em uma próxima ação executável, reunindo prioridade, motivo, mensagem sugerida e atalhos para WhatsApp, follow-up e CRM.

## Nova rota
- `GET /assistente/acoes`
- `POST /assistente/acoes/{alunoId}/whatsapp`
- `POST /assistente/acoes/{alunoId}/follow-up`

## Regras
- Reutiliza `ScoreRiscoAlunoService` e `RetencaoInteligenteService`.
- Considera alunos com score >= 25.
- Ordena pelo maior score e, em empate, por maior tempo sem contato.
- Prioridade CRÍTICA: score >= 75.
- Prioridade ALTA: score >= 50.
- Prioridade MÉDIA: demais casos elegíveis.
- A mensagem é gerada pelo mesmo motor de retenção já existente.
- O envio de WhatsApp e a criação de follow-up reutilizam os fluxos existentes, preservando o registro no CRM.

## Banco de dados
Nenhuma tabela ou migration nova.

## Arquivos
- `AcaoAssistenteView.java`
- `AssistenteAcoesService.java`
- `AssistenteAcoesController.java`
- `templates/assistente/acoes.html`

## Princípio
A etapa não substitui os módulos de CRM ou retenção. Ela funciona como uma camada de execução sobre as regras existentes, evitando duplicação de lógica.
