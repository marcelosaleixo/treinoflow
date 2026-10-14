# Etapa 169 — Detecção de e-mails duplicados na importação CSV

## Objetivo
Evitar que a importação prévia aceite ou importe registros com e-mails repetidos, tanto dentro do mesmo CSV quanto entre o arquivo e os alunos já cadastrados para o personal autenticado.

## Alterações
- `AlunoRepository`: consulta `existsByPersonalIdAndEmailIgnoreCase` para verificar e-mails já cadastrados no escopo do personal.
- Validação prévia: normaliza e-mail removendo espaços externos e comparando sem diferenciar maiúsculas/minúsculas; informa duplicidade dentro do arquivo e cadastro existente.
- Importação definitiva: repete as verificações antes de salvar, registra a linha e o motivo no relatório de erros e ignora o registro duplicado.
- Alterações aplicadas às duas cópias do projeto incluídas no ZIP.

## Testes manuais recomendados
1. CSV com dois alunos usando `teste@exemplo.com` e `TESTE@EXEMPLO.COM`: a segunda ocorrência deve ser sinalizada.
2. CSV com e-mail já cadastrado para o mesmo personal: deve ser sinalizado na validação e ignorado na importação.
3. Mesmo e-mail cadastrado por outro personal: a consulta é restrita ao personal atual.
4. CSV sem e-mail ou com e-mails distintos: não deve ser bloqueado por duplicidade.

A integridade do arquivo ZIP foi verificada. A compilação Maven e os testes de integração não foram executados neste ambiente.
