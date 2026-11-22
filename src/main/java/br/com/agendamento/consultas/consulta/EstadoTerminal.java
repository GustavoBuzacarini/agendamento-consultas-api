package br.com.agendamento.consultas.consulta;

/** REALIZADA, CANCELADA e FALTOU: estados finais, sem nenhuma transição permitida. */
final class EstadoTerminal implements EstadoConsulta {

    private final StatusConsulta status;

    EstadoTerminal(StatusConsulta status) {
        this.status = status;
    }

    @Override
    public StatusConsulta status() {
        return status;
    }
}
