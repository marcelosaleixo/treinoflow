# TreinoFlow — Etapa 167 (V126)

## Validação de limites dos campos na importação CSV

- Nome: máximo de 120 caracteres.
- E-mail: máximo de 150 caracteres e formato básico válido.
- Telefone: máximo de 20 caracteres.
- Objetivo: máximo de 100 caracteres.
- Data de nascimento e data de início não podem ser futuras.
- As mesmas regras são verificadas na validação prévia e na importação definitiva.

Os limites seguem as colunas declaradas na entidade `Aluno`. Não foi alterado o schema do banco.

## Testes recomendados

1. Validar/importar CSV com nome acima de 120 caracteres.
2. Repetir com e-mail acima de 150, telefone acima de 20 e objetivo acima de 100 caracteres.
3. Testar datas futuras nas colunas de nascimento e início.
4. Confirmar que linhas inválidas aparecem no relatório de erros e não são salvas.

Integridade do ZIP verificada. Compilação Maven e testes integrados não executados neste ambiente.
