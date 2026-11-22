package br.com.agendamento.consultas.seguranca;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * API sem sessão (stateless): quem entra recebe um JWT e o envia no cabeçalho Authorization.
 * Regras de acesso por endereço e perfil; a checagem de "dono da consulta" fica no controller.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SegurancaConfig {

    /** Custo do BCrypt: 12 (a OWASP pede ao menos 10; ver ADR da autenticação). */
    static final int CUSTO_DO_BCRYPT = 12;

    @Bean
    PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder(CUSTO_DO_BCRYPT);
    }

    @Bean
    JwtDecoder jwtDecoder(JwtService jwtService) {
        return jwtService.decodificador();
    }

    @Bean
    SecurityFilterChain filtroDeSeguranca(HttpSecurity http, JwtDecoder decodificador,
                                          RespostasDeErroDeSeguranca respostas) throws Exception {
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(jwt -> List.<GrantedAuthority>of(
                new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("perfil"))));

        http
                .csrf(AbstractHttpConfigurer::disable) // sem cookie de sessão, o token vai no cabeçalho
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/senha").authenticated()
                        .requestMatchers(HttpMethod.GET, "/consultas/minhas").hasRole(Perfil.PACIENTE.name())
                        .requestMatchers(HttpMethod.GET, "/consultas/*")
                        .hasAnyRole(Perfil.RECEPCAO.name(), Perfil.PACIENTE.name())
                        .requestMatchers(HttpMethod.PATCH, "/consultas/*/cancelar")
                        .hasAnyRole(Perfil.RECEPCAO.name(), Perfil.PACIENTE.name())
                        .anyRequest().hasRole(Perfil.RECEPCAO.name()))
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.decoder(decodificador).jwtAuthenticationConverter(conversor))
                        .authenticationEntryPoint(respostas)
                        .accessDeniedHandler(respostas))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(respostas)
                        .accessDeniedHandler(respostas));
        return http.build();
    }
}
