# Correção definitiva - Ambiguous mapping /assistente/aprendizado

## Causa raiz

O código-fonte do projeto corrigido possui apenas um controller com o endpoint:

`GET /assistente/aprendizado`

`AprendizadoRetencaoController#index`.

O erro que continua aparecendo em uma instalação anterior indica que o Spring está carregando também uma classe compilada antiga:

`AprendizadoAcoesAssistenteController.class`

Isso normalmente acontece quando o `target/classes` não foi limpo depois da remoção/alteração do controller.

## Correção

1. Feche o Spring Boot/IDE.
2. Apague a pasta `target`.
3. Execute `mvnw.cmd clean package -DskipTests`.
4. Execute `mvnw.cmd spring-boot:run`.

No Windows, basta executar `LIMPAR-E-EXECUTAR.bat`.

## Conferência

Depois do build, deve existir apenas:

`target/classes/com/marceloaleixo/treinoflow/controller/AprendizadoRetencaoController.class`

para os controllers relacionados ao aprendizado. A classe antiga `AprendizadoAcoesAssistenteController.class` não deve existir.

O endpoint `/assistente/aprendizado` continua reservado ao painel da Etapa 80.
