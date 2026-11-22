<!-- onp-spec:documentation-contract=v1 -->

# PRD: Agendamento de consultas (UBS / clínica)

> feature: agendamento-consultas
> status: revisado

[ADR](adr.md) · [TDD](design.md) · [Spec](spec.md) · [Tarefas](tasks.md) · [Pesquisa](research.md)

## Pedido original do cliente

> bom, vou querer o #4 (Agendamento de Consultas em UBS / Clínica) ... banco pode usar o PostgreSQL e o Grandle [Gradle]. Contexto: projeto Java/Spring Boot do curso DIO de padrões de projeto; ideia com impacto real e escopo não muito complexo.

Origem: conversa com o cliente em 2026-11-21/22. Respostas posteriores (pergunta nativa, 2026-11-22), literais:

- Atores: "Recepção + Paciente (Recomendado)".
- Autenticação: "Sem login (Recomendado)".
- Prioridade: "Idoso 60+, Gestante, Pessoa com deficiencia e tambem casos muitos graves, importante o sistema saber qual situações sao graves ou nao."
- Validações: "Acho insteressante o Conflito de horário do médico, limite de consultas ser configuravel, e a regra da Antecedencia mínima."

Ajuste e aceite (2026-11-22), literais:

- "aceito todas, mas alguns ajustes, como a situação NORMAL e GRAVE, acho bom medir com cores, como verde/nomal, amarelo/intermediario e vermelho/grave"
- "sobre os pontos de atenção faz o que é recomendado e sempre relacionando com o que o curso quer"

## Problema, público e contexto

**Palavras do cliente:** agendamento de consultas em UBS/clínica, com impacto real, sem muita complexidade.

**Interpretação da equipe (a validar):** unidades de saúde têm conflitos de agenda e atendem sem critério claro de prioridade. Quem usa: recepção (opera a agenda) e paciente (consulta/cancela). O projeto também é um exercício do curso, então os padrões State, Chain of Responsibility e Strategy devem aparecer onde fazem sentido.

## Resultados desejados e métricas

Sem linha de base: não há dados da unidade. Métricas de produto ficam como **hipóteses** (ex.: zero agendamentos com conflito de horário; 100% das consultas com prioridade calculada) e só podem ser medidas em uso real. Para o projeto de estudo, o resultado verificável é: todos os critérios de aceite da [spec](spec.md) com prova PASS em `onp-spec verify`.

## Requisitos e prioridades

| Requisito | Palavras do cliente / origem | Prioridade | Critérios na spec |
|---|---|---|---|
| Cadastrar pacientes e profissionais | "Recepção + Paciente" | Alta | AC-001, AC-002 |
| Catálogo de motivos com gravidade por cores (verde, amarelo, vermelho) | "o sistema saber qual situações sao graves ou nao"; "verde/nomal, amarelo/intermediario e vermelho/grave" | Alta | AC-003, AC-017, AC-019 |
| Padrões do curso (Singleton, Strategy, Facade) | "sempre relacionando com o que o curso quer" | Alta | [TDD §5](design.md) |
| Evitar conflito de horário do médico | "Conflito de horário do médico" | Alta | AC-005, AC-006 |
| Limite de consultas configurável | "limite de consultas ser configuravel" | Média | AC-008 |
| Antecedência mínima | "regra da Antecedencia mínima" | Média | AC-007 |
| Prioridade: idoso, gestante, PcD, grave | "Idoso 60+, Gestante, Pessoa com deficiencia e ... casos muitos graves" | Alta | AC-014 a AC-019 |
| Ciclo de vida da consulta | interpretação da equipe (agendada, confirmada, cancelada, realizada, falta) | Alta | AC-010 a AC-013 |
| Endereço do paciente pelo CEP (ViaCEP, Facade do curso); se o serviço cair, recusar o cadastro | Q-004: "Sim, implementar agora (Recomendado)"; falha: "Recusa o cadastro (Recomendado)" (2026-11-22) | Média | AC-020, AC-021, AC-022 |
| PostgreSQL e Gradle | "PostgreSQL e o Grandle" | Restrição técnica | [ADR](adr.md) |

## Escopo e exclusões

Entra: API REST Java/Spring Boot com os critérios da spec. Não entra (ver [spec](spec.md)): login, visão do médico, notificações, penalização por faltas, integrações externas, interface web, triagem clínica.

## Regras de produto e domínios

- Prioridade legal: idosos (60+), gestantes e pessoas com deficiência têm atendimento prioritário (Lei 10.048/2000, consultada em 2026-11-22; ver [pesquisa](research.md)). A lei lista outros beneficiários (Q-005).
- Gravidade é dado **cadastrado e validado por profissional de saúde**; o sistema não diagnostica.
- Domínios: paciente, profissional/agenda, motivo, consulta, prioridade. Nenhum PRD/ADR anterior (projeto novo).

## Dependências, riscos e suposições

- Risco clínico: lista de motivos graves mal definida pode priorizar errado → Q-001 da spec; revisão por profissional de saúde antes de uso real.
- Dados pessoais de saúde (CPF, gestante, PcD) → ver [TDD §8](design.md); sem autenticação o MVP **não é adequado a produção**.
- ASM-001 a ASM-008 confirmadas pelo cliente em 2026-11-22. Abertas na [spec](spec.md): ASM-009, Q-001, Q-003, Q-004, Q-005.

## Validação com o cliente

Respostas acima registradas literalmente. O cliente aceitou as suposições ASM-001 a ASM-008 e ajustou a gravidade para três cores. Em 2026-11-22 o cliente confirmou a ASM-009 ("Confirmo ASM-009") e aprovou os quatro documentos ("Aprovo os 4"). Perguntas Q-001, Q-003, Q-004 e Q-005 seguem abertas e não bloqueiam o MVP.
