<!-- onp-spec:documentation-contract=v1 -->

# TDD: Login e permissões (fase 2)

> feature: autenticacao
> status: revisado

[PRD](prd.md) · [ADR](adr.md) · [Spec](spec.md) · [Tarefas](tasks.md) · [Pesquisa](research.md)

## 1. Identificação e responsáveis

Autor: Claude (sessão Claude Code). Responsável técnico e aprovador: cliente (aluno do curso). Data: 2026-11-22. Versão 0.1. Revisão: aprovado pelo cliente em 2026-11-22 ("Confirmo e aprovo tudo (Recomendado)").

## 2. Contexto e sistema atual

API Spring Boot 4.1.1 / Java 25 / PostgreSQL da feature [agendamento-consultas](../agendamento-consultas/design.md), hoje **sem autenticação** (ASM-007 anterior). Controladores: pacientes, profissionais, motivos, consultas, agenda. Erros em `ProblemDetail` pelo `TratadorDeErros`. Testes de integração com Testcontainers, `MockMvc` e relógio fixo (`RelogioDeTeste`, terça 2026-12-01 10:00).

## 3. Problema e motivação

Dados pessoais e de saúde ficam abertos a quem alcança a API, e o paciente não consegue consultar nem cancelar sozinho. Custo de não agir: exposição de dados (LGPD) e carga manual na recepção. Ver [PRD](prd.md).

## 4. Escopo e não objetivos

Escopo: AC-023 a AC-039 da [spec](spec.md). Não objetivos: refresh/revogação, bloqueio por tentativas, recuperação por e-mail, 2FA, contas extras da recepção, login social, HTTPS/CORS.

## 5. Solução e arquitetura

Pacotes novos: `seguranca/` (configuração, JWT, respostas de erro, usuário logado) e `conta/` (usuário, login, contas de paciente, conta inicial).

**Modelo de dados** (migração `V6__usuario.sql`):
- `usuario(id, usuario UNIQUE, senha_hash, perfil, paciente_id UNIQUE NULL REFERENCES paciente(id), criado_em)`
- `perfil`: `RECEPCAO` | `PACIENTE`. Para `PACIENTE`, `paciente_id` é obrigatório (restrição `CHECK`); para `RECEPCAO`, é nulo.

**Fluxo de login:** `POST /auth/login {usuario, senha}` → busca o usuário → compara com BCrypt (custo 12) → emite JWT HS256 com `sub` = usuário, `perfil`, `pacienteId` (se paciente), `iat`, `exp = iat + 1 h` → `200 {token, tipo:"Bearer", expiraEm}`. Falha (usuário inexistente ou senha errada): `401 "usuário ou senha inválidos"`; para não revelar se o usuário existe, o hash de uma senha fictícia é comparado também no caso de usuário inexistente.

**Verificação:** `SegurancaConfig` (`SecurityFilterChain`): CSRF desligado (API sem cookie), sessão `STATELESS`, `oauth2ResourceServer(jwt)`. `JwtDecoder` construído com `NimbusJwtDecoder.withSecretKey(...)` e algoritmo HS256, com `JwtTimestampValidator` usando o `Clock` da aplicação. Um `JwtAuthenticationConverter` transforma a claim `perfil` na autoridade `ROLE_RECEPCAO` ou `ROLE_PACIENTE`. Erros: `AuthenticationEntryPoint` (401 "autenticação necessária" ou "token inválido ou expirado") e `AccessDeniedHandler` (403 "sem permissão para esta operação"), ambos em `application/problem+json`.

**Regras de acesso:**

| Caminho | Quem |
|---|---|
| `POST /auth/login`, `GET /actuator/health` | todos (sem token) |
| `POST /auth/senha` | qualquer usuário autenticado |
| `GET /consultas/minhas` | PACIENTE |
| `GET /consultas/{id}`, `PATCH /consultas/{id}/cancelar` | RECEPCAO; PACIENTE só se a consulta for dele (senão 403) |
| `POST /pacientes/{id}/conta`, `POST /pacientes/{id}/conta/senha-provisoria` | RECEPCAO |
| demais endereços (cadastros, agendar, confirmar, realizar, faltou, agenda) | RECEPCAO |

