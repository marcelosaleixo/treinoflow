# TreinoFlow — Etapa 168 (V127)

## Validação estrutural do CSV

- Detecta aspas não fechadas em linhas do CSV.
- Compara a quantidade de campos de cada registro com a quantidade de colunas do cabeçalho.
- Na validação prévia, registra a linha e o motivo sem interromper a análise do arquivo inteiro.
- Na importação definitiva, rejeita a linha estruturalmente inválida e inclui o motivo no relatório de erros.
- Mantém as validações existentes de CPF, e-mail, status, sexo, limites de campos e datas.

## Testes recomendados

1. Validar um CSV correto com 9 colunas.
2. Retirar um campo de uma linha e confirmar o erro de quantidade de colunas.
3. Acrescentar um campo a uma linha e confirmar o erro.
4. Abrir aspas sem fechá-las e confirmar o erro estrutural.
5. Confirmar que linhas válidas do mesmo arquivo continuam sendo analisadas/importadas.

ZIP verificado quanto à integridade. Compilação Maven e testes integrados não executados neste ambiente.
