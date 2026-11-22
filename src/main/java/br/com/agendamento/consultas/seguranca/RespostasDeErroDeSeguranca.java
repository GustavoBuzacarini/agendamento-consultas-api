package br.com.agendamento.consultas.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Respostas 401 e 403 no mesmo formato de erro da API (application/problem+json, RFC 9457). */
@Component
public class RespostasDeErroDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta,
                         AuthenticationException erro) throws IOException {
        String autorizacao = requisicao.getHeader(HttpHeaders.AUTHORIZATION);
        boolean veioComToken = autorizacao != null && autorizacao.regionMatches(true, 0, "Bearer ", 0, 7);
        resposta.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        escrever(resposta, 401, "Unauthorized",
                veioComToken ? "token inválido ou expirado" : "autenticação necessária");
    }

    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta,
                       AccessDeniedException erro) throws IOException {
        escrever(resposta, 403, "Forbidden", "sem permissão para esta operação");
    }

    private static void escrever(HttpServletResponse resposta, int status, String titulo, String detalhe)
            throws IOException {
        resposta.setStatus(status);
        resposta.setContentType("application/problem+json");
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resposta.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + titulo + "\",\"status\":" + status
                + ",\"detail\":\"" + detalhe + "\"}");
    }
}
