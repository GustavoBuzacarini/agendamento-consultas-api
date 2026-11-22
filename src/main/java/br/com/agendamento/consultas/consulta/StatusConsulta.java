package br.com.agendamento.consultas.consulta;

/**
 * Status da consulta (persistido como texto). Cada constante delega as transições ao seu
 * {@link EstadoConsulta} (padrão State).
 *
 * <pre>
 * AGENDADA -> CONFIRMADA -> REALIZADA | FALTOU
 * AGENDADA | CONFIRMADA -> CANCELADA
 * </pre>
 */
public enum StatusConsulta {
    AGENDADA,
    CONFIRMADA,
    REALIZADA,
    CANCELADA,
    FALTOU;

    private EstadoConsulta estado() {
        return switch (this) {
            case AGENDADA -> new EstadoAgendada();
            case CONFIRMADA -> new EstadoConfirmada();
            case REALIZADA, CANCELADA, FALTOU -> new EstadoTerminal(this);
        };
    }

    public boolean ativa() {
        return estado().ativa();
    }

    public StatusConsulta confirmar() {
        return estado().confirmar();
    }

    public StatusConsulta cancelar() {
        return estado().cancelar();
    }

    public StatusConsulta realizar() {
        return estado().realizar();
    }

    public StatusConsulta marcarFalta() {
        return estado().marcarFalta();
    }
}
