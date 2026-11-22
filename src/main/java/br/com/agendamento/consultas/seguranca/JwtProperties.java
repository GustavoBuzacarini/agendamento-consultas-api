package br.com.agendamento.consultas.seguranca;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuração do token. O segredo vem só do ambiente (JWT_SECRET, constituição P-002) e a
 * aplicação se recusa a iniciar sem ele ou com um segredo curto demais para HS256 (AC-038).
 */
@ConfigurationProperties(prefix = "seguranca.jwt")
public record JwtProperties(String segredo, @DefaultValue("1h") Duration validade) {

    public static final int TAMANHO_MINIMO_DO_SEGREDO = 32;

    public JwtProperties {
        if (segredo == null || segredo.length() < TAMANHO_MINIMO_DO_SEGREDO) {
            throw new IllegalStateException(
                    "JWT_SECRET deve ter ao menos " + TAMANHO_MINIMO_DO_SEGREDO + " caracteres");
        }
    }
}
