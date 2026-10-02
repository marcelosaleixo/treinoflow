# Etapa 26 — Renovação de link expirado

- O editor identifica quando a validade do link público terminou.
- Links expirados não são apresentados como ativos nem oferecem ações de compartilhamento.
- O personal recebe aviso e pode gerar um novo link escolhendo a validade.
- A operação utiliza a rota e validações existentes de liberação; um novo token é emitido quando o anterior expirou.
- Sem alteração de banco de dados.

Teste manual: liberar um treino, aguardar/ajustar a expiração para o passado em ambiente de teste, abrir o editor e gerar novo link. Confirmar que o novo link abre e o token anterior não é aceito.
