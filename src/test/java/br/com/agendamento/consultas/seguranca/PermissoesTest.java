package br.com.agendamento.consultas.seguranca;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import br.com.agendamento.consultas.motivo.Gravidade;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class PermissoesTest extends IntegrationTestBase {

    private static final String SEM_PERMISSAO = "sem permissão para esta operação";

    private long idDe(ResultActions resultado) throws Exception {
        return ((Number) JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    private ResultActions comoPaciente(Long pacienteId, MockHttpServletRequestBuilder requisicao) throws Exception {
        return mvcAnonimo.perform(comToken(requisicao, tokenDoPaciente(pacienteId)));
    }

    @Test
    @DisplayName("@spec:AC-031 Recepção continua fazendo tudo")
    void recepcaoContinuaFazendoTudo() throws Exception {
        // cadastros
        long paciente = idDe(mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"nome\":\"Maria\",\"cpf\":\"529.982.247-25\",\"dataNascimento\":\"1980-05-20\"}"))
                .andExpect(status().isCreated()));
        long profissional = idDe(mvc.perform(post("/profissionais").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"Dra. Ana","especialidade":"Clínica Geral","duracaoConsultaMin":30,"expediente":[
                         {"diaSemana":"WEDNESDAY","horaInicio":"08:00","horaFim":"12:00"}]}"""))
                .andExpect(status().isCreated()));
        long motivo = idDe(mvc.perform(post("/motivos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Rotina\",\"gravidade\":\"VERDE\"}"))
                .andExpect(status().isCreated()));

        // agendar, confirmar, realizar, cancelar outra, faltou e consultar a agenda
        long primeira = idDe(agendar(paciente, profissional, motivo, QUARTA, 9, 0).andExpect(status().isCreated()));
        long segunda = idDe(agendar(paciente, profissional, motivo, QUARTA, 9, 30).andExpect(status().isCreated()));
        mvc.perform(patch("/consultas/" + primeira + "/confirmar")).andExpect(status().isOk());
        mvc.perform(patch("/consultas/" + primeira + "/realizar")).andExpect(status().isOk());
        mvc.perform(patch("/consultas/" + segunda + "/cancelar")).andExpect(status().isOk());
        mvc.perform(get("/agenda").param("profissionalId", String.valueOf(profissional)).param("data", "2026-12-02"))
                .andExpect(status().isOk());
        mvc.perform(get("/consultas/" + primeira)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("@spec:AC-032 Paciente é barrado onde só a recepção atua")
    void pacienteEBarradoOndeSoARecepcaoAtua() throws Exception {
        Long paciente = novoPacienteComum();
        Long profissional = novoProfissionalDiasUteis();
        Long motivo = novoMotivo("Rotina", Gravidade.VERDE);
        long consulta = idDe(agendar(paciente, profissional, motivo, QUARTA, 9, 0));
        String corpoQualquer = "{}";

        comoPaciente(paciente, post("/pacientes").contentType(MediaType.APPLICATION_JSON).content(corpoQualquer))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").value(SEM_PERMISSAO));
        comoPaciente(paciente, post("/profissionais").contentType(MediaType.APPLICATION_JSON).content(corpoQualquer))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").value(SEM_PERMISSAO));
        comoPaciente(paciente, post("/motivos").contentType(MediaType.APPLICATION_JSON).content(corpoQualquer))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").value(SEM_PERMISSAO));
        comoPaciente(paciente, post("/consultas").contentType(MediaType.APPLICATION_JSON).content(corpoQualquer))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").value(SEM_PERMISSAO));
        comoPaciente(paciente, patch("/consultas/" + consulta + "/confirmar"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").value(SEM_PERMISSAO));
        comoPaciente(paciente, patch("/consultas/" + consulta + "/realizar")).andExpect(status().isForbidden());
        comoPaciente(paciente, patch("/consultas/" + consulta + "/faltou")).andExpect(status().isForbidden());
        comoPaciente(paciente, get("/agenda").param("profissionalId", String.valueOf(profissional))
                .param("data", "2026-12-02")).andExpect(status().isForbidden());
        comoPaciente(paciente, post("/pacientes/" + paciente + "/conta")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("@spec:AC-033 Paciente vê só as próprias consultas")
    void pacienteVeSoAsProprias() throws Exception {
        Long profissional = novoProfissionalDiasUteis();
        Long motivo = novoMotivo("Rotina", Gravidade.VERDE);
        Long ana = novoPacienteComum();
        Long beto = novoPacienteComum();
        long deAna = idDe(agendar(ana, profissional, motivo, QUARTA, 9, 0));
        long deBeto = idDe(agendar(beto, profissional, motivo, QUARTA, 9, 30));

        comoPaciente(ana, get("/consultas/minhas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(deAna));

        comoPaciente(ana, get("/consultas/" + deAna)).andExpect(status().isOk());
        comoPaciente(ana, get("/consultas/" + deBeto))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value(SEM_PERMISSAO));

        // a recepção continua vendo a de qualquer paciente
        mvc.perform(get("/consultas/" + deBeto)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("@spec:AC-034 Paciente cancela a própria consulta, e só a própria")
    void pacienteCancelaASuaESoASua() throws Exception {
        Long profissional = novoProfissionalDiasUteis();
        Long motivo = novoMotivo("Rotina", Gravidade.VERDE);
        Long ana = novoPacienteComum();
        Long beto = novoPacienteComum();
        long deAna = idDe(agendar(ana, profissional, motivo, QUARTA, 9, 0));
        long deBeto = idDe(agendar(beto, profissional, motivo, QUARTA, 9, 30));

        comoPaciente(ana, patch("/consultas/" + deAna + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        // o horário da Ana ficou livre
        agendar(beto, profissional, motivo, QUARTA, 9, 0).andExpect(status().isCreated());

        // a consulta do Beto não pode ser cancelada pela Ana e continua agendada
        comoPaciente(ana, patch("/consultas/" + deBeto + "/cancelar"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value(SEM_PERMISSAO));
        mvc.perform(get("/consultas/" + deBeto)).andExpect(jsonPath("$.status").value("AGENDADA"));
    }
}
