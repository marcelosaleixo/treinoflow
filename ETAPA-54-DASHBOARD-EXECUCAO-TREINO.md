# ETAPA 54 — Dashboard de Execução do Treino

## Objetivo
Transformar os dados já registrados pelo checklist da Etapa 53 em um painel operacional para o Personal acompanhar o que foi prescrito e o que realmente foi executado.

## O que foi implementado
- Nova rota: `/performance/execucao`.
- Indicadores dos últimos 30 dias:
  - sessões concluídas;
  - exercícios prescritos;
  - exercícios realizados;
  - taxa de conclusão prescrito × realizado;
  - média das notas dos treinos;
  - observações registradas nos exercícios.
- Visão por aluno:
  - sessões;
  - itens prescritos;
  - itens concluídos;
  - taxa de conclusão;
  - último treino;
  - média das notas.
- Visão por exercício:
  - prescrições e execuções;
  - taxa de conclusão;
  - última carga informada;
  - últimas repetições informadas;
  - quantidade de observações.
- Isolamento por Personal usando `personalId` nas consultas.
- Layout responsivo para desktop e celular.
- Nenhuma nova tabela foi criada: a funcionalidade reutiliza `registros_treino_aluno`, `treino_exercicios` e `execucoes_exercicios` existentes.

## Regra principal
A taxa de conclusão compara os itens de exercícios prescritos nas sessões concluídas com os itens marcados como concluídos no checklist do aluno.

## Arquivos principais
- `ExecucaoTreinoDashboardView.java`
- `ExecucaoTreinoDashboardService.java`
- `ExecucaoTreinoController.java`
- `performance/execucao.html`
- `RegistroTreinoAlunoRepository.java`
- `TreinoExercicioRepository.java`
- `ExecucaoExercicioRepository.java`

## Observação de validação
Foi feita validação estrutural dos arquivos e das referências entre controller, service, DTO, repositories e template. O build Maven completo depende do ambiente possuir acesso/artefatos do Maven Wrapper.
