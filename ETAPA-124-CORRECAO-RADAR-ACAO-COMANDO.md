# ETAPA 124 — Correção RadarAcaoController / RadarAcaoService

## Problema
A chamada com muitos parâmetros entre `RadarAcaoController` e `RadarAcaoService` podia ficar desalinhada entre versões do projeto, causando erro de compilação na chamada `acoes.registrar(...)`.

## Correção
Foi criado o DTO `RadarAcaoRegistro`, usado como objeto de comando único entre Controller e Service.

O Controller agora monta o registro e chama:

```java
acoes.registrar(registro);
```

O Service extrai os dados do registro e mantém as mesmas regras de negócio, incluindo validação do resultado, próxima ação, propriedade do aluno, CRM e aprendizado do Radar.

## Preservado
- Etapa 110 — ação executável do Radar
- Etapa 111 — reavaliação pós-ação
- Etapa 112 — aprendizado das ações do Radar
- Etapa 121 — pagamentos divididos
- Etapa 122 — recorrência automática
- Etapa 123 — cobrança inteligente

Não há alteração de banco nesta correção.
