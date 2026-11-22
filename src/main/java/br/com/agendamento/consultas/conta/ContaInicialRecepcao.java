package br.com.agendamento.consultas.conta;

import br.com.agendamento.consultas.seguranca.Perfil;
import java.time.Clock;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria a primeira conta da recepção na subida, a partir das variáveis de ambiente. Só age se ainda
 * não existir nenhuma conta de recepção, então subidas seguintes não criam outra nem mudam a senha.
 */
@Component
@EnableConfigurationProperties(ContaInicialProperties.class)
public class ContaInicialRecepcao implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ContaInicialRecepcao.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final ContaInicialProperties propriedades;
    private final Clock relogio;

    public ContaInicialRecepcao(UsuarioRepository usuarios, PasswordEncoder codificador,
                                ContaInicialProperties propriedades, Clock relogio) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (usuarios.existsByPerfil(Perfil.RECEPCAO)) {
            return;
        }
        String usuario = propriedades.usuario();
        String senha = propriedades.senha();
        if (usuario == null || usuario.isBlank() || senha == null || senha.isBlank()) {
            LOG.warn("Nenhuma conta da recepção existe e RECEPCAO_USUARIO_INICIAL/RECEPCAO_SENHA_INICIAL "
                    + "não estão definidas: ninguém conseguirá entrar até a configuração ser feita.");
            return;
        }
        if (senha.length() < 8 || senha.length() > AuthService.SENHA_MAXIMA) {
            throw new IllegalStateException("RECEPCAO_SENHA_INICIAL deve ter entre 8 e "
                    + AuthService.SENHA_MAXIMA + " caracteres");
        }
        usuarios.save(new Usuario(usuario.trim(), codificador.encode(senha), Perfil.RECEPCAO, null,
                LocalDateTime.now(relogio)));
        LOG.info("Conta inicial da recepção criada: {}", usuario.trim());
    }
}
