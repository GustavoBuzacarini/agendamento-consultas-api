package br.com.agendamento.consultas.seguranca;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/** Quem está logado nesta requisição, lido do token já validado. */
public record UsuarioLogado(String usuario, Perfil perfil, Long pacienteId) {

    public static UsuarioLogado atual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("nenhum usuário autenticado nesta requisição");
        }
        Number paciente = jwt.getClaim("pacienteId");
        return new UsuarioLogado(jwt.getSubject(), Perfil.valueOf(jwt.getClaimAsString("perfil")),
                paciente == null ? null : paciente.longValue());
    }

    public boolean ehRecepcao() {
        return perfil == Perfil.RECEPCAO;
    }
}
