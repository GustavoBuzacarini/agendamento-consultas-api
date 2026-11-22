package br.com.agendamento.consultas.paciente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class PacienteCepTest extends IntegrationTestBase {

    @Autowired
    private PacienteRepository pacientes;

    private ResultActions cadastrar(String cepJson) throws Exception {
        String corpo = """
                {"nome":"Maria da Silva","cpf":"529.982.247-25","dataNascimento":"1980-05-20"%s}"""
                .formatted(cepJson);
        return mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    @Test
    @DisplayName("@spec:AC-020 CEP válido preenche o endereço do paciente")
    void cepValidoPreencheOEndereco() throws Exception {
        String local = cadastrar(",\"cep\":\"01001-000\"")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.endereco.logradouro").value("Praça da Sé"))
                .andExpect(jsonPath("$.endereco.bairro").value("Sé"))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.endereco.uf").value("SP"))
                .andReturn().getResponse().getHeader("Location");

        // o endereço foi gravado, não só devolvido na resposta
        mvc.perform(get(local))
                .andExpect(jsonPath("$.endereco.cep").value("01001000"))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"));
    }

    @Test
    @DisplayName("@spec:AC-020 Paciente sem CEP continua sendo aceito, com endereço em branco")
    void pacienteSemCepContinuaValido() throws Exception {
        cadastrar("")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.endereco").doesNotExist());
    }

    @Test
    @DisplayName("@spec:AC-021 CEP inexistente ou mal formatado é recusado")
    void cepInexistenteOuMalFormatadoERecusado() throws Exception {
        cadastrar(",\"cep\":\"99999-999\"")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("CEP não encontrado"));

        cadastrar(",\"cep\":\"123\"").andExpect(status().isBadRequest());

        assertEquals(0, pacientes.count(), "nenhum paciente deve ter sido criado");
    }

    @Test
    @DisplayName("@spec:AC-022 Serviço de CEP fora do ar recusa o cadastro com aviso claro")
    void servicoDeCepForaDoArRecusaOCadastro() throws Exception {
        cadastrar(",\"cep\":\"00000-000\"")
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("serviço de consulta de CEP indisponível, tente novamente"));

        assertEquals(0, pacientes.count(), "nenhum paciente deve ter sido gravado");
    }
}
