package br.com.agendamento.consultas.motivo;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/motivos")
public class MotivoController {

    public record MotivoRequest(@NotBlank String descricao, @NotNull Gravidade gravidade) {
    }

    public record MotivoResponse(Long id, String descricao, Gravidade gravidade) {

        static MotivoResponse de(MotivoAtendimento m) {
            return new MotivoResponse(m.getId(), m.getDescricao(), m.getGravidade());
        }
    }

    private final MotivoRepository repository;

    public MotivoController(MotivoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<MotivoResponse> cadastrar(@Valid @RequestBody MotivoRequest r) {
        String descricao = r.descricao().trim();
        if (repository.existsByDescricaoIgnoreCase(descricao)) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT, "motivo de atendimento já cadastrado");
        }
        MotivoAtendimento criado = repository.saveAndFlush(new MotivoAtendimento(descricao, r.gravidade()));
        return ResponseEntity.created(URI.create("/motivos/" + criado.getId())).body(MotivoResponse.de(criado));
    }

    @GetMapping
    public List<MotivoResponse> listar() {
        return repository.findAll(Sort.by("descricao")).stream().map(MotivoResponse::de).toList();
    }
}
