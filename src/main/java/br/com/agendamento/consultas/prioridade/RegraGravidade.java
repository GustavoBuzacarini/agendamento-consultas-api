package br.com.agendamento.consultas.prioridade;

import br.com.agendamento.consultas.motivo.MotivoAtendimento;
import br.com.agendamento.consultas.paciente.Paciente;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Gravidade do motivo: VERMELHO é urgente; AMARELO é prioritário; VERDE não concede nada (ASM-009). */
@Component
public class RegraGravidade implements RegraPrioridade {

    @Override
    public Optional<Prioridade> avaliar(Paciente paciente, MotivoAtendimento motivo, LocalDate dataConsulta) {
        return switch (motivo.getGravidade()) {
            case VERMELHO -> Optional.of(Prioridade.URGENTE);
            case AMARELO -> Optional.of(Prioridade.PRIORITARIA);
            case VERDE -> Optional.empty();
        };
    }
}
