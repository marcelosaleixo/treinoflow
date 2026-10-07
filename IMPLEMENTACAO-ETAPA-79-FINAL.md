# TreinoFlow — Etapa 79 Final

## Fluxo implementado

`Score de risco -> melhor ação -> melhor horário -> mensagem inteligente -> execução -> jornada -> resultado -> aprendizado`

### Alterações
- `MensagemInteligenteService` passa a ser usada pela automação real.
- Mensagens são escolhidas por faixa de risco + tipo de ação, usando somente resultados `RECUPERADO`/`RENOVADO` e mínimo de 3 amostras.
- O melhor horário é aprendido por faixa de risco com mínimo de 3 resultados finais.
- Somente a rotina agendada respeita o melhor horário aprendido; a execução manual continua imediata.
- WhatsApp recebe a mensagem inteligente efetivamente selecionada.
- Follow-up registra a mensagem sugerida no CRM e na ação assistente.
- A ação criada pela automação é marcada como automática de forma determinística, sem buscar uma ação recente por janela de tempo.
- A jornada automática é iniciada diretamente na ação criada.
- Segundo e último contatos da jornada usam a inteligência de mensagens disponível.
- Não foram adicionadas tabelas ou colunas novas nesta etapa.

## Validação

Os cinco serviços Java alterados foram compilados com `javac` contra as classes compiladas e as dependências existentes no JAR do V17.

O build Maven completo não foi executado porque este ambiente não consegue acessar `repo.maven.apache.org` para baixar o Maven 3.9.16 exigido pelo wrapper.

Ao substituir o projeto, execute no Windows:

```bat
mvnw.cmd clean package
```

ou, se o Maven estiver instalado:

```bat
mvn clean package
```
