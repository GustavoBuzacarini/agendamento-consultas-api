package br.com.agendamento.consultas.validacao;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import br.com.agendamento.consultas.motivo.MotivoRepository;
import org.springframework.http.HttpStatus;

/** AC-009: o motivo informado precisa existir no catálogo; os elos seguintes usam o motivo encontrado. */
public class ValidaMotivoExistente extends ValidadorAgendamento {

    private final MotivoRepository motivos;

    public ValidaMotivoExistente(MotivoRepository motivos) {
        this.motivos = motivos;
    }

    @Override
    protected void verificar(ContextoAgendamento contexto) {
        contexto.definirMotivo(motivos.findById(contexto.motivoId())
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "motivo de atendimento não encontrado")));
    }
}
