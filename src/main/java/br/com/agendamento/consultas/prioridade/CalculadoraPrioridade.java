package br.com.agendamento.consultas.prioridade;

import br.com.agendamento.consultas.motivo.MotivoAtendimento;
import br.com.agendamento.consultas.paciente.Paciente;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/** Aplica todas as regras (Strategy) e fica com a maior prioridade; as regras não se somam. */
@Component
public class CalculadoraPrioridade {

    private final List<RegraPrioridade> regras;

    public CalculadoraPrioridade(List<RegraPrioridade> regras) {
        this.regras = List.copyOf(regras);
    }

    public Prioridade calcular(Paciente paciente, MotivoAtendimento motivo, LocalDate dataConsulta) {
        List<Prioridade> concedidas = regras.stream()
                .flatMap(r -> r.avaliar(paciente, motivo, dataConsulta).stream())
                .toList();
        return concedidas.isEmpty() ? Prioridade.NORMAL : Collections.max(concedidas);
    }
}
