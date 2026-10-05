# Correção — Erro Thymeleaf no Dashboard de Performance

## Causa
O template `performance/index.html` tentava avaliar uma expressão com chamada estática `T(java.lang.Math).min(...)` combinada com ternário dentro de `th:style`, e o parser do Thymeleaf falhou ao interpretar a expressão.

## Correção
A regra de cálculo da altura da barra foi movida para `DashboardPerformanceView.SemanaPerformanceView.alturaBarra()`. O template agora apenas renderiza:

```html
th:style="'height:' + semana.alturaBarra + '%'"
```

Isso mantém a apresentação simples e elimina lógica Java complexa do template.
