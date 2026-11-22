<!-- onp-spec:documentation-contract=v1 -->

# Spec: Login e permissões (fase 2)

> feature: autenticacao
> status: auditada

## Contexto

Na fase 1 a API é aberta (ASM-007 de [agendamento-consultas](../agendamento-consultas/spec.md)). Esta fase 2 acrescenta **login com token (JWT)** e **dois perfis**: a **recepção**, que faz tudo, e o **paciente**, que vê e cancela só as próprias consultas. A recepção cria a conta do paciente.

## Histórias

### US-007 — Entrar com usuário e senha

Como usuário (recepção ou paciente), quero entrar com meu usuário e senha, para receber um acesso temporário ao sistema.

#### AC-023 — Login correto devolve um token de acesso de 1 hora

- **Dado** uma conta existente com usuário e senha conhecidos
- **Quando** o usuário envia o login com essas credenciais
- **Então** recebe um token do tipo Bearer que vale por 1 hora (resposta 200, com a hora de expiração)
- **E** o token carrega o perfil do usuário (recepção ou paciente)

#### AC-024 — Usuário ou senha errados são recusados sem dar pistas

- **Dado** uma conta existente
- **Quando** alguém tenta entrar com a senha errada, ou com um usuário que não existe
- **Então** o sistema avisa "usuário ou senha inválidos" (resposta 401), com a mesma mensagem nos dois casos

### US-008 — Proteger a API

Como responsável pela unidade, quero que só quem entrou use a API, para que dados de pacientes não fiquem abertos.

#### AC-025 — Sem token, a API recusa

- **Dado** uma requisição sem token a qualquer endereço protegido (por exemplo, consulta de paciente)
- **Quando** ela é enviada
- **Então** o sistema avisa "autenticação necessária" (resposta 401)

#### AC-026 — Token vencido ou adulterado é recusado

- **Dado** um token que já passou de 1 hora, ou que teve o conteúdo alterado
- **Quando** ele é usado em um endereço protegido
- **Então** o sistema avisa "token inválido ou expirado" (resposta 401)

#### AC-027 — Login e saúde continuam abertos

- **Dado** uma requisição sem token
- **Quando** ela vai ao login ou ao endereço de saúde da aplicação
- **Então** a resposta não exige autenticação (login responde 200 ou 401 pelas credenciais; saúde responde UP)

#### AC-038 — Sem chave de assinatura configurada, a aplicação não inicia

- **Dado** que a chave do token (`JWT_SECRET`) está ausente ou tem menos de 32 caracteres
- **Quando** a aplicação tenta subir
- **Então** ela se recusa a iniciar com a mensagem "JWT_SECRET deve ter ao menos 32 caracteres", em vez de usar uma chave fraca

### US-009 — Recepção cria e gerencia a conta do paciente

Como recepcionista, quero criar o acesso de um paciente já cadastrado, para que ele possa consultar e cancelar as próprias consultas.

#### AC-028 — Recepção cria a conta e recebe a senha provisória uma única vez

- **Dado** um paciente cadastrado sem conta
- **Quando** a recepção cria a conta dele
- **Então** o usuário é o CPF do paciente e o sistema mostra uma senha provisória, uma única vez (resposta 201)
- **E** o paciente consegue entrar com essa senha

#### AC-029 — Paciente que já tem conta não ganha uma segunda

- **Dado** um paciente que já tem conta
- **Quando** a recepção tenta criar outra conta para ele
- **Então** o sistema avisa "paciente já possui conta" (resposta 409)

#### AC-030 — Recepção gera nova senha provisória e a antiga deixa de valer

- **Dado** um paciente com conta que esqueceu a senha
- **Quando** a recepção pede uma nova senha provisória
- **Então** o sistema mostra a nova senha, uma única vez
- **E** a senha antiga deixa de funcionar no login

#### AC-039 — Senhas nunca são guardadas nem devolvidas em texto

- **Dado** uma conta criada ou com senha trocada
- **Quando** se olha o que o sistema guardou e o que respondeu
- **Então** só existe o resumo seguro (hash) da senha no banco
- **E** nenhuma resposta da API, exceto a senha provisória no momento em que é gerada, contém a senha

### US-010 — Perfis e permissões

Como responsável pela unidade, quero que cada perfil só faça o que lhe cabe, para evitar acesso indevido.

#### AC-031 — Recepção continua fazendo tudo

- **Dado** um usuário da recepção com token válido
- **Quando** ele cadastra, agenda, confirma, cancela, registra resultado e consulta a agenda
- **Então** todas as operações funcionam como na fase 1 (a recepção não perde nenhuma função)

#### AC-032 — Paciente é barrado onde só a recepção atua

- **Dado** um paciente com token válido
- **Quando** ele tenta cadastrar paciente, profissional ou motivo, agendar, confirmar, realizar, marcar falta ou consultar a agenda do dia
- **Então** o sistema avisa "sem permissão para esta operação" (resposta 403)

#### AC-033 — Paciente vê só as próprias consultas

- **Dado** dois pacientes com consultas
- **Quando** o primeiro lista as próprias consultas, ou tenta ver a consulta do segundo
- **Então** a lista traz só as dele, e ver a do outro é recusado com "sem permissão para esta operação" (resposta 403)

