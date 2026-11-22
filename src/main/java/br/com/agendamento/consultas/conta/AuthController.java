package br.com.agendamento.consultas.conta;

import br.com.agendamento.consultas.seguranca.Perfil;
import br.com.agendamento.consultas.seguranca.UsuarioLogado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public record LoginRequest(@NotBlank String usuario, @NotBlank String senha) {
    }

    public record LoginResponse(String token, String tipo, Instant expiraEm, Perfil perfil) {
    }

    public record TrocaDeSenhaRequest(
            @NotBlank String senhaAtual,
            @NotBlank @Size(min = 8, max = AuthService.SENHA_MAXIMA,
                    message = "deve ter de 8 a 72 caracteres") String novaSenha) {
    }

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse entrar(@Valid @RequestBody LoginRequest r) {
        AuthService.Entrada entrada = service.entrar(r.usuario(), r.senha());
        return new LoginResponse(entrada.token().valor(), "Bearer", entrada.token().expiraEm(),
                entrada.usuario().getPerfil());
    }

    @PostMapping("/senha")
    public ResponseEntity<Void> trocarSenha(@Valid @RequestBody TrocaDeSenhaRequest r) {
        service.trocarSenha(UsuarioLogado.atual().usuario(), r.senhaAtual(), r.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
