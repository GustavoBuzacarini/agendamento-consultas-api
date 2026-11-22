<!-- onp-spec:documentation-contract=v1 -->

# TDD: Documentação da API (OpenAPI/Swagger)

> feature: documentacao-api
> status: revisado

[PRD](prd.md) · [ADR](adr.md) · [Spec](spec.md) · [Tarefas](tasks.md) · [Pesquisa](research.md)

## 1. Identificação e responsáveis

Autor: Claude (sessão Claude Code). Responsável técnico e aprovador: cliente (aluno do curso). Data: 2026-11-22. Versão 0.1. Revisão: aprovado pelo cliente em 2026-11-22 ("Aprovo os 4 (Recomendado)").

## 2. Contexto e sistema atual

API Spring Boot 4.1.1 / Java 25 com login JWT e perfis ([autenticacao](../autenticacao/design.md)). `SegurancaConfig` protege tudo, exceto `POST /auth/login`, `GET /actuator/health` e `/error`. Controllers em `paciente`, `profissional`, `motivo`, `consulta`, `agenda`, `conta` e `seguranca`.

## 3. Problema e motivação

Sem documentação navegável, usar e avaliar a API exige ler o código. Custo de não agir: baixa usabilidade e portfólio menos claro. Ver [PRD](prd.md).

## 4. Escopo e não objetivos

Escopo: AC-040 a AC-042 da [spec](spec.md). Não objetivos: exemplos detalhados por endereço, versionamento, publicação externa.

## 5. Solução e arquitetura

- Dependência `springdoc-openapi-starter-webmvc-ui` (versão compatível com o Boot 4.1.1, a confirmar). Gera `/v3/api-docs` e a Swagger UI (`/swagger-ui.html` e `/swagger-ui/**`) a partir dos controllers.
- `ConfiguracaoDocumentacao` (pacote `documentacao`): um `@Bean OpenAPI` com título "API de Agendamento de Consultas", descrição em português (regras de prioridade e perfis) e o esquema de segurança `bearerAuth` (HTTP, scheme `bearer`, formato JWT), aplicado globalmente para a página oferecer o botão "Authorize".
- **Acesso:** `SegurancaConfig` lê `documentacao.aberta` (padrão `false`, variável `DOCUMENTACAO_ABERTA`). Se `true`, acrescenta `requestMatchers(GET, "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()` antes da regra geral. Se `false`, esses endereços caem na regra geral e exigem token (qualquer perfil autenticado em `/v3/api-docs`; a regra `anyRequest` exige perfil RECEPCAO, então só a recepção os vê no modo fechado).
- Sem mudança de contrato dos endereços existentes e sem migração de dados.

## 6. Riscos e mitigação

| Risco | Evidência / hipótese | Probabilidade | Impacto | Mitigação | Responsável |
|---|---|---|---|---|---|
| Versão do springdoc incompatível com o Boot 4.1.1 | Fontes divergem (Pesquisa) | Média | Médio | Subir a aplicação e rodar os testes; tentar outra versão 3.x | Claude |
| Flag ligada em ambiente exposto | Padrão é `false` | Baixa | Médio | Padrão desligado; aviso no README e no `.env.example` | Cliente |
| Regressão na segurança existente | Edita `SegurancaConfig` | Baixa | Alto | Rodar `verify` das 3 features; testes de 401/403 existentes cobrem | Claude |
| Documentação expor detalhes internos | Gerada dos controllers | Baixa | Baixo | Só endereços e modelos públicos; sem segredos | Claude |

## 7. Plano de implementação

Tarefa única, [T-018](tasks.md): dependência, `ConfiguracaoDocumentacao`, ajuste da `SegurancaConfig`, `application.yml`, `.env.example`, README e testes. Execução sequencial (um conjunto de arquivos).

## 8. Segurança e privacidade

Documentação não contém dados de pacientes (só estrutura). Padrão fechado. Quando aberta, só GET e só os endereços de documentação; todos os endereços de dados continuam exigindo token e perfil. Sem segredos no código (a flag não é segredo). Sem novos dados pessoais.

## 9. Estratégia de testes

Dois testes de integração: `DocumentacaoAbertaTest` (com `documentacao.aberta=true`) prova AC-040 e AC-041; `DocumentacaoFechadaTest` (padrão) prova AC-042, incluindo o acesso com token da recepção. Regressão: `onp-spec verify` e `audit --ci` das três features.

## 10. Operação e observabilidade

Sem métricas novas. Diagnóstico: se a página der 404 ou a aplicação falhar ao iniciar após atualizar o Spring Boot ou o springdoc, conferir a compatibilidade das versões.

## 11. Reversão e recuperação

Remover a dependência, a classe de configuração e a exceção na `SegurancaConfig`. Nenhum dado afetado.

## 12. Métricas de sucesso

Técnicas: AC-040 a AC-042 com PASS e 39 critérios anteriores intactos; `audit --ci` exit 0. De produto: não definidas.

## 13. Glossário e domínios

- **OpenAPI:** formato que descreve a API. **Swagger UI:** página interativa que lê o OpenAPI. **Documentação aberta:** acessível sem token (`DOCUMENTACAO_ABERTA=true`).

## 14. Alternativas e decisões

Ver [ADR](adr.md): springdoc (A), OpenAPI à mão, README, sempre aberta e só com token descartados. Pendente: aceite da proposta A.

## 15. Dependências e compatibilidade

springdoc-openapi-starter-webmvc-ui (linha 3.x; versão a confirmar), Spring Boot 4.1.1, Java 25, Spring Security (já presente). Variável nova: `DOCUMENTACAO_ABERTA` (opcional).

## 16. Performance e capacidade

Não aplicável: a documentação é gerada sob demanda, fora do caminho de negócio, e a feature não altera nenhum endereço de dados. Sem orçamento de performance definido nem medição.

## 17. Migração e implantação

Sem migração. Implantação: nenhuma variável obrigatória; para usar a página localmente, `DOCUMENTACAO_ABERTA=true` no `.env`.

## 18. Perguntas e suposições

Sem suposições nem perguntas abertas na [spec](spec.md). Conflito com AC-025/AC-027 (autenticacao): decidido pelo cliente ("Prosseguir (Recomendado)").

## 19. Marcos e acompanhamento

Marco único: T-018. Acompanhamento pela lista de tarefas do harness.

## 20. Revisão e rastreabilidade

PRD (requisitos) → ADR (springdoc e flag) → TDD (este) → spec (AC-040…042) → tasks (T-018) → testes `@spec:AC-xxx` → `verify`. Revisores: nenhum ainda. Altere o status para revisado após a aprovação do cliente.

Estrutura baseada no [Technical Design Doc Creator, Tech Leads Club](https://github.com/tech-leads-club/agent-skills/blob/main/packages/skills-catalog/skills/%28creation%29/create-technical-design-doc/SKILL.md); texto e requisitos de rastreabilidade adaptados para este projeto.
