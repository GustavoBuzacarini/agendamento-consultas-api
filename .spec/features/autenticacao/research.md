<!-- onp-spec:documentation-contract=v1 -->

# Pesquisa de produto: login e permissões

> feature: autenticacao
> status: revisado

[PRD](prd.md) · [ADR](adr.md) · [TDD](design.md) · [Spec](spec.md) · [Tarefas](tasks.md)

## Pergunta e hipótese

Pergunta: qual a forma mais simples e segura de autenticar uma API Spring Boot de estudo com dois perfis? Hipótese (falsificável): Spring Security como resource server com JWT HS256 stateless atende o escopo sem infraestrutura extra. A evidência pode mudar a escolha entre HS256 e RS256 e entre bcrypt e Argon2id.

Consulta feita em 2026-11-22 por busca web. Resultados de busca são resumos, não leitura integral dos documentos; a versão exata do Spring Security 7 deve ser conferida na documentação oficial durante a implementação.

## Concorrentes e alternativas

| Produto / alternativa | Comportamento observado | Fonte primária | Data da consulta | Limitações |
|---|---|---|---|---|
| Spring Security resource server JWT | O resource server, por padrão, só aceita tokens RS256; para HS256 define-se explicitamente um `JwtDecoder` com a chave. O módulo de decodificação JWT vem em `spring-security-oauth2-jose`. | [documentação do Spring Security](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html) | 2026-11-22 | Não achei exemplo específico de Spring Boot 4 / Security 7 com HS256; conferir na implementação |
| Sessão com cookie | Não pesquisada | — | — | Descartada por design (ver ADR) |
| Provedor externo (Keycloak, Auth0) | Não pesquisado a fundo | — | — | Fora do escopo de estudo |
| Fazer nada (API aberta) | Mantém dados de pacientes expostos | — | — | Risco à privacidade |

## Evidências e dados

- **Chave HS256:** fontes secundárias indicam que HS256 exige segredo de 256 bits (32 bytes) e que a chave deve vir de variável de ambiente, não do código. Por isso o AC-038 exige no mínimo 32 caracteres. Observação de blogs e documentação; não validei a biblioteca com a chave curta.
- **Senhas:** a [cartilha da OWASP](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) prefere Argon2id; para bcrypt indica fator de custo de pelo menos 10 (uma cópia não oficial cita 12) e limite de 72 bytes na senha. Adotamos custo 12 e limite de 72 caracteres. Não encontrei revisão datada de 2026 da cartilha.
- **Dados de uso:** nenhum. Sem baseline do volume de logins nem de cancelamentos pelo paciente: **inconclusivo**. Coleta proposta se for a uso real: por 4 semanas, contar logins, falhas de login por usuário e cancelamentos feitos pelo paciente versus pela recepção (numerador/denominador por semana).

## Viabilidade matemática e estatística

Custo do bcrypt: cada login faz um hash; o alvo da OWASP é menos de 1 segundo por hash. O custo 12 foi medido na máquina do projeto na T-012: 316 ms por login em média (5 logins, pelo MockMvc); é medida de um cenário, não garantia geral. Volume esperado no estudo: baixo.

## Proposta e alternativas ao pedido

Manter as escolhas do cliente (Recomendado em todas). Alternativas registradas no [ADR](adr.md): RS256, sessão, provedor externo, Argon2id. Nenhuma altera o escopo sem decisão do cliente.

## Decisão e experimento

Decisões do cliente: respostas literais no [PRD](prd.md). Experimento de validação: os critérios da spec provados por testes (`onp-spec verify`). Suposições confirmadas e documentos aprovados pelo cliente em 2026-11-22.
