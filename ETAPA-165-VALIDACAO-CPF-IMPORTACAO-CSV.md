# Etapa 165 — Validação de CPF na importação CSV

## Alterações
- Valida CPF com 11 dígitos e dígitos verificadores oficiais durante a validação prévia e novamente na importação definitiva.
- Rejeita sequências repetidas, como `11111111111`.
- CPF continua opcional quando o campo está vazio.
- Normaliza CPF para somente dígitos antes de persistir, reduzindo divergências de formatação em verificações de duplicidade.
- Atualiza o texto da tela para explicar a validação.

## Testes manuais sugeridos
1. Validar um CSV com CPF válido, como `529.982.247-25`.
2. Validar um CPF inválido, como `111.111.111-11`, e confirmar que é apontado como erro.
3. Importar CPF válido formatado e verificar que o banco recebe somente dígitos.
4. Confirmar que CPF vazio continua permitido, se os campos obrigatórios do cadastro também permitirem.

A validação do ZIP não equivale à compilação Maven nem a testes integrados; execute-os no ambiente do projeto antes da publicação.
