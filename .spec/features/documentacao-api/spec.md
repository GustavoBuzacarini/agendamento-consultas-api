<!-- onp-spec:documentation-contract=v1 -->

# Spec: Documentação da API (OpenAPI/Swagger)

> feature: documentacao-api
> status: auditada

## Contexto

Quem usa a API (a recepção, um aluno avaliando o projeto, um futuro front-end) precisa ver quais endereços existem e testá-los sem montar requisições à mão. Esta feature gera a **documentação OpenAPI** e a página interativa **Swagger UI**. Por segurança, a documentação vem **fechada por padrão** (exige login como o resto da API) e só abre sem token quando `DOCUMENTACAO_ABERTA=true`.

Origem das decisões (respostas do cliente, 2026-11-22): "Documentação da API (OpenAPI/Swagger)", "Fechada por padrão (Recomendado)" e, para o conflito com AC-025 e AC-027 da feature [autenticacao](../autenticacao/spec.md), "Prosseguir (Recomendado)".

## Histórias

### US-013 — Consultar e testar a API por uma página de documentação

Como pessoa que usa ou avalia a API, quero uma página que liste os endereços e me deixe testá-los com meu token, para entender e experimentar a API sem escrever requisições à mão.

#### AC-040 — A documentação lista os endereços e prevê o uso do token

- **Dado** que a documentação está aberta (`DOCUMENTACAO_ABERTA=true`)
- **Quando** alguém pede o resumo da API (`/v3/api-docs`) sem token
- **Então** recebe o resumo (resposta 200) com o título "API de Agendamento de Consultas"
- **E** o resumo lista os endereços principais (login, pacientes, consultas e agenda)
- **E** declara a autenticação por token Bearer (JWT), para a página oferecer o botão "Authorize"

#### AC-041 — A página interativa abre sem login quando a documentação está aberta

- **Dado** que a documentação está aberta
- **Quando** alguém abre a página interativa (Swagger UI) sem token
- **Então** a página carrega (resposta 200)

#### AC-042 — Por padrão a documentação exige login

- **Dado** que a documentação **não** foi aberta (configuração padrão)
- **Quando** alguém pede o resumo da API ou a página interativa sem token
- **Então** o sistema avisa "autenticação necessária" (resposta 401)
- **E** com o token da recepção, o resumo da API é entregue (resposta 200)

## Fora de escopo

- Exemplos de requisição detalhados para cada endereço e versionamento da API.
- Publicar a documentação em site externo.
- Alterar o comportamento de qualquer regra de negócio ou de segurança já provada nas features anteriores.

## Suposições

Nenhuma.

## Perguntas em aberto

Nenhuma.

## Documentos relacionados

[PRD](prd.md) · [ADR](adr.md) · [TDD](design.md) · [Tarefas](tasks.md) · [Pesquisa](research.md) · Features relacionadas: [autenticacao](../autenticacao/spec.md) · [agendamento-consultas](../agendamento-consultas/spec.md)
