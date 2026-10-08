# Correção do deploy EasyPanel — AdminMasterController

## Causa raiz
O build do EasyPanel falhou na compilação Java em `AdminMasterController.java`.

Erros reportados pelo Maven:
- linha 31: `illegal start of type`
- linha 31: `> expected`
- linha 32: `illegal start of type`
- linha 112: `illegal start of type`
- linha 332: `illegal start of type`
- linha 332: `malformed floating-point literal`
- linha 332: `';' expected`

O arquivo correto desta versão do TreinoFlow foi restaurado. A versão corrigida possui estrutura Java válida, 250 linhas e 41 pares de chaves.

## Observação de segurança
O log de deploy contém credenciais passadas como build arguments. Essas credenciais devem ser rotacionadas e mantidas apenas nas variáveis de ambiente/Secrets do EasyPanel, nunca no código ou em logs compartilhados.
