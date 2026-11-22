package br.com.agendamento.consultas.motivo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class MotivoTest extends IntegrationTestBase {

    @Test
    @DisplayName("@spec:AC-003 Motivo de atendimento é cadastrado com sua cor de gravidade")
    void motivoCadastradoComCorDeGravidade() throws Exception {
        mvc.perform(post("/motivos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Dor no peito\",\"gravidade\":\"VERMELHO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gravidade").value("VERMELHO"));

        mvc.perform(get("/motivos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.descricao=='Dor no peito')].gravidade").value("VERMELHO"));

        // sem cor de gravidade
        mvc.perform(post("/motivos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Sem cor\"}"))
                .andExpect(status().isBadRequest());

        // cor fora das três permitidas
        mvc.perform(post("/motivos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Cor errada\",\"gravidade\":\"AZUL\"}"))
                .andExpect(status().isBadRequest());
    }
}
