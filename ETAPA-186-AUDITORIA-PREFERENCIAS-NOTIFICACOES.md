# TreinoFlow — Etapa 186: Auditoria de preferências de notificações

## Objetivo
Registrar mudanças reais feitas pelo Personal nas preferências de notificações em tempo real, permitindo consulta paginada e mantendo o isolamento por usuário autenticado.

## Implementação
- Entidade `PreferenciaNotificacaoAuditoria` e tabela `auditoria_preferencias_notificacao`.
- Repositório com consulta restrita ao `personalId`, ordenada da alteração mais recente para a mais antiga.
- Serviço de auditoria registra categoria, valor anterior, valor novo e data/hora.
- A auditoria é gravada na mesma transação que salva as preferências; falha ao gravar a auditoria também impede salvar parcialmente as preferências.
- Só registra quando o valor efetivamente muda. Preferências ainda não cadastradas são tratadas como ativadas por padrão.
- Nova tela `/notificacoes/preferencias/historico`, com paginação de 25 itens e sem exposição de dados de outros Personais.
- Link do histórico disponível na tela de preferências.
- Migração de referência: `src/main/resources/db/migration/V186__auditoria_preferencias_notificacao.sql`.

## Arquivos principais
- `entity/PreferenciaNotificacaoAuditoria.java`
- `repository/PreferenciaNotificacaoAuditoriaRepository.java`
- `service/PreferenciaNotificacaoAuditoriaService.java`
- `service/PreferenciaNotificacaoService.java`
- `controller/NotificacaoController.java`
- `templates/notificacoes/preferencias-historico.html`

## Testes manuais recomendados
1. Autenticar como Personal A, mudar uma categoria de ativada para desativada e salvar.
2. Abrir `/notificacoes/preferencias/historico` e confirmar categoria, valor anterior, valor novo e data/hora.
3. Salvar sem alterar os valores e confirmar que não há novos registros redundantes.
4. Alterar outra categoria e conferir a ordenação mais recente primeiro.
5. Criar mais de 25 alterações para verificar a paginação.
6. Autenticar como Personal B e confirmar que não vê o histórico do Personal A.
7. Executar `mvnw.cmd test` no Windows ou `./mvnw test` no Linux e validar a aplicação com o PostgreSQL de homologação.

## Banco de dados
A entidade JPA permite criação/atualização em ambientes configurados com `spring.jpa.hibernate.ddl-auto=update`. Para ambientes com migrações controladas, revisar e aplicar o SQL V186 conforme o procedimento de implantação do projeto. Não executar automaticamente scripts manuais fora do fluxo de migração utilizado pelo ambiente.

## Limitações de validação
A implementação foi revisada estaticamente. A compilação Maven e os testes de integração precisam ser executados no ambiente com a distribuição Maven/dependências disponíveis antes de publicar em produção.
