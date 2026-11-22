<!-- onp-spec:documentation-contract=v1 -->

# TDD: Agendamento de consultas

> feature: agendamento-consultas
> status: revisado

[PRD](prd.md) · [ADR](adr.md) · [Spec](spec.md) · [Tarefas](tasks.md) · [Pesquisa](research.md)

## 1. Identificação e responsáveis

Autor: Claude (sessão Claude Code). Responsável técnico e aprovador: cliente (aluno do curso). Data: 2026-11-22. Versão 0.1. Revisão: aprovado pelo cliente em 2026-11-22 ("Aprovo os 4"). O TDD descreve o plano; a implementação seguiu o desenho com os desvios registrados nas tarefas (por exemplo: restrição de exclusão no PostgreSQL em vez de índice único parcial, e `AgendaConsultas` como interface entre a cadeia e a persistência).

## 2. Contexto e sistema atual

Projeto novo, sem sistema anterior e sem outros domínios. Documentos relacionados: apenas os desta feature e a [constituição](../../constituicao.md).

## 3. Problema e motivação

Ver [PRD](prd.md): agendas com conflito e sem critério de prioridade. Custo de não agir: horários perdidos e atendimento sem ordem legal.

## 4. Escopo e não objetivos

Escopo: AC-001 a AC-022 da [spec](spec.md). Não objetivos: login, visão do médico, notificações, penalização por faltas, integrações, interface web, triagem clínica.

## 5. Solução e arquitetura

Aplicação única Spring Boot (monólito), camadas `controller → service → repository`, pacote `br.com.agendamento.consultas`.

**Modelo de dados (PostgreSQL, migrações Flyway):**
- `paciente(id, nome, cpf UNIQUE, data_nascimento, gestante, pessoa_com_deficiencia, cep, logradouro, bairro, cidade, uf)` — endereço opcional, preenchido pelo ViaCEP (US-006)
- `profissional(id, nome, especialidade, duracao_consulta_min)` e `expediente(profissional_id, dia_semana, hora_inicio, hora_fim)`
- `motivo_atendimento(id, descricao UNIQUE, gravidade)` — gravidade: VERDE (normal) | AMARELO (intermediária) | VERMELHO (grave)
- `consulta(id, paciente_id, profissional_id, motivo_id, inicio, fim, status, prioridade, criada_em)`

**Padrões:**
- *State* — `StatusConsulta`: AGENDADA → CONFIRMADA → REALIZADA | FALTOU; AGENDADA|CONFIRMADA → CANCELADA. Cada estado implementa `EstadoConsulta` com as transições válidas; qualquer outra lança `TransicaoInvalidaException` (409).
- *Chain of Responsibility* — `CadeiaDeValidacao` executa elos em ordem: motivo existente → expediente → conflito de horário → antecedência mínima → limite de consultas. Elos de antecedência e limite são ignorados só quando o motivo é VERMELHO (ASM-002, AC-017); AMARELO segue as regras (AC-019).
- *Strategy* — `RegraPrioridade` (idoso, gestante, deficiência, gravidade); `CalculadoraPrioridade` aplica todas as regras injetadas e escolhe a maior: URGENTE (VERMELHO) > PRIORITARIA (idoso, gestante, PcD, AMARELO) > NORMAL (VERDE).
- *Facade* — `AgendamentoFacade` é o ponto único chamado pelo controller; orquestra cadeia, prioridade, estado e repositório (padrão presente no lab do curso).
- *Facade (CEP)* — `CepFacade.buscar(cep)` esconde a normalização do CEP, a chamada HTTP ao ViaCEP (`ViaCepClient`, RestClient com limite de 3 s), a leitura da resposta e os erros: CEP inexistente ⇒ 422, serviço fora do ar ⇒ 503 e o paciente não é gravado (AC-020 a AC-022). É o equivalente do lab do curso que consulta o ViaCEP; os testes de API usam um `CepClient` de teste, sem internet.
- *Singleton / Repository* — beans do Spring (escopo singleton) e Spring Data JPA, como no lab do curso, sem Singleton escrito à mão.

**Relação com o curso (DIO, labs `lab-padroes-projeto-java` e `lab-padroes-projeto-spring`, lidos em 2026-11-22):** os labs mostram Singleton, Strategy e Facade (em Spring: Singleton, Strategy/Repository e Facade com consulta ViaCEP). Este projeto cobre os três e acrescenta State e Chain of Responsibility. A integração externa do lab (ViaCEP) foi incluída por decisão do cliente (Q-004, 2026-11-22) e virou a US-006.

**Contratos REST (rascunho):** `POST /pacientes`, `POST /profissionais`, `POST/GET /motivos`, `POST /consultas`, `PATCH /consultas/{id}/confirmar|cancelar|realizar|faltou`, `GET /agenda?profissionalId=&data=`. Erros em `application/problem+json` (RFC 9457): 400 validação, 404 não encontrado, 409 conflito/transição inválida, 422 regra de negócio.

**Falhas e concorrência:** dois agendamentos simultâneos no mesmo horário → restrição única parcial no banco `(profissional_id, inicio)` para consultas ativas, convertida em 409.

## 6. Riscos e mitigação

