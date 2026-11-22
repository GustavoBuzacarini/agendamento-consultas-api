<!-- onp-spec:documentation-contract=v1 -->

# PRD: Documentação da API (OpenAPI/Swagger)

> feature: documentacao-api
> status: revisado

[ADR](adr.md) · [TDD](design.md) · [Spec](spec.md) · [Tarefas](tasks.md) · [Pesquisa](research.md)

## Pedido original do cliente

> Documentação da API (OpenAPI/Swagger) (resposta do cliente, 2026-11-22, à pergunta 'Quais melhorias opcionais você quer?'). Acesso: 'Fechada por padrão (Recomendado)'. Conflito com AC-025/AC-027 da feature autenticacao: 'Prosseguir (Recomendado)'.

## Problema, público e contexto

**Palavras do cliente:** uma documentação da API em OpenAPI/Swagger, fechada por padrão.

**Interpretação da equipe (a validar):** a API tem 20+ endereços, perfis e token; sem documentação, só quem leu o código sabe usá-la. Público: o próprio cliente (testar sem PowerShell), quem avalia o projeto do curso (portfólio) e um futuro front-end.

## Resultados desejados e métricas

Resultado verificável: os critérios AC-040 a AC-042 da [spec](spec.md) com prova PASS e nenhuma mudança nos 39 critérios já provados. Sem linha de base de uso; métrica de produto não definida.

## Requisitos e prioridades

| Requisito | Palavras do cliente / origem | Prioridade | Critérios na spec |
|---|---|---|---|
| Documentação OpenAPI e página Swagger UI | "Documentação da API (OpenAPI/Swagger)" | Alta | AC-040, AC-041 |
| Fechada por padrão; aberta só por configuração | "Fechada por padrão (Recomendado)" | Alta | AC-042 |
| Dar para usar o token na página | interpretação da equipe (a API exige login) | Média | AC-040 |

## Escopo e exclusões

Entra: US-013. Não entra: exemplos detalhados por endereço, versionamento, publicação externa.

## Regras de produto e domínios

- Domínio novo: documentação. Domínios afetados: **autenticacao** (regra "tudo protegido, exceto login e saúde", AC-025 e AC-027, e `SegurancaConfig`) e **agendamento-consultas** (os controllers passam a ser lidos para gerar a documentação, sem alteração de comportamento).
- Exceção à regra anterior, **por decisão explícita do cliente** ("Prosseguir (Recomendado)", 2026-11-22): com `DOCUMENTACAO_ABERTA=true`, apenas os endereços da documentação (GET) abrem sem token. Os documentos das features anteriores não são alterados.

## Dependências, riscos e suposições

- Depende da biblioteca springdoc-openapi compatível com o Spring Boot 4.1.1 (a confirmar na implementação).
- Risco: documentação aberta expõe a estrutura da API a quem alcançar a rede; por isso o padrão é fechada e a variável deve ficar desligada em qualquer ambiente exposto.
- Suposições: nenhuma aberta na [spec](spec.md).

## Validação com o cliente

Respostas literais acima. Documentos aprovados pelo cliente em 2026-11-22 ("Aprovo os 4 (Recomendado)").
