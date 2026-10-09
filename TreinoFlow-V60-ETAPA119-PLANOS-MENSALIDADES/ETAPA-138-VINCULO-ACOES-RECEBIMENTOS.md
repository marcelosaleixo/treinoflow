# Etapa 138 — Vínculo entre ações de CRM e recebimentos

## Entrega
- Nova entidade `InteracaoRecebimentoVinculo` para associar uma interação do CRM a um pagamento efetivamente registrado.
- Restrição única por par interação/pagamento para impedir duplicidade do mesmo vínculo.
- Repositório com consulta dos vínculos por Personal e soma dos valores associados a uma ação.
- A entidade referencia os pagamentos já existentes; não cria nem altera lançamentos financeiros.

## Regras importantes
- A camada de serviço/controlador que expuser criação ou remoção de vínculos deve validar que a interação e o pagamento pertencem ao mesmo Personal e que o aluno relacionado é consistente.
- A soma é um indicador de recebimentos vinculados, não prova causal de que a ação gerou o pagamento.
- O projeto pode criar a tabela via configuração JPA existente; em produção, prefira migração versionada e backup antes de aplicar alterações de esquema.

## Validação
O pacote foi montado a partir da versão V82 e a integridade do ZIP foi verificada. Compilação e testes de integração não foram confirmados neste ambiente.
