package br.com.agendamento.consultas.profissional;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** Uma faixa de atendimento semanal, por exemplo segunda-feira das 08:00 às 12:00. */
@Embeddable
public record Expediente(
        @NotNull @Enumerated(EnumType.STRING) DayOfWeek diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFim) {
}