A checagem de dono é feita no serviço (compara `pacienteId` do token com o da consulta) com `UsuarioLogado`, e não só na configuração, porque depende de dados do banco.

**Conta inicial:** `ContaInicialRecepcao` (`ApplicationRunner`) cria a conta quando não há nenhuma de recepção e `RECEPCAO_USUARIO_INICIAL`/`RECEPCAO_SENHA_INICIAL` existem; senão, registra aviso. **Fail-fast:** `JwtProperties` valida `JWT_SECRET` ≥ 32 caracteres na inicialização (AC-038).

## 6. Riscos e mitigação

| Risco | Evidência / hipótese | Probabilidade | Impacto | Mitigação | Responsável |
|---|---|---|---|---|---|
| Força bruta no login | Sem limite de tentativas (ASM-015) | Média se exposto | Alto | Não expor publicamente; fase futura: limite por usuário/IP | Cliente |
| Token vazado vale até 1 h | Sem revogação (ASM-013) | Baixa | Médio | Validade curta; futuro: refresh com revogação | Cliente |
| Chave fraca ou vazada | `JWT_SECRET` só no ambiente | Baixa | Alto | Falha ao iniciar se < 32 caracteres (AC-038); `.env` fora do git | Claude/cliente |
| Regressão dos 22 critérios anteriores | Todos os endereços passam a exigir token | Média | Médio | Testes antigos com token da recepção por padrão; `verify` de ambas as features | Claude |
| Enumeração de usuários | Mensagens diferentes no login | Média | Médio | Mesma mensagem e comparação de hash fictício (AC-024) | Claude |
| Spring Security 7 com HS256 diferente do documentado | Não achei exemplo oficial para Boot 4 | Média | Baixo | Implementar com teste primeiro (AC-023, AC-026) e conferir a documentação oficial | Claude |

## 7. Plano de implementação

Ordem ([tasks](tasks.md)): T-011 (segurança base, adapta os testes) → T-012 (usuário e login) → T-013 (contas de pacientes) e T-015 (trocar senha) e T-016 (conta inicial) → T-014 (permissões e consultas do paciente) → T-017 (documentação). T-012, T-015 e T-016 tocam arquivos em comum com T-012; executar em sequência.

## 8. Segurança e privacidade

Esta feature é a análise de segurança do sistema. Autenticação: JWT HS256, 1 hora, sem sessão. Senhas: BCrypt custo 12, mínimo 8 e máximo 72 caracteres; nunca em texto no banco, em logs ou em respostas (exceto a senha provisória, mostrada uma vez ao gerar, AC-039). A senha provisória vem de `SecureRandom`. Segredos (`JWT_SECRET`, senha inicial) só por variável de ambiente (P-002). Logs: nunca registrar senha, token nem CPF completo. Mensagens de erro de login sem pistas. Dados pessoais: LGPD; sem retenção definida (fora de escopo). CSRF desligado é aceitável porque não há cookie de sessão e o token vai no cabeçalho `Authorization`.

## 9. Estratégia de testes

JUnit 5 + Testcontainers + `MockMvc`. Cada critério tem teste com `@spec:AC-xxx` no `@DisplayName`. A `IntegrationTestBase` passa a enviar por padrão um token da recepção (emitido pelo `JwtService` com o relógio fixo), e oferece um `MockMvc` sem token para AC-025/026/027. Os 22 critérios anteriores continuam provados pelos mesmos testes. AC-026: token expirado emitido com `exp` no passado e token adulterado com assinatura trocada. AC-038: `ApplicationContextRunner` com chave curta. AC-039: consulta direta ao banco confirma hash BCrypt (`$2...`) e que nenhuma resposta traz a senha. Regressão: `onp-spec verify` e `audit --ci` das duas features.

