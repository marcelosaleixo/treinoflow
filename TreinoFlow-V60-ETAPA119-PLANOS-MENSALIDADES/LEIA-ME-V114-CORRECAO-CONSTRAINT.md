# V114 — Correção efetiva da constraint no PostgreSQL

O log continua mostrando a mesma constraint `notificacoes_treinoflow_tipo_check` após a versão anterior. A causa provável é que a migration SQL não foi executada no banco: o projeto não possui dependência/configuração Flyway no `pom.xml`, então colocar o arquivo na pasta de migrations não garante sua execução.

## Passos obrigatórios
1. Faça backup do banco.
2. Abra o banco PostgreSQL que a aplicação realmente usa (confira `DB_URL` no EasyPanel/ambiente).
3. Execute o arquivo `CORRIGIR-CONSTRAINT-NOTIFICACOES.sql` no banco.
4. Reinicie a aplicação e abra novamente a tela de notificações.

O SQL remove somente a constraint de tipos antiga e a recria com todos os tipos declarados no enum atual. Ele não apaga registros e não desativa a validação.

Se o SQL falhar por causa de dados antigos que já contenham tipos fora da lista, não remova a validação permanentemente; confira os tipos existentes antes de corrigir.