#### AC-034 — Paciente cancela a própria consulta, e só a própria

- **Dado** um paciente com uma consulta agendada e outra consulta de outro paciente
- **Quando** ele cancela a sua, e depois tenta cancelar a do outro
- **Então** a dele passa a CANCELADA e o horário é liberado
- **E** cancelar a do outro é recusado com "sem permissão para esta operação" (resposta 403)

### US-011 — Trocar a própria senha

Como usuário, quero trocar minha senha, para trocar a senha provisória por uma só minha.

#### AC-035 — Usuário troca a própria senha informando a atual

- **Dado** um usuário logado
- **Quando** ele envia a senha atual e uma nova senha válida
- **Então** a troca é aceita (resposta 204)
- **E** o login com a senha nova funciona e com a antiga não

#### AC-036 — Troca com senha atual errada ou senha nova fraca é recusada

- **Dado** um usuário logado
- **Quando** ele informa a senha atual errada, ou uma nova senha com menos de 8 caracteres
- **Então** a atual errada é recusada com "senha atual incorreta" (resposta 422)
- **E** a nova fraca é recusada com aviso de dados inválidos (resposta 400)
- **E** a senha não muda em nenhum dos dois casos

### US-012 — Primeira conta da recepção

Como administrador da instalação, quero que a primeira conta da recepção nasça das variáveis de ambiente, para não haver senha no código nem endpoint aberto.

#### AC-037 — A conta inicial da recepção é criada na primeira subida e não se duplica

- **Dado** que não existe nenhuma conta da recepção e as variáveis de usuário e senha iniciais estão definidas
- **Quando** a aplicação sobe
- **Então** a conta da recepção é criada, e o login com ela funciona
- **E** uma segunda subida não cria outra conta nem altera a senha existente

## Fora de escopo

- Refresh token, logout com revogação de token e login em vários dispositivos controlado pelo servidor.
- Bloqueio por tentativas erradas, recuperação de senha por e-mail/SMS e autenticação em dois fatores (ASM-015).
- Contas adicionais da recepção e perfil de administrador: só existe a conta inicial (ASM-017).
- Login social (Google etc.), HTTPS e CORS (infraestrutura e front-end).
- Mudança no comportamento de negócio dos 22 critérios da feature agendamento-consultas.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-010 | O usuário do paciente é o CPF (11 dígitos); o usuário da recepção é um nome livre (ex.: "recepcao"). | confirmada | Cliente, 2026-11-22: "Confirmo e aprovo tudo (Recomendado)" |
| ASM-011 | Senha com mínimo de 8 e máximo de 72 caracteres (limite do bcrypt); guardada só como hash BCrypt. | confirmada | Cliente, 2026-11-22: "Confirmo e aprovo tudo (Recomendado)" |
| ASM-012 | A senha provisória é gerada pelo sistema (10 caracteres aleatórios) e mostrada uma única vez; a troca é **recomendada, mas não obrigatória** no primeiro acesso. | confirmada | Cliente, 2026-11-22: "Confirmo e aprovo tudo (Recomendado)" |
| ASM-013 | O token é um JWT assinado com HS256 pela chave `JWT_SECRET`, com 1 hora de validade e as informações: usuário, perfil e, para paciente, o id do paciente. Não há refresh nem revogação (sair = descartar o token). | confirmada | Cliente, 2026-11-22: "Confirmo e aprovo tudo (Recomendado)" |
| ASM-014 | Permissões: a recepção faz tudo; o paciente só lista as próprias consultas, vê e cancela as próprias e troca a própria senha. Confirmar, realizar e marcar falta ficam com a recepção. | confirmada | Cliente, 2026-11-22: "Confirmo e aprovo tudo (Recomendado)" |
| ASM-015 | Sem limite de tentativas de login e sem recuperação de senha automática na fase 2: se o paciente esquecer a senha, a recepção gera outra (AC-030). Risco aceito e registrado. | confirmada | Cliente, 2026-11-22: "Confirmo e aprovo tudo (Recomendado)" |
| ASM-016 | A conta inicial da recepção usa `RECEPCAO_USUARIO_INICIAL` e `RECEPCAO_SENHA_INICIAL` (senha com a regra de 8 caracteres) e só é criada se não existir nenhuma conta de recepção. | confirmada | Cliente, 2026-11-22: "Confirmo e aprovo tudo (Recomendado)" |
| ASM-017 | Não há endpoint para criar outras contas da recepção nesta fase; só a conta inicial. | confirmada | Cliente, 2026-11-22: "Confirmo e aprovo tudo (Recomendado)" |
| ASM-018 | Erros de autenticação e permissão seguem o mesmo formato de erro da API (application/problem+json). | confirmada | Cliente, 2026-11-22: "Confirmo e aprovo tudo (Recomendado)" |

## Perguntas em aberto

Nenhuma.

## Documentos relacionados

[PRD](prd.md) · [ADR](adr.md) · [TDD](design.md) · [Tarefas](tasks.md) · [Pesquisa](research.md) · Feature anterior: [agendamento-consultas](../agendamento-consultas/spec.md)
