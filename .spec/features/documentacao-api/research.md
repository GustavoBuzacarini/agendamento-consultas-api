<!-- onp-spec:documentation-contract=v1 -->

# Pesquisa de produto: documentação da API

> feature: documentacao-api
> status: revisado

[PRD](prd.md) · [ADR](adr.md) · [TDD](design.md) · [Spec](spec.md) · [Tarefas](tasks.md)

## Pergunta e hipótese

Pergunta: qual biblioteca gera OpenAPI e Swagger UI para Spring Boot 4.1.1? Hipótese (falsificável): o springdoc-openapi, na linha 3.x, funciona com o Spring Boot 4 e basta para o escopo. A evidência pode mudar qual versão usar.

Consulta feita em 2026-11-22 por busca web e pelo repositório Maven Central. Resultados de busca são resumos, não leitura integral dos documentos.

## Concorrentes e alternativas

| Produto / alternativa | Comportamento observado | Fonte primária | Data da consulta | Limitações |
|---|---|---|---|---|
| springdoc-openapi (`springdoc-openapi-starter-webmvc-ui`) | Artefato para Spring MVC que inclui a Swagger UI; versões 3.0.2 a 3.2.1 existem no Maven Central. Uma fonte secundária diz que a linha 3.x atende o Spring Boot 4 e que a linha 2.x atende o Boot 3; os resultados divergem sobre qual 3.x casa exatamente com o 4.1 | [springdoc.org](https://springdoc.org) e [Maven Central](https://repo.maven.apache.org/maven2/org/springdoc/springdoc-openapi-starter-webmvc-ui/) | 2026-11-22 | Compatibilidade exata com o Boot 4.1.1 só se confirma subindo a aplicação e rodando os testes |
| OpenAPI escrito à mão | Não pesquisado | — | — | Desatualiza |
| Fazer nada | API sem documentação navegável | — | — | Difícil de usar e avaliar |

## Evidências e dados

- Uma fonte secundária relata que versão incompatível causa falha na inicialização ou 404 na página; por isso o AC-040/AC-041 provam a página e o resumo funcionando de verdade.
- Dados de uso: nenhum: **inconclusivo** quanto ao ganho para quem usa a API.

## Viabilidade matemática e estatística

Não aplicável: sem custo/benefício mensurável. Custo técnico: uma dependência e uma classe de configuração.

## Proposta e alternativas ao pedido

Manter o pedido do cliente (springdoc, fechada por padrão). Alternativas no [ADR](adr.md). Nenhuma amplia o escopo.

## Decisão e experimento

Decisões do cliente: respostas literais no [PRD](prd.md). Experimento de validação: AC-040 a AC-042 provados por testes (`onp-spec verify`), com a versão do springdoc que subir corretamente no Spring Boot 4.1.1. Documentos aprovados pelo cliente em 2026-11-22.
