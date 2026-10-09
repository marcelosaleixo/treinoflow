# V97 — Correção da exclusão de alunos com avaliações físicas

## Causa
A tabela `avaliacoes_fisicas` contém FK obrigatória para `alunos`. A exclusão física de um aluno referenciado viola a restrição `fkivmxxm2ocvr27js608xlu2o3d` do PostgreSQL.

## Correção
O método `AlunoService.excluir` agora realiza inativação lógica (`status = INATIVO`) e salva o aluno, preservando avaliações físicas e demais dados históricos. A mensagem da interface foi ajustada para informar a inativação. Não é necessária migração de banco.

## Validação pendente
O ZIP foi verificado quanto à integridade. Execute `\.\mvnw.cmd clean compile` e teste a inativação em ambiente local. A compilação Maven não foi executada neste ambiente.
