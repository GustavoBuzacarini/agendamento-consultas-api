<!-- onp-spec:documentation-contract=v1 -->

# Spec: Agendamento de consultas (UBS / clínica)

> feature: agendamento-consultas
> status: auditada

## Contexto

Unidades de saúde (UBS e clínicas) perdem horários por conflitos de agenda e atendem sem critério claro de prioridade. Esta API permite à **recepção** agendar, confirmar e cancelar consultas e ao **paciente** consultar e cancelar as próprias, aplicando regras de validação e prioridade (idoso, gestante, pessoa com deficiência e casos graves).

## Histórias

### US-001 — Cadastrar pacientes e profissionais

Como recepcionista, quero cadastrar pacientes e profissionais (com o expediente semanal), para poder agendar consultas.

#### AC-001 — Paciente é cadastrado e o CPF não se repete

- **Dado** que não existe paciente com o CPF 529.982.247-25
- **Quando** a recepção cadastra um paciente com esse CPF
- **Então** o paciente é criado (resposta 201)
- **E** um segundo cadastro com o mesmo CPF é recusado com a mensagem "CPF já cadastrado" (resposta 409)

#### AC-002 — Profissional é cadastrado com seu expediente semanal

- **Dado** um profissional com expediente de segunda a sexta, das 08:00 às 12:00, consultas de 30 minutos
- **Quando** a recepção o cadastra
- **Então** o profissional é criado e passa a oferecer horários só dentro desse expediente (resposta 201)

### US-002 — Catalogar motivos de atendimento com gravidade

Como recepcionista, quero escolher o motivo do atendimento a partir de um catálogo em que cada motivo tem um nível de gravidade, para que o sistema saiba quais situações são graves.

#### AC-003 — Motivo de atendimento é cadastrado com sua cor de gravidade

- **Dado** o catálogo de motivos, com gravidade medida por cores: VERDE (normal), AMARELO (intermediária) e VERMELHO (grave)
- **Quando** a recepção cadastra o motivo "Dor no peito" com gravidade VERMELHO
- **Então** o motivo aparece no catálogo com gravidade VERMELHO (resposta 201)
- **E** um motivo sem cor de gravidade, ou com uma cor fora das três, é recusado (resposta 400)

### US-003 — Agendar consulta com validações

Como recepcionista, quero agendar uma consulta e ser avisada quando uma regra impedir, para evitar horários conflitantes e abusos.

#### AC-004 — Agendamento válido cria uma consulta "Agendada"

- **Dado** um paciente, um profissional com horário livre dentro do expediente e motivo cadastrado
- **Quando** a recepção agenda a consulta com a antecedência mínima exigida
- **Então** a consulta é criada com status AGENDADA (resposta 201)

#### AC-005 — Horário já ocupado do médico é recusado

- **Dado** que o profissional já tem consulta ativa às 09:00 de um dia
- **Quando** a recepção tenta agendar outra consulta com ele às 09:00
- **Então** o sistema avisa "horário indisponível" e não cria a consulta (resposta 409)

#### AC-006 — Horário fora do expediente é recusado

- **Dado** um profissional que atende das 08:00 às 12:00
- **Quando** a recepção tenta agendar às 15:00 (ou em um sábado)
- **Então** o sistema avisa "fora do expediente do profissional" (resposta 422)

#### AC-007 — Agendamento sem a antecedência mínima é recusado

- **Dado** uma antecedência mínima configurada de 2 horas e um motivo VERDE ou AMARELO
- **Quando** a recepção tenta agendar para daqui a 1 hora
- **Então** o sistema avisa "antecedência mínima de 2 horas não respeitada" (resposta 422)

#### AC-008 — Paciente acima do limite de consultas futuras é recusado

- **Dado** um limite configurado de 2 consultas futuras por paciente e um paciente que já tem 2 consultas ativas
- **Quando** a recepção tenta agendar a terceira com motivo VERDE ou AMARELO
- **Então** o sistema avisa "limite de consultas futuras atingido" (resposta 422)
- **E** ao mudar o limite na configuração para 3, o mesmo agendamento é aceito

#### AC-009 — Motivo inexistente é recusado

- **Dado** um agendamento que informa um motivo que não existe no catálogo
- **Quando** a recepção envia o pedido
- **Então** o sistema avisa "motivo de atendimento não encontrado" (resposta 404) e nada é criado

### US-004 — Acompanhar o ciclo de vida da consulta

Como recepcionista, quero confirmar, cancelar e registrar o resultado da consulta, para manter a agenda fiel à realidade.

#### AC-010 — Consulta agendada pode ser confirmada

- **Dado** uma consulta AGENDADA
- **Quando** a recepção a confirma
- **Então** o status passa a CONFIRMADA

#### AC-011 — Cancelar uma consulta libera o horário

- **Dado** uma consulta AGENDADA ou CONFIRMADA
- **Quando** ela é cancelada
- **Então** o status passa a CANCELADA
- **E** outro paciente consegue agendar aquele mesmo horário

