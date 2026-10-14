# Etapa 153 — Formatação de CPF no cadastro e edição de aluno

## Implementado
- Máscara progressiva `000.000.000-00` durante a digitação.
- Limite de 11 dígitos numéricos, ignorando pontuação colada ou digitada.
- Formatação automática do CPF existente ao abrir a edição.
- Validação de quantidade de dígitos no navegador e validação definitiva dos dígitos verificadores no backend.
- Campo permanece opcional.

## Observações
O backend continua armazenando somente os 11 dígitos, conforme a etapa anterior. A máscara do navegador é apenas usabilidade; não substitui validação do servidor.

## Validação sugerida
Testar CPF digitado, colado com pontuação, colado sem pontuação, CPF parcial, edição de CPF já salvo e envio com campo vazio. Executar `mvnw.cmd clean compile` e os testes existentes no ambiente de desenvolvimento.
