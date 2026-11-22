<!-- onp-spec:documentation-contract=v1 -->

# Pesquisa de produto: agendamento de consultas

> feature: agendamento-consultas
> status: revisado

[PRD](prd.md) · [ADR](adr.md) · [TDD](design.md) · [Spec](spec.md) · [Tarefas](tasks.md)

## Pergunta e hipótese

Pergunta: que regras de agendamento, prioridade e gravidade fazem sentido para uma API de MVP de UBS/clínica e como o projeto se liga ao que o curso pede? Hipótese (falsificável): conflito de horário, antecedência, limite por paciente, prioridade legal e gravidade por cores cobrem o essencial sem integração externa. A evidência pode mudar quais regras entram no MVP.

Consulta feita em 2026-11-22 por busca web e leitura dos repositórios do curso. Resultados de busca são resumos, não leitura integral das normas; onde afeta regra, está marcado.

## Concorrentes e alternativas

| Produto / alternativa | Comportamento observado | Fonte primária | Data da consulta | Limitações |
|---|---|---|---|---|
| Planilha / agenda de papel (situação atual provável) | Não observado; hipótese de conflitos e falta de prioridade explícita | — | — | Sem dados da unidade |
| Sistemas do SUS (ex.: e-SUS APS) | Não consultado | — | — | Fora do escopo copiar |
| Classificação de risco por cores no SUS | Há modelos de 4 cores (vermelho, amarelo, verde, azul) e de 5 cores com laranja (Manchester); a Atenção Básica (Cadernos de Atenção Básica nº 28) usa vermelho (imediato), amarelo (risco moderado) e verde (mesmo dia), conforme resumo da busca | [UNA-SUS](https://moodle.unasus.gov.br/vitrine29/mod/resource/view.php?id=3625), [Sec. Saúde SP](https://homocdr.saude.sp.gov.br/wp-content/uploads/2022/08/CLASSIFICACAO-DE-RISCO-12.8.22.pdf) | 2026-11-22 | Tempos-alvo variam por protocolo; vale o do município/estado |
| Fazer nada | Mantém o problema | — | — | — |

## Evidências e dados

- **Gravidade em 3 cores:** a escala do cliente (verde, amarelo, vermelho) coincide com a usada na Atenção Básica, citada acima (observação a partir do resumo da busca, não do texto integral do caderno). É um **encaixe bom para UBS**, mas é uma simplificação: protocolos de pronto-atendimento usam mais cores. Esta decisão é do cliente (Q-002 respondida).
- **Prioridade legal:** a Lei 10.048/2000 garante atendimento prioritário a pessoas com deficiência, idosos (60 anos após a Lei 10.741/2003), gestantes, lactantes e pessoas com criança de colo; alterações posteriores (Lei 14.626/2023) incluíram pessoas com TEA ou mobilidade reduzida e doadores de sangue ([texto no Planalto](https://planalto.gov.br/ccivil_03/leis/L10048.htm), via resumo de busca em 2026-11-22). O MVP cobre só idoso, gestante e PcD, como pediu o cliente; os demais estão em Q-005.
- **Curso (DIO):** os repositórios [lab-padroes-projeto-java](https://github.com/digitalinnovationone/lab-padroes-projeto-java) e [lab-padroes-projeto-spring](https://github.com/digitalinnovationone/lab-padroes-projeto-spring), lidos em 2026-11-22, trabalham **Singleton, Strategy e Facade** (em Spring: Singleton, Strategy/Repository e Facade com consulta a ViaCEP). O projeto cobre os três e acrescenta State e Chain of Responsibility.
- **Dados da unidade:** nenhum fornecido. Frequência de conflitos e faltas: **inconclusivo**. Coleta proposta se for a uso real: 4 semanas registrando agendamentos totais, conflitos evitados, faltas e cancelamentos (numerador/denominador por semana).
- **Limite:** a lista de motivos por cor **não** vem de fonte clínica; precisa de profissional de saúde (Q-001).

## Viabilidade matemática e estatística

Não aplicável ao MVP de estudo: sem dados de custo/benefício. Volume esperado (hipótese): dezenas de agendamentos/dia por unidade, muito abaixo da capacidade de um PostgreSQL único; orçamento em [TDD §16](design.md).

## Proposta e alternativas ao pedido

- Manter o escopo pedido, com gravidade por cores como catálogo cadastrável (ASM-001).
- Opção ligada ao curso (Q-004): busca de endereço por CEP (ViaCEP) por meio de Facade, como no lab Spring. Útil para o cadastro e para fixar o conteúdo, mas amplia o escopo: **só entra com resposta do cliente**.
- Alternativa de gravidade: escala de 4 ou 5 cores como nos protocolos hospitalares; descartada pelo cliente por ora.

## Decisão e experimento

Decisões do cliente (respostas literais no [PRD](prd.md)): prioridade e validações, escala de 3 cores, aplicar o recomendado e relacionar ao curso. Experimento de validação: demonstrar os critérios da spec por testes (`onp-spec verify`). Pendentes: ASM-009, Q-001, Q-003, Q-004, Q-005.
