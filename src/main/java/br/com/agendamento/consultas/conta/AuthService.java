package br.com.agendamento.consultas.conta;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import br.com.agendamento.consultas.seguranca.JwtService;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    /** Limite do bcrypt: senhas maiores nem são comparadas. */
    public static final int SENHA_MAXIMA = 72;

    private static final String ERRO_DE_LOGIN = "usuário ou senha inválidos";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final JwtService jwt;
    /** Hash de uma senha qualquer, comparado quando o usuário não existe, para gastar o mesmo tempo. */
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder codificador, JwtService jwt) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.jwt = jwt;
        this.hashFicticio = codificador.encode("senha-ficticia-para-igualar-o-tempo");
    }

    /** Resultado do login: o token e o perfil de quem entrou. */
    public record Entrada(JwtService.Token token, Usuario usuario) {
    }

    @Transactional(readOnly = true)
    public Entrada entrar(String nome, String senha) {
        Optional<Usuario> encontrado = usuarios.findByUsuario(nome.trim());
        String hash = encontrado.map(Usuario::getSenhaHash).orElse(hashFicticio);
        boolean senhaConfere = senha.length() <= SENHA_MAXIMA && codificador.matches(senha, hash);
        if (encontrado.isEmpty() || !senhaConfere) {
            // mesma mensagem para usuário inexistente e senha errada: não revela quem tem conta
            throw new RegraDeNegocioException(HttpStatus.UNAUTHORIZED, ERRO_DE_LOGIN);
        }
        Usuario usuario = encontrado.get();
        return new Entrada(jwt.emitir(usuario.getUsuario(), usuario.getPerfil(), usuario.getPacienteId()), usuario);
    }

    /** Troca a própria senha: exige a senha atual e guarda só o hash da nova (US-011). */
    @Transactional
    public void trocarSenha(String nome, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarios.findByUsuario(nome)
                .orElseThrow(() -> new RegraDeNegocioException(HttpStatus.UNAUTHORIZED, "autenticação necessária"));
        if (senhaAtual.length() > SENHA_MAXIMA || !codificador.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new RegraDeNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "senha atual incorreta");
        }
        usuario.definirSenhaHash(codificador.encode(novaSenha));
    }
}
