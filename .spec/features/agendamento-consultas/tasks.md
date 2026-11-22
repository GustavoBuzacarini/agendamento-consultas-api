<!-- onp-spec:documentation-contract=v1 -->

# Tasks: Agendamento de consultas

> feature: agendamento-consultas

<!--
  Status: pendente | em-andamento | concluida
  Atalho: onp-spec tarefa agendamento-consultas <T-xxx> <status>
  Pacote base: br.com.agendamento.consultas  (src/main/java/br/com/agendamento/consultas)
-->

## T-001 — Esqueleto Spring Boot + Gradle + PostgreSQL + ponte de verificação [concluida]

- Refs: US-001
- Arquivos: build.gradle, settings.gradle, docker-compose.yml, src/main/resources/application.yml, src/main/java/br/com/agendamento/consultas/AgendamentoApplication.java, tools/run-gradle-tests.mjs, onpspec.config.json, .gitignore
- Notas: gerar projeto (Java LTS, Spring Boot, Web, Data JPA, Validation, PostgreSQL, Flyway, Actuator). A ponte `tools/run-gradle-tests.mjs` roda `gradlew test` e converte o JUnit XML de `build/test-results/test` para o JSON no formato jest (`reporter: jest-json`, `reporterOutputFile: .spec/verification/raw.json`), pois o motor não lê JUnit direto. Ver [TDD §9](design.md) e [ADR](adr.md).

## T-002 — Cadastro de pacientes [concluida]

- Refs: US-001, AC-001
- Arquivos: src/main/java/br/com/agendamento/consultas/paciente/Paciente.java, src/main/java/br/com/agendamento/consultas/paciente/PacienteRepository.java, src/main/java/br/com/agendamento/consultas/paciente/PacienteService.java, src/main/java/br/com/agendamento/consultas/paciente/PacienteController.java, src/main/resources/db/migration/V1__paciente.sql, src/test/java/br/com/agendamento/consultas/paciente/PacienteTest.java, src/main/java/br/com/agendamento/consultas/erro/RegraDeNegocioException.java, src/main/java/br/com/agendamento/consultas/erro/TratadorDeErros.java, src/test/java/br/com/agendamento/consultas/IntegrationTestBase.java, build.gradle
- Notas: depende de T-001. CPF único e validado. Também cria a base compartilhada: tratador de erros (ProblemDetail, movido da T-008), a base de testes com PostgreSQL via Testcontainers e as dependências de teste no `build.gradle`.

## T-003 — Cadastro de profissionais e expediente [concluida]

- Refs: US-001, AC-002
- Arquivos: src/main/java/br/com/agendamento/consultas/profissional/Profissional.java, src/main/java/br/com/agendamento/consultas/profissional/Expediente.java, src/main/java/br/com/agendamento/consultas/profissional/ProfissionalRepository.java, src/main/java/br/com/agendamento/consultas/profissional/ProfissionalService.java, src/main/java/br/com/agendamento/consultas/profissional/ProfissionalController.java, src/main/resources/db/migration/V2__profissional.sql, src/test/java/br/com/agendamento/consultas/profissional/ProfissionalTest.java
- Notas: depende de T-001.

## T-004 — Catálogo de motivos com gravidade [concluida]

- Refs: US-002, AC-003
- Arquivos: src/main/java/br/com/agendamento/consultas/motivo/MotivoAtendimento.java, src/main/java/br/com/agendamento/consultas/motivo/Gravidade.java, src/main/java/br/com/agendamento/consultas/motivo/MotivoRepository.java, src/main/java/br/com/agendamento/consultas/motivo/MotivoController.java, src/main/resources/db/migration/V3__motivo.sql, src/test/java/br/com/agendamento/consultas/motivo/MotivoTest.java
- Notas: depende de T-001. `Gravidade` é o enum VERDE, AMARELO, VERMELHO (ASM-001). O seed de motivos é de exemplo, marcado "não clínico", até a resposta a Q-001.

