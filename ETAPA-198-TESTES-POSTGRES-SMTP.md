# Etapa 198 — Testes de integração PostgreSQL e SMTP

## Objetivo
Adicionar testes de integração opcionais para validar a reivindicação atômica da fila em PostgreSQL real e o envio de mensagens para um SMTP simulado, sem disparar e-mails reais.

## Arquivos
- `EntregaAlertaEmailPostgresIT`: executa PostgreSQL 16 em Testcontainers/Docker e testa a reivindicação condicional da entrega.
- `EmailNotificacaoGreenMailIT`: executa GreenMail local e verifica destinatário e assunto enviados por `EmailNotificacaoService`.
- Dependências de teste Testcontainers PostgreSQL/JUnit e GreenMail adicionadas ao `pom.xml`.

## Execução
Os testes terminam em `IT` e não são incluídos pelo padrão do Surefire em todas as configurações. Execute explicitamente:

```bash
mvn -Dtest=EntregaAlertaEmailPostgresIT,EmailNotificacaoGreenMailIT test
```

O teste PostgreSQL exige Docker em execução. Quando Docker não estiver disponível, Testcontainers ignora esse teste por configuração. O teste GreenMail usa porta SMTP local e não necessita de conta ou credenciais externas.

## Limites
Estes testes cobrem uma reivindicação repetida no PostgreSQL e o envio SMTP simulado. Não comprovam por si só a ausência de duplicidade em uma implantação distribuída, nem o cenário de queda após o servidor SMTP aceitar a mensagem e antes da confirmação no banco. Executar no ambiente alvo antes da publicação.
