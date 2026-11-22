package br.com.agendamento.consultas.validacao;

import br.com.agendamento.consultas.motivo.MotivoAtendimento;
import br.com.agendamento.consultas.paciente.Paciente;
import br.com.agendamento.consultas.profissional.Profissional;
import java.time.LocalDateTime;

/** Dados do pedido que percorrem a cadeia; o primeiro elo preenche o motivo encontrado. */
public class ContextoAgendamento {

    private final Paciente paciente;
    private final Profissional profissional;
    private final Long motivoId;
    private final LocalDateTime inicio;
    private MotivoAtendimento motivo;

    public ContextoAgendamento(Paciente paciente, Profissional profissional, Long motivoId, LocalDateTime inicio) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.motivoId = motivoId;
        this.inicio = inicio;
    }

    public Paciente paciente() {
        return paciente;
    }

    public Profissional profissional() {
        return profissional;
    }

    public Long motivoId() {
        return motivoId;
    }

    public LocalDateTime inicio() {
        return inicio;
    }

    public LocalDateTime fim() {
        return inicio.plusMinutes(profissional.getDuracaoConsultaMin());
    }

    /** Motivo já resolvido; só existe depois do elo de motivo. */
    public MotivoAtendimento motivo() {
        return motivo;
    }

    void definirMotivo(MotivoAtendimento motivo) {
        this.motivo = motivo;
    }
}
