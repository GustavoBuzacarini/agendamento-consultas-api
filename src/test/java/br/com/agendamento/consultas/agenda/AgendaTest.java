package br.com.agendamento.consultas.agenda;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import br.com.agendamento.consultas.motivo.Gravidade;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgendaTest extends IntegrationTestBase {

    @Test
    @DisplayName("@spec:AC-018 A agenda do dia vem ordenada por prioridade")
    void agendaDoDiaVemOrdenadaPorPrioridade() throws Exception {
        Long profissional = novoProfissionalDiasUteis();
        Long rotina = novoMotivo("Rotina", Gravidade.VERDE);
        Long grave = novoMotivo("Situação grave", Gravidade.VERMELHO);

        Long comum = novoPacienteComum();
        Long gestante = novoPaciente(LocalDate.of(1995, 3, 10), true, false);
        Long outro = novoPacienteComum();

        agendar(comum, profissional, rotina, QUARTA, 8, 0).andExpect(status().isCreated());     // comum 08:00
        agendar(gestante, profissional, rotina, QUARTA, 9, 0).andExpect(status().isCreated());  // prioritária 09:00
        agendar(outro, profissional, grave, QUARTA, 10, 0).andExpect(status().isCreated());     // urgente 10:00

        mvc.perform(get("/agenda").param("profissionalId", profissional.toString()).param("data", "2026-12-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].prioridade").value("URGENTE"))
                .andExpect(jsonPath("$[0].inicio").value("2026-12-02T10:00:00"))
                .andExpect(jsonPath("$[1].prioridade").value("PRIORITARIA"))
                .andExpect(jsonPath("$[1].inicio").value("2026-12-02T09:00:00"))
                .andExpect(jsonPath("$[2].prioridade").value("NORMAL"))
                .andExpect(jsonPath("$[2].inicio").value("2026-12-02T08:00:00"));
    }

    @Test
    @DisplayName("A agenda mostra só as consultas do dia pedido; profissional inexistente dá 404")
    void soConsultasAtivasDoDia() throws Exception {
        Long profissional = novoProfissionalDiasUteis();
        Long rotina = novoMotivo("Rotina", Gravidade.VERDE);

        agendar(novoPacienteComum(), profissional, rotina, QUARTA, 9, 0).andExpect(status().isCreated());
        agendar(novoPacienteComum(), profissional, rotina, QUINTA, 9, 0).andExpect(status().isCreated());

        mvc.perform(get("/agenda").param("profissionalId", profissional.toString()).param("data", "2026-12-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/agenda").param("profissionalId", "999").param("data", "2026-12-02"))
                .andExpect(status().isNotFound());
    }
}
