<!-- onp-spec:documentation-contract=v1 -->

# ADR: Autenticação com Spring Security e JWT stateless

> feature: autenticacao
> status: revisado

[PRD](prd.md) · [TDD](design.md) · [Spec](spec.md) · [Tarefas](tasks.md) · [Pesquisa](research.md)

## Identificação e contexto

- Decisão: como autenticar e autorizar a API na fase 2.
- Data: 2026-11-22. Domínio: autenticacao, com impacto em agendamento-consultas. Atende os requisitos do [PRD](prd.md).
- Autor: Claude (sessão Claude Code). Decisões do cliente: token de acesso único de 1 hora, perfis Recepção e Paciente, conta criada pela recepção, conta inicial por variáveis de ambiente (respostas literais no PRD).

## Restrições e regras anteriores

- [Constituição](../../constituicao.md): P-001 (prova executável) e P-002 (segredos fora do código; verificação sobre `src/**/*.java`). A chave `JWT_SECRET` e a senha inicial virão só do ambiente.
- Feature anterior, ADR [agendamento-consultas](../agendamento-consultas/adr.md): stack Spring Boot, camadas e padrões. **Continua válida.**
- Regra anterior **substituída para esta fase, por decisão explícita do cliente** ("Prosseguir (Recomendado)", 2026-11-22): ASM-007 "Sem autenticação no MVP" da spec anterior e "Sem login" do PRD anterior. O ADR anterior não é alterado.

## Alternativas consideradas

| # | Alternativa | Benefício | Custo / limitação |
|---|---|---|---|
| A | **Spring Security + resource server JWT, HS256, sem sessão (proposta)** | Padrão do ecossistema; sem estado no servidor; poucas classes | Sem revogação antes de expirar; chave compartilhada |
| B | Sessão com cookie (HttpSession) | Revogação simples | Exige estado e não combina com API consumida por apps; CSRF |
| C | JWT com RS256 (chave pública/privada) | Chave de verificação pública; melhor para vários serviços | Gestão de par de chaves; excessivo para um serviço único |
| D | Provedor externo (Keycloak/Auth0) | Recursos prontos (refresh, MFA, recuperação) | Infraestrutura e custo; foge do escopo de estudo |
| E | Autenticação própria sem Spring Security (filtro caseiro) | Nenhuma dependência nova | Reinventa segurança; risco de falhas |

Hash de senha: **BCrypt** (custo 12) em vez de Argon2id. A cartilha da OWASP prefere Argon2id e aceita bcrypt como alternativa quando Argon2id não está disponível; adotamos bcrypt por vir pronto no Spring Security e por bastar ao projeto, com limite de 72 caracteres.

## Decisão e justificativa

Proposta: **A**, com BCrypt custo 12, token de 1 hora e sem refresh (decisão do cliente). O decodificador é construído explicitamente com a chave HS256 (o padrão do resource server é RS256) e usa o `Clock` da aplicação. **Status: aceita pelo cliente em 2026-11-22.**

## Consequências e riscos

- Ganha: API fechada, perfis e autoatendimento mínimo, com pouca complexidade.
- Perde: sem revogação, um token vazado vale até expirar (1 hora); sem limite de tentativas, o login sofre força bruta (ASM-015). Mitigação futura: limite por usuário/IP, refresh com revogação, Argon2id.
- Os 22 critérios anteriores passam a rodar com token da recepção (mudança só nos testes; comportamento igual). Ver [TDD §9](design.md).
- Detalhes técnicos e riscos no [TDD](design.md).

## Validade, revisão e substituição

Revisar se: surgir segundo serviço (considerar RS256), exigência de logout imediato (refresh com revogação), exposição pública na internet (limite de tentativas, HTTPS obrigatório, MFA) ou novos perfis. Este ADR **substitui, só para a fase 2**, a regra de API aberta da feature anterior; ele não apaga nem altera o histórico dela.

## Respostas e aprovação

Aceite do conflito com a regra anterior: cliente, 2026-11-22, "Prosseguir (Recomendado)" (registrado em `governance.json` por `onp-spec impacto`). Aprovação da proposta A: cliente, 2026-11-22, "Confirmo e aprovo tudo (Recomendado)". A decisão passa a **aceita**.
