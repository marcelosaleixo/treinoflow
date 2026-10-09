# Etapa 136 — Indicadores de resultado das ações financeiras

- Nova rota: `/financeiro/resultado-acoes`.
- Reaproveita as interações existentes do CRM, sem criar entidade ou migração.
- Filtra por ano/mês e mostra contagens por resultado e tipo de interação.
- Exibe até 100 ações recentes do período.
- Taxa descritiva de resultado positivo = (recuperados + renovados) / (recuperados + renovados + cancelamentos). Registros em acompanhamento, sem resposta e outros ficam fora do denominador.
- Não afirma causalidade entre uma ação e o resultado; usa os resultados registrados pelo Personal.
- Isolamento por Personal é aplicado consultando interações pelo ID do usuário autenticado.

## Validação

O ZIP foi verificado quanto à integridade. A compilação Maven e testes funcionais devem ser executados no ambiente do projeto antes de publicar.
