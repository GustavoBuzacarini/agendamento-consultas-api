package br.com.agendamento.consultas.consulta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class EstadoConsultaTest {

    @Test
    @DisplayName("@spec:AC-010 Consulta agendada pode ser confirmada")
    void agendadaPodeSerConfirmada() {
        assertEquals(StatusConsulta.CONFIRMADA, StatusConsulta.AGENDADA.confirmar());
    }

    @Test
    @DisplayName("@spec:AC-012 Consulta já realizada não pode ser cancelada")
    void realizadaNaoPodeSerCancelada() {
        StatusConsulta realizada = StatusConsulta.REALIZADA;

        TransicaoInvalidaException erro = assertThrows(TransicaoInvalidaException.class, realizada::cancelar);

        assertEquals(HttpStatus.CONFLICT, erro.getStatus());
        assertTrue(erro.getMessage().contains("transição de status inválida"));
        // o status da consulta não muda: o enum é imutável e continua REALIZADA
        assertEquals(StatusConsulta.REALIZADA, realizada);
    }

    @Test
    @DisplayName("@spec:AC-013 Só consulta confirmada pode virar Realizada ou Faltou")
    void soConfirmadaViraRealizadaOuFaltou() {
        assertEquals(StatusConsulta.REALIZADA, StatusConsulta.CONFIRMADA.realizar());
        assertEquals(StatusConsulta.FALTOU, StatusConsulta.CONFIRMADA.marcarFalta());

        TransicaoInvalidaException erro =
                assertThrows(TransicaoInvalidaException.class, StatusConsulta.AGENDADA::realizar);
        assertTrue(erro.getMessage().contains("transição de status inválida"));
        assertThrows(TransicaoInvalidaException.class, StatusConsulta.AGENDADA::marcarFalta);
    }

    @Test
    @DisplayName("Consultas agendadas e confirmadas podem ser canceladas; só elas ocupam horário")
    void cancelamentoEConsultaAtiva() {
        assertEquals(StatusConsulta.CANCELADA, StatusConsulta.AGENDADA.cancelar());
        assertEquals(StatusConsulta.CANCELADA, StatusConsulta.CONFIRMADA.cancelar());
        assertTrue(StatusConsulta.AGENDADA.ativa());
        assertTrue(StatusConsulta.CONFIRMADA.ativa());
        assertEquals(false, StatusConsulta.CANCELADA.ativa());
    }
}
