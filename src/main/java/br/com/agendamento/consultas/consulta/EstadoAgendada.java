package br.com.agendamento.consultas.consulta;

/** AGENDADA: pode ser confirmada ou cancelada. */
final class EstadoAgendada implements EstadoConsulta {

    @Override
    public StatusConsulta status() {
        return StatusConsulta.AGENDADA;
    }

    @Override
    public boolean ativa() {
        return true;
    }

    @Override
    public StatusConsulta confirmar() {
        return StatusConsulta.CONFIRMADA;
    }

    @Override
    public StatusConsulta cancelar() {
        return StatusConsulta.CANCELADA;
    }
}