#### AC-012 — Consulta já realizada não pode ser cancelada

- **Dado** uma consulta REALIZADA
- **Quando** alguém tenta cancelá-la
- **Então** o sistema avisa "transição de status inválida" (resposta 409) e o status continua REALIZADA

#### AC-013 — Só consulta confirmada pode virar "Realizada" ou "Faltou"

- **Dado** uma consulta CONFIRMADA e outra apenas AGENDADA
- **Quando** a recepção marca ambas como REALIZADA
- **Então** a confirmada vira REALIZADA e a agendada é recusada com "transição de status inválida"
- **E** a consulta confirmada também pode ser marcada como FALTOU

### US-005 — Aplicar prioridade de atendimento

Como recepcionista, quero que o sistema reconheça a prioridade do paciente e do caso, para atender primeiro quem precisa.

#### AC-014 — Idoso (60 anos ou mais) recebe prioridade

- **Dado** um paciente com 60 anos completos na data da consulta e motivo VERDE
- **Quando** a consulta é agendada
- **Então** a consulta tem prioridade PRIORITARIA

#### AC-015 — Gestante recebe prioridade

- **Dado** um paciente marcado como gestante e motivo VERDE
- **Quando** a consulta é agendada
- **Então** a consulta tem prioridade PRIORITARIA

#### AC-016 — Pessoa com deficiência recebe prioridade

- **Dado** um paciente marcado como pessoa com deficiência e motivo VERDE
- **Quando** a consulta é agendada
- **Então** a consulta tem prioridade PRIORITARIA

#### AC-017 — Caso vermelho (grave) é urgente e dispensa antecedência e limite

- **Dado** um motivo com gravidade VERMELHO e um paciente já no limite de consultas futuras
- **Quando** a recepção agenda para daqui a 30 minutos, em horário livre dentro do expediente
- **Então** a consulta é criada com prioridade URGENTE (encaixe)
- **E** conflito de horário e expediente continuam sendo verificados

#### AC-018 — A agenda do dia vem ordenada por prioridade

- **Dado** três consultas no mesmo dia: uma comum às 08:00, uma prioritária às 09:00 e uma urgente às 10:00
- **Quando** a recepção consulta a agenda do dia do profissional
- **Então** a lista vem na ordem urgente, prioritária, comum (empates por horário)

#### AC-019 — Caso amarelo (intermediário) é prioritário, mas segue as regras normais

- **Dado** um paciente sem outra prioridade, abaixo do limite de consultas futuras, e um motivo com gravidade AMARELO
- **Quando** a recepção agenda com a antecedência mínima exigida
- **Então** a consulta é criada com prioridade PRIORITARIA
- **E** se o paciente já estiver no limite de consultas futuras, o mesmo pedido é recusado com "limite de consultas futuras atingido" (o amarelo não dispensa as regras)

### US-006 — Preencher o endereço do paciente pelo CEP

Como recepcionista, quero informar só o CEP ao cadastrar o paciente e ter o endereço preenchido automaticamente, para cadastrar mais rápido e sem erros de digitação.

#### AC-020 — CEP válido preenche o endereço do paciente

- **Dado** um CEP existente (01001-000) e o serviço de consulta de CEP (ViaCEP) disponível
- **Quando** a recepção cadastra um paciente informando esse CEP
- **Então** o paciente é criado com logradouro, bairro, cidade e UF vindos da consulta do CEP (resposta 201)
- **E** um paciente cadastrado sem CEP continua sendo aceito, com o endereço em branco

#### AC-021 — CEP inexistente ou mal formatado é recusado

- **Dado** que o CEP 99999-999 não existe
- **Quando** a recepção tenta cadastrar um paciente com esse CEP
- **Então** o sistema avisa "CEP não encontrado" (resposta 422) e o paciente não é criado
- **E** um CEP que não tenha 8 dígitos é recusado com aviso de dados inválidos (resposta 400)

#### AC-022 — Serviço de CEP fora do ar recusa o cadastro com aviso claro

- **Dado** que o serviço de consulta de CEP está indisponível
- **Quando** a recepção tenta cadastrar um paciente informando um CEP
- **Então** o sistema avisa "serviço de consulta de CEP indisponível, tente novamente" (resposta 503)
- **E** nenhum paciente é gravado, para não deixar endereço incompleto sem aviso

## Fora de escopo

