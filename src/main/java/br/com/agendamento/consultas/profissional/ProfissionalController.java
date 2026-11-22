package br.com.agendamento.consultas.profissional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    public record ProfissionalRequest(
            @NotBlank String nome,
            @NotBlank String especialidade,
            @Min(5) @Max(240) int duracaoConsultaMin,
            @NotEmpty List<@Valid Expediente> expediente) {
    }

    public record ProfissionalResponse(Long id, String nome, String especialidade, int duracaoConsultaMin,
                                       List<Expediente> expediente) {

        static ProfissionalResponse de(Profissional p) {
            return new ProfissionalResponse(p.getId(), p.getNome(), p.getEspecialidade(),
                    p.getDuracaoConsultaMin(), p.getExpediente());
        }
    }

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProfissionalResponse> cadastrar(@Valid @RequestBody ProfissionalRequest r) {
        Profissional criado = service.cadastrar(r.nome(), r.especialidade(), r.duracaoConsultaMin(), r.expediente());
        return ResponseEntity.created(URI.create("/profissionais/" + criado.getId()))
                .body(ProfissionalResponse.de(criado));
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscar(@PathVariable Long id) {
        return ProfissionalResponse.de(service.buscar(id));
    }
}
