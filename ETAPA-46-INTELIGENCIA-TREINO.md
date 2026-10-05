# ETAPA 46 — Inteligência de Treino e Recomendações

## Objetivo
Transformar os indicadores da Etapa 45 em ações práticas para o Personal, sem inventar dados e sem criar dependência de nova tabela.

## Implementado
- `RecomendacaoPerformanceView` para representar alertas e ações.
- `InteligenciaTreinoService` com regras simples e explicáveis.
- Integração das recomendações ao `DashboardPerformanceService`.
- Nova seção "Inteligência de treino" no dashboard `/performance`.
- Recomendações para retenção, frequência, adesão, satisfação e oportunidade comercial.
- Prioridades ALTA, MEDIA e BAIXA.
- Layout responsivo para desktop e celular.

## Regras
- Aluno sem treino há 30 dias -> retenção / prioridade alta.
- Aluno sem treino na semana -> acompanhamento / prioridade alta.
- Menos de 5 sessões em 30 dias -> revisão de frequência / média.
- Média de nota abaixo de 3,5 -> investigar satisfação / média.
- Carteira sem sessão nos últimos 7 dias -> contato ativo / média.
- Média geral igual ou superior a 4,5 -> oportunidade de prova social / baixa.
- Sem alertas -> mensagem de carteira saudável.

## Segurança
As recomendações usam apenas os dados já filtrados pelo Personal autenticado. Não há exposição de dados de outro Personal.

## Banco de dados
Nenhuma nova tabela ou migration é necessária nesta etapa.
