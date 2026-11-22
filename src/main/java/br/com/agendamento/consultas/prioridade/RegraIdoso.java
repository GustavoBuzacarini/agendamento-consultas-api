package br.com.agendamento.consultas.prioridade;

import br.com.agendamento.consultas.motivo.MotivoAtendimento;
import br.com.agendamento.consultas.paciente.Paciente;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Pessoa com 60 anos completos na data da consulta (Lei 10.048/2000 e Estatuto da Pessoa Idosa). */
@Component
public class RegraIdoso implements RegraPrioridade {

    static final int IDADE_MINIMA = 60;

    @Override
    public Optional<Prioridade> avaliar(Paciente paciente, MotivoAtendimento motivo, LocalDate dataConsulta) {
        return paciente.idadeEm(dataConsulta) >= IDADE_MINIMA
                ? Optional.of(Prioridade.PRIORITARIA)
                : Optional.empty();
    }
}
