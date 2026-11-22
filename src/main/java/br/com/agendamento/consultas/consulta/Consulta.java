package br.com.agendamento.consultas.consulta;

import br.com.agendamento.consultas.prioridade.Prioridade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "consulta")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "profissional_id", nullable = false)
    private Long profissionalId;

    @Column(name = "motivo_id", nullable = false)
    private Long motivoId;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridade prioridade;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    protected Consulta() {
    }

    public Consulta(Long pacienteId, Long profissionalId, Long motivoId, LocalDateTime inicio, LocalDateTime fim,
                    Prioridade prioridade, LocalDateTime criadaEm) {
        this.pacienteId = pacienteId;
        this.profissionalId = profissionalId;
        this.motivoId = motivoId;
        this.inicio = inicio;
        this.fim = fim;
        this.prioridade = prioridade;
        this.criadaEm = criadaEm;
        this.status = StatusConsulta.AGENDADA;
    }

    // As mudanças de status passam pelo padrão State (StatusConsulta): transição inválida lança exceção.

    public void confirmar() {
        status = status.confirmar();
    }

    public void cancelar() {
        status = status.cancelar();
    }

    public void realizar() {
        status = status.realizar();
    }

    public void marcarFalta() {
        status = status.marcarFalta();
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getProfissionalId() {
        return profissionalId;
    }

    public Long getMotivoId() {
        return motivoId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }
}
