# TreinoFlow — Etapa 8: Controle de acesso público

## Implementado
- Expiração automática do link público após 30 dias da liberação.
- Validação de expiração ao acessar `/a/{token}`.
- Revogação manual pelo personal, invalidando o token atual e retornando o treino a rascunho.
- Nova liberação gera token novo e validade renovada.
- Registro da quantidade de visualizações e data/hora da última visualização bem-sucedida.
- Editor exibe token, link público, expiração, visualizações e última visualização.
- Página do aluno informa até quando o acesso é válido.
- Injeção de dependências mantida com `@Autowired` por atributo.

## Persistência
As propriedades novas da entidade `Treino` são `acessoExpiraEm`, `totalVisualizacoes` e `dataUltimaVisualizacao`. Com `spring.jpa.hibernate.ddl-auto=update`, o Hibernate deverá atualizar o esquema local. Para produção, recomenda-se criar migração versionada (Flyway/Liquibase) antes de aplicar alterações de banco.

## Regras
- Validade padrão: 30 dias.
- Faixa aceita pelo serviço: 1 a 365 dias (o fluxo atual utiliza 30 dias).
- Link expirado não exibe o treino.
- Revogar invalida o token e permite liberar novamente.
- A contagem representa acessos bem-sucedidos à página pública, não usuários únicos.

## Validação
O build Maven não foi concluído: o Maven Wrapper falhou ao baixar a distribuição do Maven do repositório remoto. Compilação e teste integrado ainda precisam ser executados no ambiente do projeto.