## T-005 — Prioridade de atendimento (padrão Strategy) [concluida]

- Refs: US-005, AC-014, AC-015, AC-016
- Arquivos: src/main/java/br/com/agendamento/consultas/prioridade/Prioridade.java, src/main/java/br/com/agendamento/consultas/prioridade/RegraPrioridade.java, src/main/java/br/com/agendamento/consultas/prioridade/RegraIdoso.java, src/main/java/br/com/agendamento/consultas/prioridade/RegraGestante.java, src/main/java/br/com/agendamento/consultas/prioridade/RegraDeficiencia.java, src/main/java/br/com/agendamento/consultas/prioridade/RegraGravidade.java, src/main/java/br/com/agendamento/consultas/prioridade/CalculadoraPrioridade.java, src/test/java/br/com/agendamento/consultas/prioridade/PrioridadeTest.java
- Notas: depende de T-002 e T-004 (tipos Paciente e Gravidade). Cada regra é um bean (Singleton do Spring) injetado como `List<RegraPrioridade>` (padrão **Strategy**, como no lab do curso); vale a maior prioridade. VERMELHO ⇒ URGENTE; AMARELO, idoso, gestante, PcD ⇒ PRIORITARIA (ASM-004, ASM-009). AC-017 e AC-019 dependem também da API de agendamento e são provados na T-008 (aqui só se testa a regra de prioridade, sem tag de critério).

## T-006 — Validações de agendamento (Chain of Responsibility) [concluida]

- Refs: US-003, AC-005, AC-006, AC-007, AC-008, AC-009
- Arquivos: src/main/java/br/com/agendamento/consultas/validacao/ValidadorAgendamento.java, src/main/java/br/com/agendamento/consultas/validacao/CadeiaDeValidacao.java, src/main/java/br/com/agendamento/consultas/validacao/ValidaMotivoExistente.java, src/main/java/br/com/agendamento/consultas/validacao/ValidaExpediente.java, src/main/java/br/com/agendamento/consultas/validacao/ValidaConflitoHorario.java, src/main/java/br/com/agendamento/consultas/validacao/ValidaAntecedenciaMinima.java, src/main/java/br/com/agendamento/consultas/validacao/ValidaLimiteConsultas.java, src/main/java/br/com/agendamento/consultas/validacao/AgendamentoProperties.java, src/main/java/br/com/agendamento/consultas/validacao/ConfiguracaoAgendamento.java, src/main/java/br/com/agendamento/consultas/validacao/ContextoAgendamento.java, src/main/java/br/com/agendamento/consultas/validacao/AgendaConsultas.java, src/test/java/br/com/agendamento/consultas/validacao/ValidacaoTest.java
- Notas: depende de T-003 e T-004. Elos de antecedência e limite pulam só quando a gravidade é VERMELHO (AC-017, ASM-002); AMARELO segue as regras (AC-019). Os elos são classes comuns (não beans): consultam as consultas existentes pela interface `AgendaConsultas`, implementada na T-008, que também monta a `CadeiaDeValidacao` como bean. Aqui a cadeia é testada sem banco.

## T-007 — Ciclo de vida da consulta (padrão State) [concluida]

- Refs: US-004, AC-010, AC-012, AC-013
- Arquivos: src/main/java/br/com/agendamento/consultas/consulta/StatusConsulta.java, src/main/java/br/com/agendamento/consultas/consulta/EstadoConsulta.java, src/main/java/br/com/agendamento/consultas/consulta/EstadoAgendada.java, src/main/java/br/com/agendamento/consultas/consulta/EstadoConfirmada.java, src/main/java/br/com/agendamento/consultas/consulta/EstadoTerminal.java, src/main/java/br/com/agendamento/consultas/consulta/TransicaoInvalidaException.java, src/test/java/br/com/agendamento/consultas/consulta/EstadoConsultaTest.java
- Notas: depende de T-002 (RegraDeNegocioException). Estados AGENDADA, CONFIRMADA, REALIZADA, CANCELADA, FALTOU; cada estado define as transições permitidas. O AC-011 ("libera o horário") só pode ser provado na T-008, por isso saiu das Refs daqui.

