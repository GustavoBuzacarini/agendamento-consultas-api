package br.com.agendamento.consultas.agenda;

import br.com.agendamento.consultas.consulta.StatusConsulta;
import br.com.agendamento.consultas.motivo.Gravidade;
import br.com.agendamento.consultas.prioridade.Prioridade;
import java.time.LocalDateTime;

/** Uma linha da agenda do dia. */
public record ItemDaAgenda(Long consultaId, LocalDateTime inicio, LocalDateTime fim, String paciente,
                           String motivo, Gravidade gravidade, Prioridade prioridade, StatusConsulta status) {
}
