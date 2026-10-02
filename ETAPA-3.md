# TreinoFlow — Etapa 3: regras de negócio e isolamento por personal

## O que foi implementado
- Consultas e operações de alunos vinculadas ao `personalId`.
- Treinos só podem ser consultados/editados pelo personal proprietário do aluno.
- Exercícios globais podem ser usados por qualquer personal; exercícios particulares ficam vinculados ao criador.
- Validações básicas de nome, status, séries, ordem e descanso.
- Liberação de treino gera token UUID e registra data/hora; treino liberado fica bloqueado para edição/exclusão nesta etapa.
- Validação de e-mail e prevenção de cadastro duplicado de personal.
- Corrigida a declaração do `UsuarioPersonalRepository`.

## Atenção para a próxima etapa
Ainda não há autenticação nem controllers. Os métodos recebem `personalId` explicitamente para estabelecer as regras do domínio. Quando a segurança for implementada, o ID deverá ser obtido do usuário autenticado no servidor — nunca confiado diretamente a um parâmetro enviado pelo navegador.

## Validação
A compilação Maven não foi concluída porque o Maven Wrapper não conseguiu baixar a distribuição do Maven (falha de acesso ao repositório). Portanto, este pacote ainda precisa ser compilado/testado em ambiente com Maven e dependências disponíveis.
