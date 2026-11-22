package br.com.agendamento.consultas.consulta;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import br.com.agendamento.consultas.motivo.Gravidade;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

class AgendamentoApiTest extends IntegrationTestBase {

    private long idDaResposta(ResultActions resultado) throws Exception {
        String corpo = resultado.andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    @Test
    @DisplayName("@spec:AC-004 Agendamento válido cria uma consulta Agendada")
    void agendamentoValidoCriaConsultaAgendada() throws Exception {
        Long profissional = novoProfissionalDiasUteis();
        Long motivo = novoMotivo("Rotina", Gravidade.VERDE);

        agendar(novoPacienteComum(), profissional, motivo, QUARTA, 9, 0)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.prioridade").value("NORMAL"))
                .andExpect(jsonPath("$.inicio").value("2026-12-02T09:00:00"))
                .andExpect(jsonPath("$.fim").value("2026-12-02T09:30:00"));
    }

    @Test
    @DisplayName("@spec:AC-011 Cancelar uma consulta libera o horário")
    void cancelarLiberaOHorario() throws Exception {
        Long profissional = novoProfissionalDiasUteis();
        Long motivo = novoMotivo("Rotina", Gravidade.VERDE);
        Long maria = novoPacienteComum();
        Long joao = novoPacienteComum();

        long consulta = idDaResposta(agendar(maria, profissional, motivo, QUARTA, 9, 0).andExpect(status().isCreated()));

        // enquanto a consulta está ativa, o horário está ocupado
        agendar(joao, profissional, motivo, QUARTA, 9, 0).andExpect(status().isConflict());

        mvc.perform(patch("/consultas/" + consulta + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        agendar(joao, profissional, motivo, QUARTA, 9, 0)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"));
    }

    @Test
    @DisplayName("@spec:AC-017 Caso vermelho (grave) é urgente e dispensa antecedência e limite")
    void casoVermelhoEUrgenteEDispensaAntecedenciaELimite() throws Exception {
        Long profissional = novoProfissionalDiasUteis();
        Long rotina = novoMotivo("Rotina", Gravidade.VERDE);
        Long grave = novoMotivo("Dor no peito", Gravidade.VERMELHO);
        Long paciente = novoPacienteComum();

        // paciente já no limite de 2 consultas futuras
        agendar(paciente, profissional, rotina, QUARTA, 9, 0).andExpect(status().isCreated());
        agendar(paciente, profissional, rotina, QUARTA, 9, 30).andExpect(status().isCreated());
        agendar(paciente, profissional, rotina, QUARTA, 10, 0).andExpect(status().isUnprocessableContent());

        // terça 10:30 = daqui a 30 minutos, dentro do expediente e livre
        agendar(paciente, profissional, grave, TERCA, 10, 30)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridade").value("URGENTE"));

        // conflito e expediente continuam valendo para o caso vermelho
        agendar(novoPacienteComum(), profissional, grave, TERCA, 10, 30)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("horário indisponível"));
        agendar(novoPacienteComum(), profissional, grave, SABADO, 9, 0)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("fora do expediente do profissional"));
    }

    @Test
    @DisplayName("@spec:AC-019 Caso amarelo (intermediário) é prioritário, mas segue as regras normais")
    void casoAmareloEPrioritarioMasSegueAsRegras() throws Exception {
        Long profissional = novoProfissionalDiasUteis();
        Long rotina = novoMotivo("Rotina", Gravidade.VERDE);
        Long medio = novoMotivo("Sintoma persistente", Gravidade.AMARELO);

        // abaixo do limite: aceito, com prioridade PRIORITARIA
        agendar(novoPacienteComum(), profissional, medio, QUARTA, 10, 0)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridade").value("PRIORITARIA"));

        // no limite: o amarelo não dispensa a regra
        Long noLimite = novoPacienteComum();
        agendar(noLimite, profissional, rotina, QUARTA, 9, 0).andExpect(status().isCreated());
        agendar(noLimite, profissional, rotina, QUARTA, 9, 30).andExpect(status().isCreated());
        agendar(noLimite, profissional, medio, QUARTA, 11, 0)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("limite de consultas futuras atingido"));
    }

    @Test
    @DisplayName("Consulta percorre confirmar, realizar e não aceita transição inválida")
    void cicloDeVidaPelaApi() throws Exception {
        Long profissional = novoProfissionalDiasUteis();
        Long motivo = novoMotivo("Rotina", Gravidade.VERDE);
        long consulta = idDaResposta(agendar(novoPacienteComum(), profissional, motivo, QUARTA, 9, 0));

        mvc.perform(patch("/consultas/" + consulta + "/realizar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("transição de status inválida")));
        mvc.perform(patch("/consultas/" + consulta + "/confirmar"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMADA"));
        mvc.perform(patch("/consultas/" + consulta + "/realizar"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REALIZADA"));
        mvc.perform(patch("/consultas/" + consulta + "/cancelar")).andExpect(status().isConflict());
        mvc.perform(get("/consultas/" + consulta)).andExpect(jsonPath("$.status").value("REALIZADA"));
    }

    @Test
    @DisplayName("Paciente, profissional ou consulta inexistentes dão 404")
    void inexistentesDao404() throws Exception {
        Long motivo = novoMotivo("Rotina", Gravidade.VERDE);

        agendar(999L, novoProfissionalDiasUteis(), motivo, QUARTA, 9, 0)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("paciente não encontrado"));
        mvc.perform(get("/consultas/999")).andExpect(status().isNotFound());
    }
}
