<!-- onp-spec:documentation-contract=v1 -->

# ADR: Stack e padrões de projeto do agendamento de consultas

> feature: agendamento-consultas
> status: revisado

[PRD](prd.md) · [TDD](design.md) · [Spec](spec.md) · [Tarefas](tasks.md) · [Pesquisa](research.md)

## Identificação e contexto

- Decisão: stack (Java, Spring Boot, Gradle, PostgreSQL), arquitetura em camadas simples e uso dos padrões State, Chain of Responsibility e Strategy.
- Data: 2026-11-22. Domínio: agendamento-consultas (projeto novo). Atende todo o [PRD](prd.md) e a [spec](spec.md).
- Autor da proposta: Claude (sessão Claude Code). **Decisões do cliente:** PostgreSQL e Gradle (resposta literal no PRD). O restante é proposta.

## Restrições e regras anteriores

Não há ADR/PRD anterior. Vigente: [constituição](../../constituicao.md) (P-001 prova executável; P-002 segredos fora do código; a regra de verificação do P-002 aponta para `src/**/*.js` e **não cobre Java**, ver Consequências).

## Alternativas consideradas

| # | Alternativa | Benefício | Custo / limitação |
|---|---|---|---|
| A | **Spring Boot + JPA + Flyway + PostgreSQL, camadas controller/service/repository (proposta)** | Padrão de mercado, simples, bom para estudo | Requer Postgres local (Docker) |
| B | Hexagonal completo (ports/adapters) | Isolamento forte | Mais arquivos; excessivo para o MVP |
| C | Java puro sem Spring | Foca só nos padrões | Foge do pedido (Spring Boot) |
| D | H2 em memória | Zero infraestrutura | Contraria a escolha do cliente (PostgreSQL) |

Padrões, ligados ao que o curso pede (labs DIO `lab-padroes-projeto-java` e `-spring`, que trabalham **Singleton, Strategy e Facade**; em Spring, Singleton e Strategy/Repository):
- **Strategy** (curso) para prioridade: cada regra é um bean; somar regra nova não altera as existentes.
- **Facade** (curso) em `AgendamentoFacade`: ponto único que orquestra cadeia, prioridade, estado e persistência.
- **Singleton** (curso): beans do Spring, sem Singleton manual (alternativa a evitar: padrão por obrigação).
- **Facade do ViaCEP** (curso, adicionada em 2026-11-22 por decisão do cliente na Q-004): `CepFacade` isola a chamada HTTP externa; falha do serviço recusa o cadastro com 503.
- **State** (extra) para `StatusConsulta`: transições por estado, sem `if` espalhado.
- **Chain of Responsibility** (extra) para validações: cada regra é um elo, fácil acrescentar/remover.

## Decisão e justificativa

Proposta: **A**, com os cinco padrões acima (três do curso, dois extras). Java LTS e versão do Spring Boot serão fixados na T-001 consultando a documentação atual (não fixados aqui por falta de verificação). **Status: aceita pelo cliente em 2026-11-22.** As versões fixadas na T-001 foram Spring Boot 4.1.1 e Java 25.

## Consequências e riscos

- Ganha: código didático e testável; regras isoladas.
- Perde: a skill não lê o relatório JUnit do Gradle; é necessária uma ponte (tarefa T-001, [TDD §9](design.md)). Se a ponte falhar, o fallback é o reporter `exitcode` (prova fraca, sem granularidade por critério).
- A verificação do P-002 cobria só `.js`; em 2026-11-22 foi ajustada para `src/**/*.java` (recomendação aplicada a pedido do cliente: "faz o que é recomendado"). `application.yml` não é coberto por essa regra; segredos ficam em variáveis de ambiente (TDD §8).
- Postgres exige Docker/instalação local: `docker-compose.yml` na T-001.

## Validade, revisão e substituição

Revisar se: o escopo ganhar autenticação (fase 2), multi-unidade/multi-tenant, ou se a ponte de testes for substituída por reporter nativo. Nenhuma decisão anterior é substituída.

## Respostas e aprovação

PostgreSQL e Gradle: resposta literal do cliente, em [PRD](prd.md#pedido-original-do-cliente), 2026-11-22. Aprovação da proposta A (stack, camadas e cinco padrões): cliente, 2026-11-22, "Aprovo os 4" (resposta à pergunta sobre aprovar PRD, ADR, TDD e pesquisa como revisados). A decisão passa a **aceita**.
