package br.com.agendamento.consultas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.com.agendamento.consultas.cep.CepClientDeTeste;
import br.com.agendamento.consultas.motivo.Gravidade;
import br.com.agendamento.consultas.motivo.MotivoAtendimento;
import br.com.agendamento.consultas.motivo.MotivoRepository;
import br.com.agendamento.consultas.paciente.Paciente;
import br.com.agendamento.consultas.paciente.PacienteRepository;
import br.com.agendamento.consultas.profissional.Expediente;
import br.com.agendamento.consultas.profissional.Profissional;
import br.com.agendamento.consultas.profissional.ProfissionalRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base dos testes de integração: um PostgreSQL real (Testcontainers) compartilhado por todas as
 * classes de teste, tabelas limpas antes de cada teste e "agora" congelado (RelogioDeTeste).
 * Exige o Docker em execução.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({RelogioDeTeste.class, CepClientDeTeste.class})
public abstract class IntegrationTestBase {

    /** "Agora" nos testes: terça-feira, 10:00. */
    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 12, 1, 10, 0);
    protected static final LocalDate TERCA = LocalDate.of(2026, 12, 1);
    protected static final LocalDate QUARTA = LocalDate.of(2026, 12, 2);
    protected static final LocalDate QUINTA = LocalDate.of(2026, 12, 3);
    protected static final LocalDate SABADO = LocalDate.of(2026, 12, 5);

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17");
    private static final AtomicLong CPF_SEQUENCIAL = new AtomicLong(10_000_000_000L);

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void propriedadesDoBanco(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", POSTGRES::getUsername);
        registro.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PacienteRepository pacientes;

    @Autowired
    private ProfissionalRepository profissionais;

    @Autowired
    private MotivoRepository motivos;

    @BeforeEach
    void limparTabelas() {
        jdbc.execute("TRUNCATE consulta, paciente, profissional, motivo_atendimento RESTART IDENTITY CASCADE");
    }

    // ---- dados de apoio, gravados direto no banco ----

    protected Long novoPaciente(LocalDate nascimento, boolean gestante, boolean pessoaComDeficiencia) {
        String cpf = String.valueOf(CPF_SEQUENCIAL.incrementAndGet());
        return pacientes.save(new Paciente("Paciente " + cpf, cpf, nascimento, gestante, pessoaComDeficiencia)).getId();
    }

    protected Long novoPacienteComum() {
        return novoPaciente(LocalDate.of(1980, 5, 20), false, false);
    }

    /** Segunda a sexta, 08:00 às 12:00, consultas de 30 minutos. */
    protected Long novoProfissionalDiasUteis() {
        List<Expediente> expediente = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY).stream()
                .map(d -> new Expediente(d, LocalTime.of(8, 0), LocalTime.of(12, 0)))
                .toList();
        return profissionais.save(new Profissional("Dra. Ana Souza", "Clínica Geral", 30, expediente)).getId();
    }

    protected Long novoMotivo(String descricao, Gravidade gravidade) {
        return motivos.save(new MotivoAtendimento(descricao, gravidade)).getId();
    }

    /** Agenda pela API; devolve o resultado para o teste conferir. */
    protected ResultActions agendar(Long pacienteId, Long profissionalId, Long motivoId, LocalDate dia, int hora,
                                    int minuto) throws Exception {
        String corpo = """
                {"pacienteId":%d,"profissionalId":%d,"motivoId":%d,"inicio":"%s"}"""
                .formatted(pacienteId, profissionalId, motivoId, LocalDateTime.of(dia, LocalTime.of(hora, minuto)));
        return mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
