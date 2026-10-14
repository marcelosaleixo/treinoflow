TREINOFLOW V117 — CORREÇÃO DEFINITIVA DA COMPATIBILIDADE DO CRM

Causa:
O log mostra que a aplicação em execução ainda está usando uma versão de CrmService que chama
buscarPorPersonalETermo(Long, String, Pageable), enquanto a classe AlunoRepository carregada
só expõe a versão com quatro argumentos. Isso é incompatibilidade entre código compilado/deployado.

Correções nesta versão:
- CrmService chama a assinatura de três argumentos.
- AlunoRepository declara explicitamente a consulta de três argumentos com @Query.
- A consulta de quatro argumentos permanece disponível para as demais telas/filtros.
- Arquivos alterados nas duas cópias do projeto que existem no ZIP.

IMPORTANTE PARA O DEPLOY:
Se o mesmo erro persistir após usar esta versão, o EasyPanel não está executando estes arquivos.
Confira o repositório, branch, diretório de build e commit utilizados pelo serviço. Faça rebuild sem cache
e confirme no log que o novo commit foi baixado. Reiniciar o container sem rebuild pode manter classes antigas.

Validação realizada:
- ZIP testado quanto à integridade.
- Conferidas as duas cópias de CrmService.java e AlunoRepository.java.
- A compilação Maven e o teste no ambiente EasyPanel não foram executados neste ambiente.
