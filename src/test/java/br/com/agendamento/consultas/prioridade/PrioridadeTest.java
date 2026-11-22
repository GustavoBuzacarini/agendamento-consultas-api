package br.com.agendamento.consultas.prioridade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.agendamento.consultas.motivo.Gravidade;
import br.com.agendamento.consultas.motivo.MotivoAtendimento;
import br.com.agendamento.consultas.paciente.Paciente;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PrioridadeTest {

    private static final LocalDate DIA_DA_CONSULTA = LocalDate.of(2026, 12, 3);

    private final CalculadoraPrioridade calculadora = new CalculadoraPrioridade(
            List.of(new RegraIdoso(), new RegraGestante(), new RegraDeficiencia(), new RegraGravidade()));

    private final MotivoAtendimento verde = new MotivoAtendimento("Rotina", Gravidade.VERDE);

    private Paciente paciente(LocalDate nascimento, boolean gestante, boolean pcd) {
        return new Paciente("Paciente Teste", "52998224725", nascimento, gestante, pcd);
    }

    @Test
    @DisplayName("@spec:AC-014 Idoso (60 anos ou mais) recebe prioridade")
    void idosoRecebePrioridade() {
        Paciente idoso = paciente(DIA_DA_CONSULTA.minusYears(60), false, false);

        assertEquals(Prioridade.PRIORITARIA, calculadora.calcular(idoso, verde, DIA_DA_CONSULTA));
    }

    @Test
    @DisplayName("@spec:AC-015 Gestante recebe prioridade")
    void gestanteRecebePrioridade() {
        Paciente gestante = paciente(LocalDate.of(1995, 3, 10), true, false);

        assertEquals(Prioridade.PRIORITARIA, calculadora.calcular(gestante, verde, DIA_DA_CONSULTA));
    }

    @Test
    @DisplayName("@spec:AC-016 Pessoa com deficiência recebe prioridade")
    void pessoaComDeficienciaRecebePrioridade() {
        Paciente pcd = paciente(LocalDate.of(1990, 7, 1), false, true);

        assertEquals(Prioridade.PRIORITARIA, calculadora.calcular(pcd, verde, DIA_DA_CONSULTA));
    }

    @Test
    @DisplayName("Quem faz 60 anos só no dia seguinte ainda não é prioritário por idade")
    void quemFaz60AnosAmanhaAindaNaoEPrioritario() {
        Paciente quaseIdoso = paciente(DIA_DA_CONSULTA.minusYears(60).plusDays(1), false, false);

        assertEquals(Prioridade.NORMAL, calculadora.calcular(quaseIdoso, verde, DIA_DA_CONSULTA));
    }

    @Test
    @DisplayName("Gravidade VERMELHO é urgente e AMARELO é prioritária")
    void gravidadePorCor() {
        Paciente comum = paciente(LocalDate.of(1990, 1, 1), false, false);

        assertEquals(Prioridade.URGENTE,
                calculadora.calcular(comum, new MotivoAtendimento("Grave", Gravidade.VERMELHO), DIA_DA_CONSULTA));
        assertEquals(Prioridade.PRIORITARIA,
                calculadora.calcular(comum, new MotivoAtendimento("Médio", Gravidade.AMARELO), DIA_DA_CONSULTA));
        assertEquals(Prioridade.NORMAL, calculadora.calcular(comum, verde, DIA_DA_CONSULTA));
    }

    @Test
    @DisplayName("Prioridades não se somam: vale a maior (gestante com caso vermelho é urgente)")
    void valeAMaior() {
        Paciente gestante = paciente(LocalDate.of(1995, 3, 10), true, false);

        assertEquals(Prioridade.URGENTE, calculadora.calcular(gestante,
                new MotivoAtendimento("Grave", Gravidade.VERMELHO), DIA_DA_CONSULTA));
    }
}
