# Agendamento de Consultas (UBS / Clínica)

API REST em **Java 25 + Spring Boot 4.1.1** (Gradle, PostgreSQL) para agendar consultas em uma UBS ou clínica, com validação de horários, prioridade de atendimento (idoso, gestante, pessoa com deficiência e casos graves por cor) e busca de endereço por CEP.

Projeto do curso de **Padrões de Projeto** (DIO), desenvolvido com especificação auditável (`onp-spec-driven`): cada critério de aceite tem um teste que o prova.

> **Aviso:** a lista de motivos por gravidade é de **exemplo, não clínica**: uma lista real precisa ser definida por profissional de saúde. O login (fase 2) usa token de 1 hora e **não tem** limite de tentativas nem recuperação automática de senha: não exponha a API na internet sem esses cuidados e sem HTTPS.

## Login e perfis (fase 2)

Todos os endereços exigem um token (`Authorization: Bearer <token>`), exceto `POST /auth/login` e `GET /actuator/health`.

| Perfil | Pode |
|---|---|
| **RECEPCAO** | Tudo: cadastros, agendar, confirmar, cancelar, registrar resultado, agenda do dia e criar a conta do paciente |
| **PACIENTE** | Listar as próprias consultas (`GET /consultas/minhas`), ver e **cancelar só as próprias**, trocar a própria senha |

- A **recepção cria a conta do paciente** (`POST /pacientes/{id}/conta`): o usuário é o CPF e o sistema mostra uma **senha provisória uma única vez**. A troca é recomendada (`POST /auth/senha`).
- A **primeira conta da recepção** nasce das variáveis `RECEPCAO_USUARIO_INICIAL` e `RECEPCAO_SENHA_INICIAL` na primeira subida.
- Senhas guardadas só como hash BCrypt (8 a 72 caracteres). O token é um JWT HS256 assinado com `JWT_SECRET` (mínimo 32 caracteres, só no ambiente); sem ele a aplicação não inicia.

## Padrões de projeto

| Padrão | Onde | Relação com o curso |
|---|---|---|
| **Strategy** | `prioridade/` — cada regra (idoso, gestante, PcD, gravidade) é uma estratégia; a maior prioridade vale | Presente nos labs |
| **Facade** | `AgendamentoFacade` (orquestra todo o agendamento) e `CepFacade` (esconde a chamada ao ViaCEP) | Presente nos labs |
| **Singleton** | beans do Spring (escopo singleton) | Presente nos labs |
| **State** | `StatusConsulta` e `Estado*` — cada status sabe suas transições | Extra |
| **Chain of Responsibility** | `validacao/` — motivo, expediente, conflito, antecedência, limite | Extra |

## Regras de negócio

- **Prioridade:** `URGENTE` (motivo VERMELHO) > `PRIORITARIA` (idoso 60+, gestante, PcD ou motivo AMARELO) > `NORMAL` (VERDE). Prioridades não se somam; vale a maior.
- **Validações do agendamento:** motivo existente → dentro do expediente → sem conflito de horário → antecedência mínima → limite de consultas futuras por paciente.
- **Encaixe:** motivo VERMELHO dispensa antecedência mínima e limite, mas **não** conflito de horário nem expediente.
- **Configuráveis** em `application.yml`: `agendamento.antecedencia-minima-horas` (2) e `agendamento.limite-consultas-futuras` (2).
- **Ciclo da consulta:** `AGENDADA → CONFIRMADA → REALIZADA | FALTOU`; `AGENDADA | CONFIRMADA → CANCELADA`.
- Consultas **ativas** (agendada ou confirmada) ocupam horário. O banco também impede sobreposição para o mesmo profissional.

## Pré-requisitos

- JDK 25 (o Gradle usa o toolchain 25)
- Docker Desktop em execução (banco e testes de integração)
- Node.js 18+ (ponte de testes e skill `onp-spec`)

## Como rodar

1. **Crie o `.env`** a partir do exemplo e defina a senha (o mesmo valor nas duas variáveis):
   ```powershell
   Copy-Item .env.example .env
   ```
   Edite `POSTGRES_PASSWORD` e `DB_PASSWORD` (o mesmo valor), `JWT_SECRET` (32 caracteres ou mais, aleatório) e `RECEPCAO_SENHA_INICIAL` (8 a 72 caracteres). O `.env` não é versionado.

2. **Suba o PostgreSQL:**
   ```powershell
   docker compose up -d
   ```

3. **Carregue as variáveis e rode a aplicação** (no IntelliJ, coloque as mesmas variáveis na configuração de execução):
   ```powershell
   Get-Content .env | Where-Object { $_ -match '^(DB_[A-Z]+|JWT_SECRET|DOCUMENTACAO_ABERTA|RECEPCAO_[A-Z_]+)=(.*)$' } | ForEach-Object { Set-Item "env:$($Matches[1])" $Matches[2] }
   .\gradlew.bat bootRun
   ```
   A API sobe em `http://localhost:8081` (mude com `SERVER_PORT`). Saúde: `http://localhost:8081/actuator/health`.