| Risco | Evidência / hipótese | Probabilidade | Impacto | Mitigação | Responsável |
|---|---|---|---|---|---|
| Gravidade clínica mal definida | Q-001 aberta | Média | Alto | Catálogo cadastrável; revisão por profissional de saúde antes de uso real | Cliente |
| Motor não lê JUnit do Gradle | Verificado no código do motor (reporters: tap, vitest-json, jest-json, exitcode) | Alta | Médio | Ponte `tools/run-gradle-tests.mjs` (T-001); fallback `exitcode` | Claude/cliente |
| Corrida em agendamentos simultâneos | Hipótese | Média | Médio | Índice único parcial + teste de concorrência | Claude |
| Dados pessoais sem autenticação | ASM-007 | Alta se exposto | Alto | MVP só local/estudo; login na fase 2 | Cliente |

## 7. Plano de implementação

Ordem das tarefas ([tasks](tasks.md)): T-001 → (T-002, T-003, T-004, T-007 em paralelo, arquivos disjuntos) → (T-005, T-006) → T-008 → T-009. Cada tarefa fecha com testes `@spec:AC-xxx` passando.

## 8. Segurança e privacidade

Dados pessoais e de saúde (CPF, gestante, PcD): LGPD os trata como dado sensível em parte. MVP **sem autenticação**: não expor publicamente. Segredos (senha do banco) via variáveis de ambiente (P-002); `docker-compose.yml` usa valores de desenvolvimento lidos de `.env` não versionado. Logs sem CPF completo (mascarar). Retenção: não definida no MVP (pergunta para a fase 2).

## 9. Estratégia de testes

JUnit 5 + Spring Boot Test + Testcontainers (PostgreSQL) para integração; testes unitários puros para State, Chain e Strategy. Cada critério tem um teste cujo `@DisplayName` contém `@spec:AC-xxx`. A ponte `tools/run-gradle-tests.mjs` executa `gradlew test`, lê `build/test-results/test/*.xml` e grava `.spec/verification/raw.json` no formato jest (`testResults[].assertionResults[]`); `onpspec.config.json` usa `reporter: jest-json`, `reporterOutputFile`, `testGlobs: src/test/**`, `srcGlobs: src/main/**`. Testes `@Disabled` contam como pulados (não são prova). Regressão: `onp-spec audit --ci` no fim de cada tarefa.

## 10. Operação e observabilidade

MVP local: Actuator `/actuator/health`; logs estruturados em INFO para criação/cancelamento de consulta (ids, sem dados pessoais). Sem alertas no MVP.

## 11. Reversão e recuperação

Migrações Flyway versionadas; reversão = restaurar o banco do ambiente local. Não aplicável a produção neste MVP.

## 12. Métricas de sucesso

Técnicas: 22/22 critérios com PASS em `onp-spec verify`; `audit --ci` com saída 0. De produto: hipóteses no [PRD](prd.md), sem baseline.

## 13. Glossário e domínios

- **Consulta ativa:** AGENDADA ou CONFIRMADA (ocupa horário e conta no limite).
- **Encaixe:** agendamento de caso VERMELHO que dispensa antecedência e limite.
- **Prioridade:** URGENTE, PRIORITARIA, NORMAL.
- **Gravidade:** atributo do motivo de atendimento, por cor: VERDE (normal), AMARELO (intermediária), VERMELHO (grave).

## 14. Alternativas e decisões

Ver [ADR](adr.md). Pendente: aceite da proposta A e versões de Java/Spring Boot (decididas na T-001).

## 15. Dependências e compatibilidade

ViaCEP (https://viacep.com.br/ws/{cep}/json/, serviço público externo; formato da resposta conferido em 2026-11-22; sem ele, o cadastro **com CEP** é recusado com 503, e o cadastro sem CEP continua funcionando), Java 25 e Spring Boot 4.1.1 (fixados na T-001), Gradle wrapper, Spring Boot, PostgreSQL (via Docker Compose), Flyway, Testcontainers (exige Docker em execução), Node ≥ 18 para a skill e a ponte. Sem Docker: testes de integração não rodam (usar o fallback a decidir com o cliente).

## 16. Performance e capacidade

Orçamento (hipótese, não medido): agendar uma consulta p95 < 300 ms com 10 000 consultas na base, 1 instância local, 10 requisições concorrentes. Medir na T-008 com carga simples antes de afirmar; sem medição, não há resultado.

## 17. Migração e implantação

Sem migração (projeto novo). Implantação local por `docker compose up` + `./gradlew bootRun`.

## 18. Perguntas e suposições

ASM-001 a ASM-008 confirmadas pelo cliente em 2026-11-22; ASM-009 e Q-001, Q-003, Q-004, Q-005 abertas, Q-002 respondida (ver [spec](spec.md#suposições)); responsável: cliente. Nenhum conflito com domínio anterior (projeto novo).

## 19. Marcos e acompanhamento

M1 esqueleto + ponte de testes (T-001); M2 cadastros (T-002/3/4); M3 regras e padrões (T-005/6/7); M4 agendamento e agenda (T-008/9). Acompanhamento pela lista de tarefas do harness; sem estimativas de tempo por falta de histórico.

## 20. Revisão e rastreabilidade

PRD (requisitos) → ADR (stack/padrões) → TDD (este) → spec (AC-001…022) → tasks (T-001…009) → testes `@spec:AC-xxx` → `verify`. Revisores: nenhum ainda. Altere o status para revisado após o cliente validar as suposições.

Estrutura baseada no [Technical Design Doc Creator, Tech Leads Club](https://github.com/tech-leads-club/agent-skills/blob/main/packages/skills-catalog/skills/%28creation%29/create-technical-design-doc/SKILL.md); texto e requisitos de rastreabilidade adaptados para este projeto.
