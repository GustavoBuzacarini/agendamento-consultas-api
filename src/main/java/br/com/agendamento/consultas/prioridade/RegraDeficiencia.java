package br.com.agendamento.consultas.prioridade;

import br.com.agendamento.consultas.motivo.MotivoAtendimento;
import br.com.agendamento.consultas.paciente.Paciente;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Paciente marcado como pessoa com deficiência no cadastro. */
@Component
public class RegraDeficiencia implements RegraPrioridade {

    @Override
    public Optional<Prioridade> avaliar(Paciente paciente, MotivoAtendimento motivo, LocalDate dataConsulta) {
        return paciente.isPessoaComDeficiencia() ? Optional.of(Prioridade.PRIORITARIA) : Optional.empty();
    }
}
