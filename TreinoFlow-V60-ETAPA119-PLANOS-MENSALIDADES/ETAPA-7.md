# Etapa 7 — Área pública do aluno

## Entregas
- Página pública responsiva em `/a/{token}` para visualizar um treino liberado.
- Apresenta nome do aluno e personal, descrição, data de liberação, exercícios, séries, repetições, carga, descanso, observações e link de vídeo.
- Token inválido, inexistente ou associado a treino não liberado apresenta uma tela de indisponibilidade.
- Acesso público limitado à leitura; nenhum endpoint de alteração foi exposto.
- Dados convertidos para DTO dentro de transação read-only para evitar dependência de lazy loading durante a renderização.
- Injeção de dependências por atributo com `@Autowired`, conforme solicitado.

## Rota
`GET /a/{token}` — não exige login. Compartilhe o endereço completo `https://seu-dominio/a/{token}` com o aluno.

## Observação de segurança
O token funciona como credencial de acesso: compartilhe apenas com o aluno. Nesta etapa não há expiração, revogação pelo personal nem registro de visualizações; esses recursos podem ser adicionados posteriormente.

## Teste manual
1. Entre como personal, crie um treino e adicione pelo menos um exercício.
2. Libere o treino e copie o token exibido no editor.
3. Abra `/a/{token}` em janela anônima; a página deve abrir sem autenticação.
4. Teste token inválido e confirme a tela de indisponibilidade.
