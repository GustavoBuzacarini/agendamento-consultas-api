package br.com.agendamento.consultas.paciente;

import br.com.agendamento.consultas.cep.Endereco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.time.LocalDate;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    public record PacienteRequest(
            @NotBlank String nome,
            @NotBlank @CPF String cpf,
            @NotNull @Past LocalDate dataNascimento,
            Boolean gestante,
            Boolean pessoaComDeficiencia,
            @Pattern(regexp = "\\d{5}-?\\d{3}", message = "deve ter 8 dígitos (ex.: 01001-000)") String cep) {
    }

    public record PacienteResponse(Long id, String nome, String cpf, LocalDate dataNascimento,
                                   boolean gestante, boolean pessoaComDeficiencia, Endereco endereco) {

        static PacienteResponse de(Paciente p) {
            return new PacienteResponse(p.getId(), p.getNome(), p.getCpf(), p.getDataNascimento(),
                    p.isGestante(), p.isPessoaComDeficiencia(), p.getEndereco());
        }
    }

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrar(@Valid @RequestBody PacienteRequest r) {
        Paciente criado = service.cadastrar(r.nome(), r.cpf(), r.dataNascimento(),
                Boolean.TRUE.equals(r.gestante()), Boolean.TRUE.equals(r.pessoaComDeficiencia()), r.cep());
        return ResponseEntity.created(URI.create("/pacientes/" + criado.getId())).body(PacienteResponse.de(criado));
    }

    @GetMapping("/{id}")
    public PacienteResponse buscar(@PathVariable Long id) {
        return PacienteResponse.de(service.buscar(id));
    }
}
