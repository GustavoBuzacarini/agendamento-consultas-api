package br.com.agendamento.consultas.seguranca;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

/** Emite e valida o token de acesso (JWT HS256) usando o relógio da aplicação. */
@Service
public class JwtService {

    /** Token emitido e o instante em que expira. */
    public record Token(String valor, Instant expiraEm) {
    }

    private final JwtProperties propriedades;
    private final Clock relogio;
    private final SecretKey chave;
    private final JwtEncoder codificador;

    public JwtService(JwtProperties propriedades, Clock relogio) {
        this.propriedades = propriedades;
        this.relogio = relogio;
        this.chave = new SecretKeySpec(propriedades.segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.codificador = new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    public Token emitir(String usuario, Perfil perfil, Long pacienteId) {
        return emitir(usuario, perfil, pacienteId, relogio.instant(), propriedades.validade());
    }

    /** Variante com instante e validade explícitos (usada para fabricar tokens vencidos nos testes). */
    public Token emitir(String usuario, Perfil perfil, Long pacienteId, Instant emissao, Duration validade) {
        Instant expiracao = emissao.plus(validade);
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(usuario)
                .issuedAt(emissao)
                .expiresAt(expiracao)
                .claim("perfil", perfil.name());
        if (pacienteId != null) {
            claims.claim("pacienteId", pacienteId);
        }
        String valor = codificador.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
        return new Token(valor, expiracao);
    }

    /** Decodificador que só aceita HS256 e confere a validade com o mesmo relógio da aplicação. */
    public JwtDecoder decodificador() {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(chave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtTimestampValidator validade = new JwtTimestampValidator(Duration.ZERO);
        validade.setClock(relogio);
        decodificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validade));
        return decodificador;
    }
}
