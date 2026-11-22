package br.com.agendamento.consultas.paciente;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PacienteTest extends IntegrationTestBase {

    private static final String PACIENTE = """
            {"nome":"Maria da Silva","cpf":"%s","dataNascimento":"1980-05-20","gestante":false,"pessoaComDeficiencia":false}
            """;

    @Test
    @DisplayName("@spec:AC-001 Paciente é cadastrado e o CPF não se repete")
    void pacienteCadastradoECpfNaoSeRepete() throws Exception {
        mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON)
                        .content(PACIENTE.formatted("529.982.247-25")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("52998224725"));

        mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON)
                        .content(PACIENTE.formatted("52998224725")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("CPF já cadastrado"));
    }

    @Test
    @DisplayName("CPF inválido é recusado")
    void cpfInvalidoERecusado() throws Exception {
        mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON)
                        .content(PACIENTE.formatted("111.111.111-11")))
                .andExpect(status().isBadRequest());
    }
}
