package br.com.agendamento.consultas.validacao;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import org.springframework.http.HttpStatus;

/** AC-005: o profissional não pode ter duas consultas ativas no mesmo horário (inclui sobreposição). */
public class ValidaConflitoHorario extends ValidadorAgendamento {

    private final AgendaConsultas agenda;

    public ValidaConflitoHorario(AgendaConsultas agenda) {
        this.agenda = agenda;
    }

    @Override
    protected void verificar(ContextoAgendamento contexto) {
        if (agenda.existeConsultaAtivaNoPeriodo(contexto.profissional().getId(), contexto.inicio(), contexto.fim())) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT, "horário indisponível");
        }
    }
}
