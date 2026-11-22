<!-- onp-spec:documentation-contract=v1 -->

# PRD: Login e permissões (fase 2)

> feature: autenticacao
> status: revisado

[ADR](adr.md) · [TDD](design.md) · [Spec](spec.md) · [Tarefas](tasks.md) · [Pesquisa](research.md)

## Pedido original do cliente

> Fase 2: login (Recomendado). Perfis: Recepção e Paciente (Recomendado). Conta do paciente: Recepção cria a conta (Recomendado). Token: Só token de acesso, 1 hora (Recomendado). Primeira conta da recepção: Variáveis de ambiente (Recomendado). Origem: respostas do cliente às perguntas nativas de 2026-11-22; a Fase 2 vem da seção 'Fase 2' do README e do pedido anterior de login com Spring Security/JWT e cancelamento pelo próprio paciente.

Respostas posteriores, literais (2026-11-22):
- Conflito com a regra "sem autenticação" da feature anterior: "Prosseguir (Recomendado)".

## Problema, público e contexto

**Palavras do cliente:** a fase 2 é o login; a recepção cria a conta do paciente; o paciente vê e cancela só as próprias consultas.

**Interpretação da equipe (a validar):** na fase 1 a API é aberta e guarda dados pessoais e de saúde (CPF, gestante, PcD, endereço). Sem login, qualquer pessoa com acesso à rede lê e altera a agenda. O login fecha essa brecha e permite o autoatendimento mínimo do paciente.

## Resultados desejados e métricas

Sem linha de base de uso. Resultado verificável: todos os critérios da [spec](spec.md) (AC-023 a AC-039) com prova PASS e nenhuma rota de dados acessível sem token. Métricas de produto (hipóteses, a medir em uso real): pacientes que cancelam pelo sistema em vez de pela recepção; consultas liberadas por cancelamento antecipado.

## Requisitos e prioridades

| Requisito | Palavras do cliente / origem | Prioridade | Critérios na spec |
|---|---|---|---|
| Login com token de 1 hora | "Token: Só token de acesso, 1 hora (Recomendado)" | Alta | AC-023, AC-024 |
| Proteger todos os endereços | "Fase 2: login (Recomendado)"; "Prosseguir (Recomendado)" | Alta | AC-025, AC-026, AC-027, AC-038 |
| Perfis Recepção e Paciente | "Perfis: Recepção e Paciente (Recomendado)" | Alta | AC-031, AC-032 |
| Recepção cria a conta do paciente | "Conta do paciente: Recepção cria a conta (Recomendado)" | Alta | AC-028, AC-029, AC-030 |
| Paciente vê e cancela só as próprias consultas | pedido anterior da fase 2 e opção "Recepção e Paciente" | Alta | AC-033, AC-034 |
| Troca da própria senha | interpretação da equipe (senha provisória) | Média | AC-035, AC-036 |
| Primeira conta pela configuração do ambiente | "Primeira conta da recepção: Variáveis de ambiente (Recomendado)" | Alta | AC-037 |
| Senha nunca em texto | interpretação da equipe (boa prática de segurança) | Alta | AC-039 |

## Escopo e exclusões

Entra: o que está nas histórias US-007 a US-012. Não entra (ver [spec](spec.md)): refresh token, revogação, bloqueio por tentativas, recuperação por e-mail, dois fatores, contas extras da recepção, login social, HTTPS e CORS.

## Regras de produto e domínios

- Domínio novo: contas e acesso. Domínio anterior afetado: **agendamento-consultas** (todos os seus endereços passam a exigir token; ganha `GET /consultas/minhas` e checagem de dono).
- Contrato anterior substituído **para esta fase**: ASM-007 "Sem autenticação no MVP" e a linha "Login" em "Fora de escopo" da [spec anterior](../agendamento-consultas/spec.md), além de "Sem login" do PRD anterior. Os documentos originais **não são alterados**; este PRD os supera para a fase 2.
- Q-003 da feature anterior ("só a recepção cancela") vale para a fase 1; na fase 2 o paciente cancela a própria consulta (AC-034), conforme a opção "Recepção e Paciente".
- Invariante: o comportamento de negócio dos 22 critérios anteriores não muda.

## Dependências, riscos e suposições

- Dados pessoais e de saúde: LGPD; senhas só como hash; segredo do token só no ambiente (P-002).
- Riscos aceitos: sem limite de tentativas nem recuperação automática (ASM-015); token sem revogação (ASM-013); troca da senha provisória não obrigatória (ASM-012).
- Suposições ASM-010 a ASM-018 na [spec](spec.md).

## Validação com o cliente

Respostas acima registradas literalmente. Em 2026-11-22 o cliente confirmou ASM-010 a ASM-018 e aprovou os documentos ("Confirmo e aprovo tudo (Recomendado)").
