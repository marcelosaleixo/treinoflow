# TreinoFlow — Etapa 14: compartilhamento do treino com o aluno

## Implementado
- Botão para copiar o link público do treino liberado.
- Compartilhamento pelo WhatsApp usando o link público e uma mensagem pronta.
- Feedback visual de sucesso/erro na cópia.
- Os controles só aparecem quando o treino está liberado e preservam o fluxo de revogação/expiração existente.
- O compartilhamento abre o WhatsApp em nova aba; o sistema não envia mensagens automaticamente nem precisa armazenar o telefone do aluno.

## Como testar
1. Entre no TreinoFlow e abra um treino com exercícios.
2. Libere o treino para gerar o link público.
3. Clique em **Copiar link** e cole em um bloco de notas ou conversa de teste.
4. Clique em **Compartilhar pelo WhatsApp** e confirme que a mensagem contém o endereço público correto.
5. Revogue o acesso e confirme que o link antigo deixa de permitir visualizar o treino.

## Observações
- O link usa o domínio atual da aplicação. Em produção, configure HTTPS e o domínio definitivo antes de compartilhar.
- A compilação Maven e o teste integrado precisam ser executados no ambiente local com PostgreSQL.