## Documentação da API (Swagger UI)

A documentação é gerada automaticamente (springdoc-openapi) e vem **fechada por padrão**: exige token como o resto da API. Para usar a página interativa no seu computador, ligue-a no `.env` e reinicie:

```
DOCUMENTACAO_ABERTA=true
```

Depois abra [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html). Para testar os endereços protegidos: faça `POST /auth/login` na própria página, copie o `token` da resposta, clique em **Authorize** e cole o token. O resumo em JSON fica em `/v3/api-docs`. Deixe a opção **desligada** em qualquer ambiente exposto.

## Endpoints

| Método | Caminho | O que faz |
|---|---|---|
| POST | `/auth/login` | Entra com usuário e senha e recebe o token (1 hora). **Sem token** |
| POST | `/auth/senha` | Troca a própria senha (senha atual + nova) |
| POST | `/pacientes/{id}/conta`, `/pacientes/{id}/conta/senha-provisoria` | Recepção cria a conta do paciente / gera nova senha provisória |
| GET | `/consultas/minhas` | Paciente lista as próprias consultas |
| POST / GET | `/pacientes`, `/pacientes/{id}` | Cadastra e consulta paciente (CPF único; `cep` opcional preenche o endereço) |
| POST / GET | `/profissionais`, `/profissionais/{id}` | Cadastra profissional com expediente semanal |
| POST / GET | `/motivos` | Catálogo de motivos com gravidade `VERDE`, `AMARELO` ou `VERMELHO` |
| POST / GET | `/consultas`, `/consultas/{id}` | Agenda e consulta |
| PATCH | `/consultas/{id}/confirmar`, `/cancelar`, `/realizar`, `/faltou` | Muda o status |
| GET | `/agenda?profissionalId=1&data=2026-12-02` | Agenda do dia, da maior para a menor prioridade |

Erros vêm em `application/problem+json` (RFC 9457): 400 dados inválidos, 401 sem login ou token inválido, 403 sem permissão, 404 não encontrado, 409 conflito ou transição inválida, 422 regra de negócio, 503 serviço de CEP indisponível.

Exemplo (PowerShell): entrar como recepção e cadastrar um paciente com CEP:
```powershell
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8081/auth/login -ContentType 'application/json' -Body '{"usuario":"recepcao","senha":"<a RECEPCAO_SENHA_INICIAL>"}'
$cabecalho = @{ Authorization = "Bearer $($login.token)" }
Invoke-RestMethod -Method Post -Uri http://localhost:8081/pacientes -Headers $cabecalho -ContentType 'application/json' -Body '{"nome":"Maria da Silva","cpf":"529.982.247-25","dataNascimento":"1980-05-20","cep":"01001-000"}'
```
Dias da semana do expediente usam os nomes do Java: `MONDAY`, `TUESDAY`, ...

## Testes e verificação da spec

Os testes de integração usam um PostgreSQL descartável (Testcontainers), por isso o Docker precisa estar aberto. Eles não dependem do seu `.env` (a chave do token e o relógio são fixos nos testes):

```powershell
.\gradlew.bat test
```

Prova dos critérios de aceite e auditoria da spec:

```powershell
node .claude/skills/onp-spec-driven/scripts/onp-spec.mjs verify agendamento-consultas
node .claude/skills/onp-spec-driven/scripts/onp-spec.mjs verify autenticacao
node .claude/skills/onp-spec-driven/scripts/onp-spec.mjs verify documentacao-api
node .claude/skills/onp-spec-driven/scripts/onp-spec.mjs audit --ci
```

O `verify` roda `tools/run-gradle-tests.mjs`, que converte o resultado do JUnit para o formato que a skill lê.

## Estrutura

```
src/main/java/br/com/agendamento/consultas/
  paciente/ profissional/ motivo/   cadastros
  consulta/                         agendamento, Facade, State
  prioridade/                       Strategy
  validacao/                        Chain of Responsibility
  agenda/                           agenda do dia
  cep/                              Facade do ViaCEP
  seguranca/ conta/                 login, JWT, perfis e contas (fase 2)
  documentacao/                     OpenAPI / Swagger UI
  erro/                             tratamento de erros (ProblemDetail)
.spec/features/agendamento-consultas/   fase 1: PRD, ADR, TDD, pesquisa, spec e tarefas
.spec/features/autenticacao/            fase 2 (login e permissões): mesma estrutura
.spec/features/documentacao-api/        documentação OpenAPI/Swagger: mesma estrutura
```

## Próximas evoluções (fora de escopo hoje)

Limite de tentativas de login e recuperação de senha, refresh token com revogação, contas adicionais da recepção (administrador), notificações, lista real de motivos revisada por profissional de saúde e outros beneficiários da lei de prioridade (lactantes, criança de colo, TEA, mobilidade reduzida).
