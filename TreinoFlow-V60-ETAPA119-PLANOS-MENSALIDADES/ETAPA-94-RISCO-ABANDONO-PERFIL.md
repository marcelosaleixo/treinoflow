# Etapa 94 — Risco de Abandono no Perfil Inteligente

## Objetivo
Integrar o score operacional de risco de abandono ao Perfil Inteligente do aluno, evitando criar um segundo motor de risco.

## Implementação
- `PerfilInteligenteAlunoService` reutiliza `ScoreRiscoAlunoService`.
- `PerfilInteligenteAlunoView` passou a expor score, nível, ação recomendada e sinais de risco.
- A tela `aluno/perfil-inteligente.html` ganhou o painel **Risco de abandono**.
- O indicador usa as regras existentes de retenção: faltas, queda de frequência, inatividade, satisfação, contato e queda de carga.
- Nenhuma nova tabela ou migração foi criada.

## Regra de segurança
O score é um indicador operacional, não um diagnóstico. A decisão de contato ou mudança de treino continua sendo do Personal.
