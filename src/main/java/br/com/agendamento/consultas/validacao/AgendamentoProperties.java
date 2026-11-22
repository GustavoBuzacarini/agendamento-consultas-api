package br.com.agendamento.consultas.validacao;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Regras configuráveis em application.yml, sob "agendamento" (ASM-006). */
@ConfigurationProperties(prefix = "agendamento")
public record AgendamentoProperties(int antecedenciaMinimaHoras, int limiteConsultasFuturas) {
}