## T-008 — Agendamento: serviço e API REST [concluida]

- Refs: US-003, AC-004, AC-011, AC-017, AC-019
- Arquivos: src/main/java/br/com/agendamento/consultas/validacao/ConfiguracaoValidacao.java, src/main/java/br/com/agendamento/consultas/consulta/AgendaConsultasJpa.java, src/test/java/br/com/agendamento/consultas/RelogioDeTeste.java, src/test/java/br/com/agendamento/consultas/IntegrationTestBase.java, src/main/java/br/com/agendamento/consultas/consulta/AgendamentoFacade.java, src/main/java/br/com/agendamento/consultas/consulta/Consulta.java, src/main/java/br/com/agendamento/consultas/consulta/ConsultaRepository.java, src/main/java/br/com/agendamento/consultas/consulta/AgendamentoService.java, src/main/java/br/com/agendamento/consultas/consulta/ConsultaController.java, src/main/resources/db/migration/V4__consulta.sql, src/test/java/br/com/agendamento/consultas/consulta/AgendamentoApiTest.java
- Notas: depende de T-005, T-006 e T-007. Implementa `AgendaConsultas` sobre o `ConsultaRepository`, registra os elos e a `CadeiaDeValidacao` como beans (ordem: motivo, expediente, conflito, antecedência, limite) e registra `StatusConsulta` na entidade. Erros em ProblemDetail (RFC 9457) com as mensagens da spec. `AgendamentoFacade` é a **Facade** (padrão do curso): o controller chama um único ponto que orquestra a cadeia de validação, a prioridade, o estado e a persistência.

## T-009 — Agenda do dia ordenada por prioridade [concluida]

- Refs: US-005, AC-018
- Arquivos: src/main/java/br/com/agendamento/consultas/agenda/ItemDaAgenda.java, src/main/java/br/com/agendamento/consultas/agenda/AgendaController.java, src/main/java/br/com/agendamento/consultas/agenda/AgendaService.java, src/test/java/br/com/agendamento/consultas/agenda/AgendaTest.java
- Notas: depende de T-008.

## T-010 — Endereço do paciente pelo CEP (ViaCEP com Facade) [concluida]

- Refs: US-006, AC-020, AC-021, AC-022
- Arquivos: src/main/java/br/com/agendamento/consultas/cep/Endereco.java, src/main/java/br/com/agendamento/consultas/cep/CepClient.java, src/main/java/br/com/agendamento/consultas/cep/ViaCepClient.java, src/main/java/br/com/agendamento/consultas/cep/CepFacade.java, src/main/resources/db/migration/V5__paciente_endereco.sql, src/main/resources/application.yml, src/main/java/br/com/agendamento/consultas/paciente/Paciente.java, src/main/java/br/com/agendamento/consultas/paciente/PacienteService.java, src/main/java/br/com/agendamento/consultas/paciente/PacienteController.java, src/test/java/br/com/agendamento/consultas/cep/CepClientDeTeste.java, src/test/java/br/com/agendamento/consultas/cep/ViaCepClientTest.java, src/test/java/br/com/agendamento/consultas/paciente/PacienteCepTest.java, src/test/java/br/com/agendamento/consultas/IntegrationTestBase.java
- Notas: depende de T-002. Padrão **Facade** do lab do curso: `CepFacade` esconde a chamada HTTP ao ViaCEP, a conversão da resposta e os erros atrás de uma operação simples. Resposta do cliente à Q-004: implementar agora; se o serviço cair, recusar o cadastro (AC-022). Os testes de API usam um `CepClient` de teste (sem internet); o `ViaCepClient` real é testado contra um servidor HTTP local.

## Documentos relacionados

[PRD](prd.md) · [ADR](adr.md) · [TDD](design.md) · [Spec](spec.md) · [Pesquisa](research.md)
