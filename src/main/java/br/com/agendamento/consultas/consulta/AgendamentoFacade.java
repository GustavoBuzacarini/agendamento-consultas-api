package br.com.agendamento.consultas.consulta;

import br.com.agendamento.consultas.paciente.Paciente;
import br.com.agendamento.consultas.paciente.PacienteService;
import br.com.agendamento.consultas.prioridade.CalculadoraPrioridade;
import br.com.agendamento.consultas.prioridade.Prioridade;
import br.com.agendamento.consultas.profissional.Profissional;
import br.com.agendamento.consultas.profissional.ProfissionalService;
import br.com.agendamento.consultas.validacao.CadeiaDeValidacao;
import br.com.agendamento.consultas.validacao.ContextoAgendamento;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * Padrão Facade: ponto único usado pelo controller. Esconde a orquestração entre cadastros,
 * cadeia de validação (Chain of Responsibility), cálculo de prioridade (Strategy), estados
 * (State) e persistência.
 */
@Service
public class AgendamentoFacade {

    private final PacienteService pacientes;
    private final ProfissionalService profissionais;
    private final CadeiaDeValidacao validacao;
    private final CalculadoraPrioridade prioridades;
    private final AgendamentoService consultas;
    private final Clock relogio;

    public AgendamentoFacade(PacienteService pacientes, ProfissionalService profissionais,
                             CadeiaDeValidacao validacao, CalculadoraPrioridade prioridades,
                             AgendamentoService consultas, Clock relogio) {
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.validacao = validacao;
        this.prioridades = prioridades;
        this.consultas = consultas;
        this.relogio = relogio;
    }

    public Consulta agendar(Long pacienteId, Long profissionalId, Long motivoId, LocalDateTime inicio) {
        Paciente paciente = pacientes.buscar(pacienteId);
        Profissional profissional = profissionais.buscar(profissionalId);

        ContextoAgendamento contexto = new ContextoAgendamento(paciente, profissional, motivoId, inicio);
        validacao.validar(contexto);

        Prioridade prioridade = prioridades.calcular(paciente, contexto.motivo(), inicio.toLocalDate());
        return consultas.criar(new Consulta(pacienteId, profissionalId, motivoId, inicio, contexto.fim(),
                prioridade, LocalDateTime.now(relogio)));
    }

    public Consulta buscar(Long id) {
        return consultas.buscar(id);
    }

    public java.util.List<Consulta> listarDoPaciente(Long pacienteId) {
        return consultas.listarDoPaciente(pacienteId);
    }

    public Consulta confirmar(Long id) {
        return consultas.confirmar(id);
    }

    public Consulta cancelar(Long id) {
        return consultas.cancelar(id);
    }

    public Consulta realizar(Long id) {
        return consultas.realizar(id);
    }

    public Consulta marcarFalta(Long id) {
        return consultas.marcarFalta(id);
    }
}
