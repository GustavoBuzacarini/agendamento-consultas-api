package br.com.agendamento.consultas.conta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import br.com.agendamento.consultas.paciente.Paciente;
import br.com.agendamento.consultas.paciente.PacienteRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

class ContaPacienteTest extends IntegrationTestBase {

    @Autowired
    private PacienteRepository pacientes;

    private String lerCampo(ResultActions resultado, String caminho) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), caminho);
    }

    private String cpfDe(Long pacienteId) {
        return pacientes.findById(pacienteId).map(Paciente::getCpf).orElseThrow();
    }

    @Test
    @DisplayName("@spec:AC-028 Recepção cria a conta e recebe a senha provisória uma única vez")
    void recepcaoCriaAContaEReceBeASenhaProvisoria() throws Exception {
        Long paciente = novoPacienteComum();

        ResultActions criada = mvc.perform(post("/pacientes/" + paciente + "/conta"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(jsonPath("$.usuario").value(cpfDe(paciente)));
        String provisoria = lerCampo(criada, "$.senhaProvisoria");
        assertEquals(10, provisoria.length());

        // o paciente entra com o CPF e a senha provisória
        login(cpfDe(paciente), provisoria)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("PACIENTE"));
    }

    @Test
    @DisplayName("@spec:AC-029 Paciente que já tem conta não ganha uma segunda")
    void pacienteComContaNaoGanhaSegunda() throws Exception {
        Long paciente = novoPacienteComum();
        mvc.perform(post("/pacientes/" + paciente + "/conta")).andExpect(status().isCreated());

        mvc.perform(post("/pacientes/" + paciente + "/conta"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("paciente já possui conta"));
    }

    @Test
    @DisplayName("@spec:AC-030 Recepção gera nova senha provisória e a antiga deixa de valer")
    void novaSenhaProvisoriaInvalidaAAntiga() throws Exception {
        Long paciente = novoPacienteComum();
        String antiga = lerCampo(mvc.perform(post("/pacientes/" + paciente + "/conta")), "$.senhaProvisoria");

        String nova = lerCampo(mvc.perform(post("/pacientes/" + paciente + "/conta/senha-provisoria"))
                .andExpect(status().isOk()), "$.senhaProvisoria");

        assertNotEquals(antiga, nova);
        login(cpfDe(paciente), antiga).andExpect(status().isUnauthorized());
        login(cpfDe(paciente), nova).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Gerar senha provisória para quem não tem conta, ou para paciente inexistente, dá 404")
    void semContaOuSemPacienteDa404() throws Exception {
        Long semConta = novoPacienteComum();
        mvc.perform(post("/pacientes/" + semConta + "/conta/senha-provisoria")).andExpect(status().isNotFound());
        mvc.perform(post("/pacientes/999/conta")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-039 Senhas nunca são guardadas nem devolvidas em texto")
    void senhasNuncaSaoGuardadasNemDevolvidasEmTexto() throws Exception {
        Long paciente = novoPacienteComum();
        String provisoria = lerCampo(mvc.perform(post("/pacientes/" + paciente + "/conta")), "$.senhaProvisoria");

        // no banco só existe o hash BCrypt
        String hash = jdbc.queryForObject("select senha_hash from usuario where paciente_id = ?", String.class,
                paciente);
        assertTrue(hash.startsWith("$2"), "deve ser um hash BCrypt");
        assertNotEquals(provisoria, hash);
        assertFalse(hash.contains(provisoria));

        // nem o login nem a consulta do paciente devolvem a senha
        String respostaDoLogin = login(cpfDe(paciente), provisoria).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertFalse(respostaDoLogin.contains(provisoria));
        String respostaDoPaciente = mvc.perform(get("/pacientes/" + paciente)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertFalse(respostaDoPaciente.contains(provisoria));
    }
}
