# Etapa 161 — Importação CSV de alunos

## Objetivo
Permitir o cadastro em lote de alunos por arquivo CSV, aproveitando a exportação já disponível e mantendo o isolamento por personal.

## Implementação
- Nova tela `/alunos/importacao` e endpoint POST para receber CSV UTF-8 separado por ponto e vírgula.
- Limite de arquivo de 5 MB e validação de extensão/cabeçalho.
- Mapeamento dos campos Nome, CPF, Sexo, E-mail, Telefone, Objetivo, Status, Data de nascimento e Data de início.
- Reutiliza `AlunoService.salvar`, preservando as regras de CPF, datas, status e limite do plano.
- Ignora CPF já cadastrado para o personal e apresenta o total importado, ignorado e até 20 mensagens de erro.
- Botão de acesso na lista de alunos.

## Limites
- A importação cria alunos novos; não atualiza registros existentes.
- Datas devem estar no formato ISO `AAAA-MM-DD`.
- Registros que excedam o limite do plano não são importados.

## Validação
ZIP integrity deve ser conferida. Executar `./mvnw clean compile` e testes manuais com CSV válido, CPF duplicado, datas inválidas e limite do plano antes de publicar.
