CORREÇÃO — COMPATIBILIDADE DO ENUM GRUPO MUSCULAR

Causa: havia exercícios persistidos com grupo_muscular = 'PERNA', valor ausente
do enum GrupoMuscular. O Hibernate falhava ao carregar esses registros.

Ajuste: incluída a constante PERNA("Perna") no enum, preservando os registros
existentes sem exigir alteração ou exclusão no banco de dados.