- Login, perfis e permissões (Spring Security/JWT) — fase 2; no MVP a API é aberta.
- Visão do médico (marcar atendimento pelo próprio médico) e prontuário.
- Notificações ao paciente (SMS/WhatsApp/e-mail) e lembretes.
- Penalização por faltas, fila de espera automática e remarcação automática.
- Integração com e-SUS/CNS e qualquer sistema externo, **exceto** a consulta de CEP (ViaCEP, US-006); interface web.
- Outros beneficiários da lei de prioridade (lactantes, criança de colo, TEA, mobilidade reduzida): evolução futura (Q-005).
- Cancelamento feito pelo próprio paciente: fase 2, com login (Q-003); no MVP só a recepção cancela.
- Diagnóstico ou triagem clínica: o sistema apenas **usa** a gravidade cadastrada.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-001 | A gravidade é um atributo do **motivo de atendimento** (catálogo cadastrável), medida por **cores**: VERDE (normal), AMARELO (intermediária) e VERMELHO (grave); o sistema não faz diagnóstico. (Ajustada pelo cliente: antes NORMAL/GRAVE.) | confirmada | Cliente, 2026-11-22: "aceito todas, mas alguns ajustes, como a situação NORMAL e GRAVE, acho bom medir com cores, como verde/nomal, amarelo/intermediario e vermelho/grave" |
| ASM-002 | Motivo VERMELHO gera prioridade URGENTE e dispensa a antecedência mínima e o limite de consultas futuras (encaixe), mas respeita conflito e expediente. | confirmada | Cliente, 2026-11-22: "aceito todas" |
| ASM-003 | A antecedência mínima vale só para **agendar**; cancelar é livre até o início da consulta. | confirmada | Cliente, 2026-11-22: "aceito todas" |
| ASM-004 | Ordem de prioridade: URGENTE (VERMELHO) > PRIORITARIA (idoso 60+, gestante, PcD, AMARELO) > NORMAL (VERDE); combinações não somam, vale a maior. | confirmada | Cliente, 2026-11-22: "aceito todas" (AMARELO ⇒ PRIORITARIA é a proposta da equipe, ver ASM-009) |
| ASM-005 | Cada profissional tem consultas de duração fixa (ex.: 30 min) e expediente semanal simples (dias da semana + faixa de horário). | confirmada | Cliente, 2026-11-22: "aceito todas" |
| ASM-006 | Padrões de configuração: antecedência 2 h e limite 2 consultas futuras; ambos em configuração da aplicação. | confirmada | Cliente, 2026-11-22: "aceito todas" |
| ASM-007 | Sem autenticação no MVP; dados pessoais mínimos (nome, CPF, nascimento, flags gestante/PcD). Fuso America/Sao_Paulo. | confirmada | Cliente, 2026-11-22: "aceito todas" |
| ASM-008 | Consulta ATIVA = AGENDADA ou CONFIRMADA; só essas ocupam horário e contam no limite. | confirmada | Cliente, 2026-11-22: "aceito todas" |
| ASM-009 | Com a escala de 3 cores, AMARELO gera prioridade PRIORITARIA (mesmo nível de idoso/gestante/PcD) **sem** dispensar antecedência nem limite; só VERMELHO faz encaixe (AC-019). Proposta da equipe, criada após o ajuste de cores. | confirmada | Cliente, 2026-11-22: "Confirmo ASM-009" |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-001 | Quais motivos de atendimento devem nascer como VERMELHO ou AMARELO (seed inicial) e quem os valida? Recomenda-se que um profissional de saúde revise a lista antes de uso real; no estudo, o seed será de exemplo, marcado como "não clínico". | respondida | Cliente, 2026-11-22, pergunta nativa: "Seed de exemplo basta (Recomendado)". A lista real, revisada por profissional de saúde, fica fora do projeto de estudo. |
| Q-002 | Deve haver mais de dois níveis de gravidade (ex.: escala de cores de classificação de risco)? | respondida | Cliente, 2026-11-22: "acho bom medir com cores, como verde/nomal, amarelo/intermediario e vermelho/grave" → três níveis (VERDE, AMARELO, VERMELHO). |
| Q-003 | O paciente pode cancelar sozinho pela API sem identificação (sem login no MVP)? Proposta: cancelar informando CPF + id da consulta. | respondida | Cliente, 2026-11-22, pergunta nativa: "Só a recepção cancela (Recomendado)". Cancelamento pelo paciente fica para a fase 2, com login. |
| Q-004 | Incluir, como em um laboratório do curso, a busca de endereço do paciente por CEP (ViaCEP) por meio de uma Facade, ligando o projeto ao conteúdo do curso? Proposta: opcional, fase 2 do MVP. | respondida | Cliente, 2026-11-22, pergunta nativa: "Sim, implementar agora (Recomendado)". Falha do serviço: "Recusa o cadastro (Recomendado)" (AC-022). Virou US-006. |
| Q-005 | A lei de atendimento prioritário também lista lactantes, pessoas com criança de colo, pessoas com TEA ou mobilidade reduzida. Incluir no MVP além de idoso, gestante e PcD? Proposta: não, só no futuro. | respondida | Cliente, 2026-11-22, pergunta nativa: "Não, só idoso/gestante/PcD (Recomendado)". |

## Documentos relacionados

[PRD](prd.md) · [ADR](adr.md) · [TDD](design.md) · [Tarefas](tasks.md) · [Pesquisa](research.md)
