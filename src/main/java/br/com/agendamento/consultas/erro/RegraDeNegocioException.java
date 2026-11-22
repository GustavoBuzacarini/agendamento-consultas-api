package br.com.agendamento.consultas.erro;

import org.springframework.http.HttpStatus;

/** Erro previsto pelas regras do negócio; carrega o status HTTP e a mensagem mostrada ao usuário. */
public class RegraDeNegocioException extends RuntimeException {

    private final HttpStatus status;

    public RegraDeNegocioException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
