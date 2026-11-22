package br.com.agendamento.consultas.prioridade;

import br.com.agendamento.consultas.motivo.MotivoAtendimento;
import br.com.agendamento.consultas.paciente.Paciente;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Padrão Strategy: cada regra decide sozinha se concede prioridade. Para acrescentar uma regra
 * nova basta criar outro bean; nenhuma regra existente muda.
 */
public interface RegraPrioridade {

    /** Prioridade que esta regra concede, ou vazio se não se aplica. */
    Optional<Prioridade> avaliar(Paciente paciente, MotivoAtendimento motivo, LocalDate dataConsulta);
}
