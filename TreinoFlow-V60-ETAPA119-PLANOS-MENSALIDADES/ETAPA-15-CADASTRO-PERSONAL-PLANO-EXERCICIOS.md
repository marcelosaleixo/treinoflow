# TreinoFlow — Etapa 15: cadastro de personal com plano automático + biblioteca inicial de exercícios

## Implementado

### Cadastro de personal
- O plano padrão é pré-selecionado no cadastro de novos personais.
- Se o Master não informar `planoId`, o backend aplica o plano padrão automaticamente.
- Não é permitido salvar um novo personal sem um plano ativo.
- Personais antigos com `plano_id` nulo recebem o plano padrão no startup, exceto o usuário MASTER.
- O plano selecionado passa a valer imediatamente para o limite de alunos.

### Gestão de planos
- Adicionado conceito de **Plano padrão**.
- Apenas um plano ativo pode ser padrão.
- O último plano padrão não pode ser desativado sem que outro plano ativo assuma o lugar.
- O formulário de plano permite marcar/desmarcar o plano padrão.
- Se o banco estiver sem planos, o sistema cria Start, Pro e Premium.

### Biblioteca inicial de exercícios
- O sistema passa a cadastrar exercícios globais automaticamente no primeiro startup.
- Exercícios globais ficam com `personal_id = NULL` e aparecem para todos os personais.
- A carga é idempotente: exercícios existentes não são duplicados.
- Foram incluídos exercícios de peito, costas, ombros, braços, quadríceps, posteriores, glúteos, adutores, abdutores, panturrilhas, abdômen, lombar e corpo todo.

## Regras
- Exercícios globais podem ser usados por todos os personais.
- Exercícios criados pelo personal continuam sendo particulares daquele personal.
- O plano padrão é usado apenas para personais; o MASTER continua sem plano comercial.
