<!-- onp-spec:documentation-contract=v1 -->

# ADR: Documentação com springdoc-openapi e acesso por configuração

> feature: documentacao-api
> status: revisado

[PRD](prd.md) · [TDD](design.md) · [Spec](spec.md) · [Tarefas](tasks.md) · [Pesquisa](research.md)

## Identificação e contexto

- Decisão: como gerar e expor a documentação da API e como controlar o acesso a ela.
- Data: 2026-11-22. Domínio: documentacao-api, com impacto em autenticacao. Atende os requisitos do [PRD](prd.md).
- Autor: Claude (sessão Claude Code). Decisão do cliente: documentação "fechada por padrão".

## Restrições e regras anteriores

- [Constituição](../../constituicao.md): P-001 e P-002 (nada de segredo no código; a flag não é segredo).
- Feature [autenticacao](../autenticacao/adr.md): API stateless com JWT; AC-025 (sem token, 401) e AC-027 (só login e saúde abertos). **A regra continua válida** para tudo, com **uma exceção por configuração**, decidida pelo cliente ("Prosseguir (Recomendado)", 2026-11-22): quando `DOCUMENTACAO_ABERTA=true`, os endereços da documentação abrem sem token. O ADR anterior não é alterado.

## Alternativas consideradas

| # | Alternativa | Benefício | Custo / limitação |
|---|---|---|---|
| A | **springdoc-openapi (gera a partir dos controllers) + flag `documentacao.aberta` (proposta)** | Documentação sempre em dia com o código; pouca configuração | Dependência nova; versão precisa casar com o Spring Boot |
| B | Escrever o `openapi.yaml` à mão | Controle total do texto | Desatualiza; trabalho duplicado |
| C | Documentação só em Markdown no README | Nenhuma dependência | Não dá para testar a API; não é padrão de mercado |
| D | Documentação sempre aberta | Simples | Expõe a estrutura da API (descartada pelo cliente) |
| E | Documentação só com token | Sempre protegida | O navegador não envia cabeçalho na carga da página: Swagger UI inutilizável |

## Decisão e justificativa

Proposta: **A**. A flag `documentacao.aberta` (padrão `false`) é lida pela configuração de segurança, que só então libera os endereços da documentação (método GET). **Status: aceita pelo cliente em 2026-11-22.**

## Consequências e riscos

- Ganha: página interativa com botão "Authorize" para colar o token; documentação acompanha o código.
- Perde: uma dependência a mais e uma exceção controlada na `SegurancaConfig`.
- Risco: esquecer a flag ligada em ambiente exposto. Mitigação: padrão `false`; aviso no README e no `.env.example`. Ver [TDD](design.md).

## Validade, revisão e substituição

Revisar se: a API for publicada na internet (considerar perfil de execução ou rede separada para a documentação), ou se o springdoc deixar de acompanhar o Spring Boot. Esta decisão **não substitui** nenhuma regra anterior; acrescenta uma exceção configurável.

## Respostas e aprovação

Aceite do conflito com AC-025/AC-027: cliente, 2026-11-22, "Prosseguir (Recomendado)" (registrado em `governance.json` por `onp-spec impacto`). Aprovação da proposta A: cliente, 2026-11-22, "Aprovo os 4 (Recomendado)". A decisão passa a **aceita**.
