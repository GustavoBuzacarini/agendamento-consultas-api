package br.com.agendamento.consultas.validacao;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import br.com.agendamento.consultas.motivo.Gravidade;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

/** AC-008: limite configurável de consultas ativas futuras por paciente; caso VERMELHO dispensa (ASM-002). */
public class ValidaLimiteConsultas extends ValidadorAgendamento {

    private final AgendamentoProperties propriedades;
    private final AgendaConsultas agenda;
    private final Clock relogio;

    public ValidaLimiteConsultas(AgendamentoProperties propriedades, AgendaConsultas agenda, Clock relogio) {
        this.propriedades = propriedades;
        this.agenda = agenda;
        this.relogio = relogio;
    }

    @Override
    protected void verificar(ContextoAgendamento contexto) {
        if (contexto.motivo().getGravidade() == Gravidade.VERMELHO) {
            return;
        }
        long futuras = agenda.contarConsultasAtivasFuturas(contexto.paciente().getId(), LocalDateTime.now(relogio));
        if (futuras >= propriedades.limiteConsultasFuturas()) {
            throw new RegraDeNegocioException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "limite de consultas futuras atingido");
        }
    }
}
