# TreinoFlow — Etapa 4: autenticação e injeção de dependências

## Implementado
- Cadastro e login de personal com Spring Security e Thymeleaf.
- Senhas armazenadas com BCrypt; nunca em texto puro.
- `UserDetailsService` carrega conta pelo e-mail e impede autenticação de conta inativa.
- Rotas protegidas por padrão; login, cadastro e recursos estáticos públicos.
- Logout via POST, com CSRF do Spring Security mantido habilitado.
- Injeção de dependências por construtor nas classes de serviço, segurança e controller.
- Formulários responsivos e validação básica de campos.
- Corrigida a expressão regular de validação de e-mail no `UsuarioPersonalService`.

## Como testar
1. Configure PostgreSQL e as variáveis conforme `.env.example`/`application.properties`.
2. Execute `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
3. Acesse `/cadastro`, crie um personal e entre em `/login`.
4. Teste `/dashboard` sem autenticação (deve redirecionar para login) e depois autenticado.

## Observações
- Não foi implementado fluxo de recuperação de senha, verificação de e-mail, limitação de tentativas ou CRUD visual de alunos/treinos.
- Build e integração com PostgreSQL precisam ser executados no ambiente do projeto; não declarar validação de runtime sem esses testes.
- Para ambientes públicos, configure HTTPS, política de senha e proteção contra abuso antes de disponibilizar o cadastro.
