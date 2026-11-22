package br.com.agendamento.consultas.profissional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ProfissionalTest extends IntegrationTestBase {

    private static final String DIAS_UTEIS = """
            {"nome":"Dra. Ana Souza","especialidade":"Clínica Geral","duracaoConsultaMin":30,"expediente":[
              {"diaSemana":"MONDAY","horaInicio":"08:00","horaFim":"12:00"},
              {"diaSemana":"TUESDAY","horaInicio":"08:00","horaFim":"12:00"},
              {"diaSemana":"WEDNESDAY","horaInicio":"08:00","horaFim":"12:00"},
              {"diaSemana":"THURSDAY","horaInicio":"08:00","horaFim":"12:00"},
              {"diaSemana":"FRIDAY","horaInicio":"08:00","horaFim":"12:00"}]}
            """;

    @Test
    @DisplayName("@spec:AC-002 Profissional é cadastrado com seu expediente semanal")
    void profissionalCadastradoComExpedienteSemanal() throws Exception {
        String local = mvc.perform(post("/profissionais").contentType(MediaType.APPLICATION_JSON).content(DIAS_UTEIS))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expediente.length()").value(5))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(local))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duracaoConsultaMin").value(30))
                .andExpect(jsonPath("$.expediente.length()").value(5))
                .andExpect(jsonPath("$.expediente[?(@.diaSemana=='SATURDAY')]").isEmpty());
    }

    @Test
    @DisplayName("Expediente com hora final antes da inicial é recusado")
    void expedienteInvertidoERecusado() throws Exception {
        String invertido = """
                {"nome":"Dr. Beto","especialidade":"Pediatria","duracaoConsultaMin":20,"expediente":[
                  {"diaSemana":"MONDAY","horaInicio":"12:00","horaFim":"08:00"}]}
                """;
        mvc.perform(post("/profissionais").contentType(MediaType.APPLICATION_JSON).content(invertido))
                .andExpect(status().isBadRequest());
    }
}
