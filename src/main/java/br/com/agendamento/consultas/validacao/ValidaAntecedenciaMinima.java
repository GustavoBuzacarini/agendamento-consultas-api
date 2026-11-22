package br.com.agendamento.consultas.validacao;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import br.com.agendamento.consultas.motivo.Gravidade;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

/** AC-007: só agenda com a antecedência mínima configurada; caso VERMELHO é encaixe e dispensa (ASM-002). */
public class ValidaAntecedenciaMinima extends ValidadorAgendamento {

    private final AgendamentoProperties propriedades;
    private final Clock relogio;

    public ValidaAntecedenciaMinima(AgendamentoProperties propriedades, Clock relogio) {
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    @Override
    protected void verificar(ContextoAgendamento contexto) {
        if (contexto.motivo().getGravidade() == Gravidade.VERMELHO) {
            return;
        }
        int horas = propriedades.antecedenciaMinimaHoras();
        LocalDateTime maisCedoPermitido = LocalDateTime.now(relogio).plusHours(horas);
        if (contexto.inicio().isBefore(maisCedoPermitido)) {
            throw new RegraDeNegocioException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "antecedência mínima de " + horas + (horas == 1 ? " hora" : " horas") + " não respeitada");
        }
    }
}
