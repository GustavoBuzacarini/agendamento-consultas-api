package br.com.agendamento.consultas.prioridade;

import br.com.agendamento.consultas.motivo.MotivoAtendimento;
import br.com.agendamento.consultas.paciente.Paciente;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Paciente marcada como gestante no cadastro. */
@Component
public class RegraGestante implements RegraPrioridade {

    @Override
    public Optional<Prioridade> avaliar(Paciente paciente, MotivoAtendimento motivo, LocalDate dataConsulta) {
        return paciente.isGestante() ? Optional.of(Prioridade.PRIORITARIA) : Optional.empty();
    }
}