## 10. Operação e observabilidade

Logs em INFO: login com sucesso e falha (usuário, sem senha), criação de conta, troca de senha, criação da conta inicial. Sem métricas novas. Diagnóstico: aviso de inicialização quando falta a conta inicial.

## 11. Reversão e recuperação

Migração `V6` só acrescenta tabela. Reversão: remover a configuração de segurança e a tabela. Dados anteriores não são afetados. Perda de acesso da recepção: recriar a conta pela configuração (apagar o usuário no banco e subir com as variáveis iniciais).

## 12. Métricas de sucesso

Técnicas: 17 critérios novos e 22 antigos com PASS; `audit --ci` exit 0 nas duas features. De produto: hipóteses no [PRD](prd.md), sem baseline.

## 13. Glossário e domínios

- **Perfil:** RECEPCAO ou PACIENTE. **Token:** JWT de 1 hora. **Senha provisória:** gerada pelo sistema, mostrada uma vez. Domínios: autenticacao (novo) e agendamento-consultas (afetado).

## 14. Alternativas e decisões

Ver [ADR](adr.md): Spring Security + JWT HS256 stateless (A); sessão, RS256, provedor externo e filtro caseiro descartados. Pendente: aceite da proposta A.

## 15. Dependências e compatibilidade

Spring Security (starter de segurança e de resource server, nomes e versões a confirmar no `build.gradle` na T-011), `spring-security-test` (se necessário), BCrypt do Spring Security, Nimbus JOSE (vem com o resource server), Java 25. Variáveis novas: `JWT_SECRET`, `RECEPCAO_USUARIO_INICIAL`, `RECEPCAO_SENHA_INICIAL`. Sem elas, a aplicação não inicia (JWT) ou sobe sem conta inicial (recepção).

## 16. Performance e capacidade

Custo dominante: um hash BCrypt por login. Orçamento (hipótese, não medido): login p95 < 1 s (alvo OWASP) com custo 12 em uma máquina de desenvolvimento, 1 requisição por vez; verificação de token < 20 ms. Medido na T-012 (2026-11-22): login com BCrypt de custo 12 levou **316 ms em média** (5 logins em sequência, depois de aquecer a JVM, pelo MockMvc, com o PostgreSQL em container, na máquina de desenvolvimento do projeto), dentro do alvo de menos de 1 s. É uma medida de uma máquina e de um cenário; não vale como garantia para outro ambiente. A verificação do token não foi medida isoladamente.

## 17. Migração e implantação

Sem migração de dados. Implantação: definir as três variáveis no ambiente e subir. Compatibilidade retroativa: **quebra** o acesso anônimo à API (decisão do cliente); clientes precisam logar.

## 18. Perguntas e suposições

ASM-010 a ASM-018 da [spec](spec.md#suposições), confirmadas pelo cliente em 2026-11-22; sem perguntas abertas. Conflito com ASM-007 da feature anterior: decidido pelo cliente ("Prosseguir (Recomendado)").

## 19. Marcos e acompanhamento

M1 segurança base e testes antigos autenticados (T-011); M2 login e contas (T-012, T-013); M3 troca de senha, conta inicial e permissões (T-015, T-016, T-014); M4 documentação (T-017). Acompanhamento pela lista de tarefas do harness; sem estimativa de tempo por falta de histórico.

## 20. Revisão e rastreabilidade

PRD (requisitos) → ADR (stack de segurança) → TDD (este) → spec (AC-023…039) → tasks (T-011…017) → testes `@spec:AC-xxx` → `verify`. Revisores: nenhum ainda. Altere o status para revisado depois do aceite do cliente.

Estrutura baseada no [Technical Design Doc Creator, Tech Leads Club](https://github.com/tech-leads-club/agent-skills/blob/main/packages/skills-catalog/skills/%28creation%29/create-technical-design-doc/SKILL.md); texto e requisitos de rastreabilidade adaptados para este projeto.
