package br.com.agendamento.consultas.validacao;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import br.com.agendamento.consultas.profissional.Expediente;
import java.time.LocalTime;
import org.springframework.http.HttpStatus;

/** AC-006: a consulta inteira precisa caber numa faixa de expediente do profissional naquele dia. */
public class ValidaExpediente extends ValidadorAgendamento {

    @Override
    protected void verificar(ContextoAgendamento contexto) {
        LocalTime inicio = contexto.inicio().toLocalTime();
        LocalTime fim = contexto.fim().toLocalTime();
        boolean viraODia = !contexto.fim().toLocalDate().equals(contexto.inicio().toLocalDate());

        boolean dentro = !viraODia && contexto.profissional().getExpediente().stream()
                .filter(e -> e.diaSemana() == contexto.inicio().getDayOfWeek())
                .anyMatch(e -> cabe(e, inicio, fim));

        if (!dentro) {
            throw new RegraDeNegocioException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "fora do expediente do profissional");
        }
    }

    private static boolean cabe(Expediente faixa, LocalTime inicio, LocalTime fim) {
        return !inicio.isBefore(faixa.horaInicio()) && !fim.isAfter(faixa.horaFim());
    }
}
