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
import br.com.agendamento.consultas.seguranca.JwtService;
import br.com.agendamento.consultas.seguranca.Perfil;
import br.com.agendamento.consultas.seguranca.TokenPadraoDeTeste;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base dos testes de integração: um PostgreSQL real (Testcontainers) compartilhado por todas as
 * classes de teste, tabelas limpas antes de cada teste, "agora" congelado (RelogioDeTeste) e API
 * protegida. O {@code mvc} já envia o token da recepção; o {@code mvcAnonimo} não envia nada.
 * Exige o Docker em execução.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({RelogioDeTeste.class, CepClientDeTeste.class, TokenPadraoDeTeste.class})
public abstract class IntegrationTestBase {

    /** "Agora" nos testes: terça-feira, 10:00. */
    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 12, 1, 10, 0);
    protected static final LocalDate TERCA = LocalDate.of(2026, 12, 1);
    protected static final LocalDate QUARTA = LocalDate.of(2026, 12, 2);
    protected static final LocalDate QUINTA = LocalDate.of(2026, 12, 3);
    protected static final LocalDate SABADO = LocalDate.of(2026, 12, 5);

    /** Segredo só dos testes (a aplicação real lê JWT_SECRET do ambiente). */
    protected static final String SEGREDO_DE_TESTE = "segredo-apenas-para-testes-com-mais-de-32-caracteres";

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
        registro.add("seguranca.jwt.segredo", () -> SEGREDO_DE_TESTE);
    }

    /** MockMvc que envia o token da recepção por padrão. */
    @Autowired
    protected MockMvc mvc;

    /** MockMvc que não envia token algum; cada teste manda o seu, se quiser. */
    protected MockMvc mvcAnonimo;

    @Autowired
    protected JwtService jwt;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private PacienteRepository pacientes;

    @Autowired
    private ProfissionalRepository profissionais;

    @Autowired
    private MotivoRepository motivos;

    @BeforeEach
    void prepararCadaTeste() {
        jdbc.execute("TRUNCATE usuario, consulta, paciente, profissional, motivo_atendimento RESTART IDENTITY CASCADE");
        mvcAnonimo = MockMvcBuilders.webAppContextSetup(contexto)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ---- tokens ----

    protected String tokenDaRecepcao() {
        return jwt.emitir("recepcao-de-teste", Perfil.RECEPCAO, null).valor();
    }

    protected String tokenDoPaciente(Long pacienteId) {
        return jwt.emitir("paciente-" + pacienteId, Perfil.PACIENTE, pacienteId).valor();
    }

    /** Acrescenta o cabeçalho Authorization: Bearer ... à requisição. */
    protected static MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder requisicao, String token) {
        return requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
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

    /** Faz o login pela API, sem token (como um usuário que ainda não entrou). */
    protected ResultActions login(String usuario, String credencial) throws Exception {
        String corpo = """
                {"usuario":"%s","senha":"%s"}""".formatted(usuario, credencial);
        return mvcAnonimo.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    /** Agenda pela API, com o token da recepção; devolve o resultado para o teste conferir. */
    protected ResultActions agendar(Long pacienteId, Long profissionalId, Long motivoId, LocalDate dia, int hora,
                                    int minuto) throws Exception {
        String corpo = """
                {"pacienteId":%d,"profissionalId":%d,"motivoId":%d,"inicio":"%s"}"""
                .formatted(pacienteId, profissionalId, motivoId, LocalDateTime.of(dia, LocalTime.of(hora, minuto)));
        return mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
