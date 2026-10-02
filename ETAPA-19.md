# Etapa 19 — Busca nas listagens

## Implementado
- Busca de alunos por nome, e-mail ou telefone, respeitando o personal autenticado.
- Busca de exercícios pelo nome, incluindo exercícios próprios e globais disponíveis para o personal.
- A busca utiliza paginação no banco e preserva o termo e o tamanho da página ao navegar.
- Botão para limpar o filtro e estado vazio orientativo.

## Como testar
1. Execute a aplicação e autentique-se como personal.
2. Em **Alunos**, pesquise parte do nome, e-mail ou telefone.
3. Em **Exercícios**, pesquise parte do nome de um exercício.
4. Navegue entre páginas e altere o tamanho da página; o filtro deve permanecer aplicado.
5. Use **Limpar** para retornar à listagem completa.

## Validação
A compilação Maven não foi executada neste ambiente porque o comando `mvn` não está instalado. Execute `mvnw.cmd clean test` no Windows ou `./mvnw clean test` em ambiente Unix, se o wrapper estiver incluído.
