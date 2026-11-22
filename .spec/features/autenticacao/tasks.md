<!-- onp-spec:documentation-contract=v1 -->

# Tasks: Login e permissões (fase 2)

> feature: autenticacao

<!--
  Status: pendente | em-andamento | concluida
  Atalho: onp-spec tarefa autenticacao <T-xxx> <status>
  Pacote base: br.com.agendamento.consultas  (src/main/java/br/com/agendamento/consultas)
  Códigos globais: as tarefas continuam a numeração da feature agendamento-consultas (T-001 a T-010).
-->

## T-011 — Segurança base: Spring Security, JWT e testes autenticados [concluida]

- Refs: US-008, AC-025, AC-026, AC-027, AC-038
- Arquivos: build.gradle, src/main/resources/application.yml, .env.example, src/main/java/br/com/agendamento/consultas/seguranca/SegurancaConfig.java, src/main/java/br/com/agendamento/consultas/seguranca/JwtProperties.java, src/main/java/br/com/agendamento/consultas/seguranca/JwtService.java, src/main/java/br/com/agendamento/consultas/seguranca/RespostasDeErroDeSeguranca.java, src/main/java/br/com/agendamento/consultas/seguranca/Perfil.java, src/main/java/br/com/agendamento/consultas/seguranca/UsuarioLogado.java, src/test/java/br/com/agendamento/consultas/IntegrationTestBase.java, src/test/java/br/com/agendamento/consultas/seguranca/TokenPadraoDeTeste.java, src/test/java/br/com/agendamento/consultas/seguranca/SegurancaTest.java, src/test/java/br/com/agendamento/consultas/seguranca/JwtPropertiesTest.java
- Notas: protege todos os endereços, menos `POST /auth/login` e `GET /actuator/health`. Sem sessão (stateless), JWT HS256 pela chave `JWT_SECRET` (sem valor padrão, P-002). O decodificador usa o mesmo `Clock` da aplicação, para os testes com relógio fixo não vencerem. A `IntegrationTestBase` passa a mandar, por padrão, o token da recepção, para os 22 critérios antigos continuarem provando o mesmo comportamento; há um `MockMvc` anônimo para os testes sem token. Ver [TDD](design.md) e [ADR](adr.md).

## T-012 — Conta de usuário e login [concluida]

- Refs: US-007, AC-023, AC-024, AC-039
- Arquivos: src/main/resources/db/migration/V6__usuario.sql, src/main/java/br/com/agendamento/consultas/conta/Usuario.java, src/main/java/br/com/agendamento/consultas/conta/UsuarioRepository.java, src/main/java/br/com/agendamento/consultas/conta/AuthService.java, src/main/java/br/com/agendamento/consultas/conta/AuthController.java, src/test/java/br/com/agendamento/consultas/conta/LoginTest.java
- Notas: depende de T-011. Senha só como hash BCrypt (custo 12). Mesma mensagem para usuário inexistente e senha errada (sem pistas).

## T-013 — Contas de pacientes criadas pela recepção [concluida]

- Refs: US-009, AC-028, AC-029, AC-030
- Arquivos: src/main/java/br/com/agendamento/consultas/conta/SenhaProvisoria.java, src/main/java/br/com/agendamento/consultas/conta/ContaPacienteService.java, src/main/java/br/com/agendamento/consultas/conta/ContaPacienteController.java, src/test/java/br/com/agendamento/consultas/conta/ContaPacienteTest.java
- Notas: depende de T-012. `POST /pacientes/{id}/conta` e `POST /pacientes/{id}/conta/senha-provisoria`, só para a recepção. A senha provisória é gerada com `SecureRandom` e mostrada uma única vez.

## T-014 — Permissões por perfil e consultas do paciente [concluida]

- Refs: US-010, AC-031, AC-032, AC-033, AC-034
- Arquivos: src/main/java/br/com/agendamento/consultas/consulta/AgendamentoFacade.java, src/main/java/br/com/agendamento/consultas/consulta/ConsultaController.java, src/main/java/br/com/agendamento/consultas/consulta/AgendamentoService.java, src/main/java/br/com/agendamento/consultas/consulta/ConsultaRepository.java, src/test/java/br/com/agendamento/consultas/seguranca/PermissoesTest.java
- Notas: depende de T-012 e T-013. **Altera arquivos da feature agendamento-consultas** (ConsultaController, AgendamentoService, ConsultaRepository) apenas para acrescentar `GET /consultas/minhas` e a checagem de dono; o comportamento dos 22 critérios anteriores não muda e segue provado pelos testes existentes. Regras de acesso por perfil na `SegurancaConfig` (T-011).

## T-015 — Troca da própria senha [concluida]

- Refs: US-011, AC-035, AC-036
- Arquivos: src/main/java/br/com/agendamento/consultas/conta/AuthService.java, src/main/java/br/com/agendamento/consultas/conta/AuthController.java, src/test/java/br/com/agendamento/consultas/conta/TrocaDeSenhaTest.java
- Notas: depende de T-012. `POST /auth/senha` com a senha atual e a nova (8 a 72 caracteres).

## T-016 — Conta inicial da recepção [concluida]

- Refs: US-012, AC-037
- Arquivos: src/main/java/br/com/agendamento/consultas/conta/ContaInicialRecepcao.java, src/main/java/br/com/agendamento/consultas/conta/ContaInicialProperties.java, src/test/java/br/com/agendamento/consultas/conta/ContaInicialTest.java
- Notas: depende de T-012. Executa na subida, só se não existir conta de recepção e as variáveis estiverem definidas; sem elas, registra aviso no log e a aplicação sobe sem conta (nenhum usuário consegue entrar até a configuração).

## T-017 — Documentação e configuração de ambiente [concluida]

- Refs: US-007, AC-023
- Arquivos: README.md, .env.example
- Notas: depende de T-011 a T-016. Atualiza o README (login, perfis, endpoints novos, variáveis `JWT_SECRET`, `RECEPCAO_USUARIO_INICIAL`, `RECEPCAO_SENHA_INICIAL`) e remove o aviso de "API sem autenticação".

## Documentos relacionados

[PRD](prd.md) · [ADR](adr.md) · [TDD](design.md) · [Spec](spec.md) · [Pesquisa](research.md)
