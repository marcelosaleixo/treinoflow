# TreinoFlow — Etapa 5

## Foco
CRUD de alunos com isolamento por personal autenticado e injeção de dependências por campo usando `@Autowired`, conforme padrão solicitado.

## Implementado
- Listagem de alunos do personal autenticado.
- Cadastro de aluno.
- Edição de aluno.
- Exclusão de aluno.
- Status ATIVO/INATIVO.
- Dashboard com total de alunos ativos.
- Navegação Dashboard/Alunos.
- Validação de propriedade: o controller não recebe `personalId` do navegador; obtém o personal pelo e-mail da sessão autenticada.
- Regras de negócio continuam centralizadas no `AlunoService`.
- Serviços, controllers e `PersonalUserDetailsService` com injeção por campo via `@Autowired`.
- Repositórios mantidos com `@Repository`.

## Padrão de injeção adotado
```java
@Service
@Transactional
public class AlunoService {

    @Autowired
    private AlunoRepository alunos;

    @Autowired
    private UsuarioPersonalRepository personais;
}
```

## Observação
O build não foi confirmado neste ambiente porque o Maven não está instalado e o Maven Wrapper não conseguiu baixar o Maven do repositório externo.
