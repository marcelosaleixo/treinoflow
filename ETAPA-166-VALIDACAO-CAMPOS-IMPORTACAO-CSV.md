# Etapa 166 — Validação consistente dos campos na importação CSV

## Alterações
- A validação prévia e a importação definitiva validam os mesmos campos básicos.
- Sexo aceita vazio, `FEMININO`, `MASCULINO` ou `NAO_INFORMADO`; valores preenchidos são normalizados para maiúsculas.
- E-mail preenchido precisa ter formato básico válido e no máximo 254 caracteres.
- Status aceita apenas `ATIVO` ou `INATIVO`; vazio assume `ATIVO`.
- Erros da importação continuam identificados por linha no relatório CSV.

## Teste manual
1. Envie um CSV com e-mail inválido, sexo desconhecido e status fora da lista.
2. Confirme que a validação prévia aponta as linhas problemáticas.
3. Tente importar diretamente o mesmo arquivo sem validar antes; as linhas inválidas devem ser ignoradas e listadas no relatório.
4. Teste também campos opcionais vazios para confirmar que continuam permitidos.

A integridade do ZIP é verificada separadamente; a compilação Maven e os testes de integração devem ser executados no ambiente do projeto antes de produção.
