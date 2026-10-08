# Checklist de validação — Etapa 85

- [x] Partir do V23 / Etapa 84
- [x] Não criar nova tabela
- [x] Analisar até 6 sessões anteriores
- [x] Calcular variação de volume
- [x] Calcular variação de RPE
- [x] Contar sessões no topo da faixa
- [x] Gerar nível de tendência
- [x] Gerar nível de confiança
- [x] Integrar ao portal do aluno
- [x] Manter a decisão final com o Personal
- [x] Preservar endpoints anteriores de aprendizado
- [x] Validar integridade do ZIP

## Build

O build Maven completo precisa ser executado no ambiente do projeto porque este ambiente não conseguiu baixar o Maven 3.9.16 de repo.maven.apache.org.

No Windows:

```bat
rmdir /s /q target
mvnw.cmd clean package -DskipTests
mvnw.cmd spring-boot:run
```
