package br.com.agendamento.consultas.consulta;

/** CONFIRMADA: pode ser cancelada, realizada ou marcada como falta. */
final class EstadoConfirmada implements EstadoConsulta {

    @Override
    public StatusConsulta status() {
        return StatusConsulta.CONFIRMADA;
    }

    @Override
    public boolean ativa() {
        return true;
    }

    @Override
    public StatusConsulta cancelar() {
        return StatusConsulta.CANCELADA;
    }

    @Override
    public StatusConsulta realizar() {
        return StatusConsulta.REALIZADA;
    }

    @Override
    public StatusConsulta marcarFalta() {
        return StatusConsulta.FALTOU;
    }
}
