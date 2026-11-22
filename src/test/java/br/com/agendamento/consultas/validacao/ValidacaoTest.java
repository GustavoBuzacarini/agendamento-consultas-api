package br.com.agendamento.consultas.validacao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import br.com.agendamento.consultas.motivo.Gravidade;
import br.com.agendamento.consultas.motivo.MotivoAtendimento;
import br.com.agendamento.consultas.motivo.MotivoRepository;
import br.com.agendamento.consultas.paciente.Paciente;
import br.com.agendamento.consultas.profissional.Expediente;
import br.com.agendamento.consultas.profissional.Profissional;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Testa a cadeia completa de validação, sem banco: motivos e agenda são substituídos por versões de teste. */
class ValidacaoTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    /** Terça-feira, 10:00. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 12, 1, 10, 0);
    private static final Clock RELOGIO = Clock.fixed(AGORA.atZone(ZONA).toInstant(), ZONA);

    private static final LocalDate TERCA = LocalDate.of(2026, 12, 1);
    private static final LocalDate QUARTA = LocalDate.of(2026, 12, 2);
    private static final LocalDate QUINTA = LocalDate.of(2026, 12, 3);
    private static final LocalDate SABADO = LocalDate.of(2026, 12, 5);

    /** Segunda a sexta, 08:00 às 12:00, consultas de 30 minutos. */
    private final Profissional profissional = new Profissional("Dra. Ana Souza", "Clínica Geral", 30, List.of(
            new Expediente(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)),
            new Expediente(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)),
            new Expediente(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)),
            new Expediente(DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)),
            new Expediente(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))));

    private final Paciente paciente =
            new Paciente("Maria da Silva", "52998224725", LocalDate.of(1980, 5, 20), false, false);

    private final AgendaEmMemoria agenda = new AgendaEmMemoria();
    private final MotivoRepository motivos = mock(MotivoRepository.class);

    /** Agenda de teste: guarda períodos ocupados e uma contagem fixa de consultas futuras do paciente. */
    private static class AgendaEmMemoria implements AgendaConsultas {
        private final List<LocalDateTime[]> ocupados = new ArrayList<>();
        private long consultasFuturas = 0;

        void ocupar(LocalDateTime inicio, LocalDateTime fim) {
            ocupados.add(new LocalDateTime[] {inicio, fim});
        }

        @Override
        public boolean existeConsultaAtivaNoPeriodo(Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
            return ocupados.stream().anyMatch(o -> o[0].isBefore(fim) && o[1].isAfter(inicio));
        }

        @Override
        public long contarConsultasAtivasFuturas(Long pacienteId, LocalDateTime agora) {
            return consultasFuturas;
        }
    }

    private CadeiaDeValidacao cadeia(int antecedenciaHoras, int limite) {
        AgendamentoProperties props = new AgendamentoProperties(antecedenciaHoras, limite);
        return new CadeiaDeValidacao(List.of(
                new ValidaMotivoExistente(motivos),
                new ValidaExpediente(),
                new ValidaConflitoHorario(agenda),
                new ValidaAntecedenciaMinima(props, RELOGIO),
                new ValidaLimiteConsultas(props, agenda, RELOGIO)));
    }

    private ContextoAgendamento pedido(LocalDate dia, int hora, int minuto) {
        return new ContextoAgendamento(paciente, profissional, 1L, LocalDateTime.of(dia, LocalTime.of(hora, minuto)));
    }

    private void motivoCadastrado(Gravidade gravidade) {
        when(motivos.findById(1L)).thenReturn(Optional.of(new MotivoAtendimento("Motivo", gravidade)));
    }

    private RegraDeNegocioException recusa(CadeiaDeValidacao cadeia, ContextoAgendamento pedido) {
        return assertThrows(RegraDeNegocioException.class, () -> cadeia.validar(pedido));
    }

    @Test
    @DisplayName("Agendamento dentro de todas as regras é aceito")
    void agendamentoValidoEAceito() {
        motivoCadastrado(Gravidade.VERDE);

        assertDoesNotThrow(() -> cadeia(2, 2).validar(pedido(QUARTA, 9, 0)));
    }

    @Test
    @DisplayName("@spec:AC-005 Horário já ocupado do médico é recusado")
    void horarioOcupadoERecusado() {
        motivoCadastrado(Gravidade.VERDE);
        agenda.ocupar(LocalDateTime.of(QUINTA, LocalTime.of(9, 0)), LocalDateTime.of(QUINTA, LocalTime.of(9, 30)));

        RegraDeNegocioException erro = recusa(cadeia(2, 2), pedido(QUINTA, 9, 0));

        assertEquals(HttpStatus.CONFLICT, erro.getStatus());
        assertEquals("horário indisponível", erro.getMessage());
    }

    @Test
    @DisplayName("@spec:AC-006 Horário fora do expediente é recusado")
    void foraDoExpedienteERecusado() {
        motivoCadastrado(Gravidade.VERDE);

        RegraDeNegocioException tarde = recusa(cadeia(2, 2), pedido(QUINTA, 15, 0));
        RegraDeNegocioException sabado = recusa(cadeia(2, 2), pedido(SABADO, 9, 0));

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, tarde.getStatus());
        assertEquals("fora do expediente do profissional", tarde.getMessage());
        assertEquals("fora do expediente do profissional", sabado.getMessage());
    }

    @Test
    @DisplayName("@spec:AC-007 Agendamento sem a antecedência mínima é recusado")
    void semAntecedenciaMinimaERecusado() {
        motivoCadastrado(Gravidade.VERDE);

        // 1 hora depois de AGORA (terça, 11:00), dentro do expediente
        RegraDeNegocioException erro = recusa(cadeia(2, 2), pedido(TERCA, 11, 0));

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, erro.getStatus());
        assertEquals("antecedência mínima de 2 horas não respeitada", erro.getMessage());
    }

    @Test
    @DisplayName("@spec:AC-008 Paciente acima do limite de consultas futuras é recusado")
    void acimaDoLimiteERecusadoELimiteEConfiguravel() {
        motivoCadastrado(Gravidade.VERDE);
        agenda.consultasFuturas = 2;

        RegraDeNegocioException erro = recusa(cadeia(2, 2), pedido(QUARTA, 9, 0));
        assertEquals("limite de consultas futuras atingido", erro.getMessage());

        // com o limite configurado em 3, o mesmo agendamento é aceito
        assertDoesNotThrow(() -> cadeia(2, 3).validar(pedido(QUARTA, 9, 0)));
    }

    @Test
    @DisplayName("@spec:AC-009 Motivo inexistente é recusado")
    void motivoInexistenteERecusado() {
        when(motivos.findById(1L)).thenReturn(Optional.empty());

        RegraDeNegocioException erro = recusa(cadeia(2, 2), pedido(QUARTA, 9, 0));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatus());
        assertEquals("motivo de atendimento não encontrado", erro.getMessage());
    }

    @Test
    @DisplayName("Caso VERMELHO dispensa antecedência e limite, mas não conflito nem expediente")
    void vermelhoDispensaAntecedenciaELimite() {
        motivoCadastrado(Gravidade.VERMELHO);
        agenda.consultasFuturas = 2;

        // 30 minutos depois de AGORA (terça, 10:30), paciente no limite: aceito como encaixe
        assertDoesNotThrow(() -> cadeia(2, 2).validar(pedido(TERCA, 10, 30)));

        agenda.ocupar(LocalDateTime.of(TERCA, LocalTime.of(10, 30)), LocalDateTime.of(TERCA, LocalTime.of(11, 0)));
        assertEquals("horário indisponível", recusa(cadeia(2, 2), pedido(TERCA, 10, 30)).getMessage());
        assertEquals("fora do expediente do profissional", recusa(cadeia(2, 2), pedido(SABADO, 9, 0)).getMessage());
    }

    @Test
    @DisplayName("Caso AMARELO continua sujeito à antecedência e ao limite")
    void amareloNaoDispensaRegras() {
        motivoCadastrado(Gravidade.AMARELO);

        assertEquals("antecedência mínima de 2 horas não respeitada",
                recusa(cadeia(2, 2), pedido(TERCA, 11, 0)).getMessage());

        agenda.consultasFuturas = 2;
        assertEquals("limite de consultas futuras atingido",
                recusa(cadeia(2, 2), pedido(QUARTA, 9, 0)).getMessage());
    }
}
