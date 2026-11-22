<!-- onp-spec:documentation-contract=v1 -->

# Tasks: Documentação da API (OpenAPI/Swagger)

> feature: documentacao-api

<!--
  Status: pendente | em-andamento | concluida
  Atalho: onp-spec tarefa documentacao-api <T-xxx> <status>
  Pacote base: br.com.agendamento.consultas  (src/main/java/br/com/agendamento/consultas)
-->

## T-018 — OpenAPI e Swagger UI com acesso controlado [concluida]

- Refs: US-013, AC-040, AC-041, AC-042
- Arquivos: build.gradle, src/main/resources/application.yml, .env.example, README.md, src/main/java/br/com/agendamento/consultas/documentacao/ConfiguracaoDocumentacao.java, src/main/java/br/com/agendamento/consultas/seguranca/SegurancaConfig.java, src/test/java/br/com/agendamento/consultas/documentacao/DocumentacaoAbertaTest.java, src/test/java/br/com/agendamento/consultas/documentacao/DocumentacaoFechadaTest.java
- Notas: usa a biblioteca springdoc-openapi (versão compatível com o Spring Boot 4.1.1, confirmada na implementação). A flag `documentacao.aberta` (variável `DOCUMENTACAO_ABERTA`, padrão `false`) é lida pela `SegurancaConfig`: só quando ligada, os endereços `/v3/api-docs`, `/swagger-ui.html` e `/swagger-ui/**` ficam abertos (método GET); desligada, seguem a regra geral (exigem token). **Altera um arquivo da feature autenticacao** (`SegurancaConfig`) apenas para acrescentar essa exceção; os 17 critérios de autenticação e os 22 de agendamento continuam provados pelos testes existentes. Decisão do cliente registrada no [PRD](prd.md) e na [spec](spec.md): "Prosseguir (Recomendado)".

## Documentos relacionados

[PRD](prd.md) · [ADR](adr.md) · [TDD](design.md) · [Spec](spec.md) · [Pesquisa](research.md)
