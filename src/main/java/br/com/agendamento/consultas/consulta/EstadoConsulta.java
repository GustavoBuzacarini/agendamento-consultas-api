package br.com.agendamento.consultas.consulta;

/**
 * Padrão State: cada status da consulta é um estado que sabe quais transições aceita.
 * Por padrão toda transição é inválida; cada estado sobrescreve só o que permite.
 */
public interface EstadoConsulta {

    StatusConsulta status();

    /** Consulta ativa ocupa o horário do profissional e conta no limite do paciente (ASM-008). */
    default boolean ativa() {
        return false;
    }

    default StatusConsulta confirmar() {
        throw new TransicaoInvalidaException(status(), "confirmar");
    }

    default StatusConsulta cancelar() {
        throw new TransicaoInvalidaException(status(), "cancelar");
    }

    default StatusConsulta realizar() {
        throw new TransicaoInvalidaException(status(), "marcar como realizada");
    }

    default StatusConsulta marcarFalta() {
        throw new TransicaoInvalidaException(status(), "marcar falta em");
    }
}
