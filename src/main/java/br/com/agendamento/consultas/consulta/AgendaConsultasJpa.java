package br.com.agendamento.consultas.consulta;

import br.com.agendamento.consultas.validacao.AgendaConsultas;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

/** Implementa, sobre o banco, o que a cadeia de validação precisa saber das consultas existentes. */
@Component
public class AgendaConsultasJpa implements AgendaConsultas {

    /** Consultas ativas (AGENDADA e CONFIRMADA) ocupam horário e contam no limite (ASM-008). */
    static final List<StatusConsulta> ATIVAS =
            Arrays.stream(StatusConsulta.values()).filter(StatusConsulta::ativa).toList();

    private final ConsultaRepository repository;

    public AgendaConsultasJpa(ConsultaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existeConsultaAtivaNoPeriodo(Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
        return repository.existeAtivaNoPeriodo(profissionalId, inicio, fim, ATIVAS);
    }

    @Override
    public long contarConsultasAtivasFuturas(Long pacienteId, LocalDateTime agora) {
        return repository.contarAtivasFuturas(pacienteId, agora, ATIVAS);
    }
}
