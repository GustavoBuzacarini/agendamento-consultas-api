package br.com.agendamento.consultas.consulta;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import br.com.agendamento.consultas.prioridade.Prioridade;
import br.com.agendamento.consultas.seguranca.UsuarioLogado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    public record ConsultaRequest(
            @NotNull Long pacienteId,
            @NotNull Long profissionalId,
            @NotNull Long motivoId,
            @NotNull LocalDateTime inicio) {
    }

    public record ConsultaResponse(Long id, Long pacienteId, Long profissionalId, Long motivoId,
                                   LocalDateTime inicio, LocalDateTime fim,
                                   StatusConsulta status, Prioridade prioridade) {

        static ConsultaResponse de(Consulta c) {
            return new ConsultaResponse(c.getId(), c.getPacienteId(), c.getProfissionalId(), c.getMotivoId(),
                    c.getInicio(), c.getFim(), c.getStatus(), c.getPrioridade());
        }
    }

    private final AgendamentoFacade facade;

    public ConsultaController(AgendamentoFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<ConsultaResponse> agendar(@Valid @RequestBody ConsultaRequest r) {
        Consulta criada = facade.agendar(r.pacienteId(), r.profissionalId(), r.motivoId(), r.inicio());
        return ResponseEntity.created(URI.create("/consultas/" + criada.getId())).body(ConsultaResponse.de(criada));
    }

    /** Fase 2: o paciente logado lista só as próprias consultas. */
    @GetMapping("/minhas")
    public List<ConsultaResponse> minhas() {
        return facade.listarDoPaciente(UsuarioLogado.atual().pacienteId()).stream()
                .map(ConsultaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ConsultaResponse buscar(@PathVariable Long id) {
        Consulta consulta = facade.buscar(id);
        exigirRecepcaoOuDono(consulta);
        return ConsultaResponse.de(consulta);
    }

    /** Fase 2: o paciente só mexe na própria consulta; a recepção, em qualquer uma. */
    private static void exigirRecepcaoOuDono(Consulta consulta) {
        UsuarioLogado logado = UsuarioLogado.atual();
        if (!logado.ehRecepcao() && !consulta.getPacienteId().equals(logado.pacienteId())) {
            throw new RegraDeNegocioException(HttpStatus.FORBIDDEN, "sem permissão para esta operação");
        }
    }

    @PatchMapping("/{id}/confirmar")
    public ConsultaResponse confirmar(@PathVariable Long id) {
        return ConsultaResponse.de(facade.confirmar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ConsultaResponse cancelar(@PathVariable Long id) {
        exigirRecepcaoOuDono(facade.buscar(id));
        return ConsultaResponse.de(facade.cancelar(id));
    }

    @PatchMapping("/{id}/realizar")
    public ConsultaResponse realizar(@PathVariable Long id) {
        return ConsultaResponse.de(facade.realizar(id));
    }

    @PatchMapping("/{id}/faltou")
    public ConsultaResponse faltou(@PathVariable Long id) {
        return ConsultaResponse.de(facade.marcarFalta(id));
    }
}
