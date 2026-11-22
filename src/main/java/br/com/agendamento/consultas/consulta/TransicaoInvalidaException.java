package br.com.agendamento.consultas.consulta;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import org.springframework.http.HttpStatus;

/** Tentativa de mudar o status da consulta de um jeito que o estado atual não permite (resposta 409). */
public class TransicaoInvalidaException extends RegraDeNegocioException {

    public TransicaoInvalidaException(StatusConsulta atual, String acao) {
        super(HttpStatus.CONFLICT,
                "transição de status inválida: não é possível " + acao + " uma consulta " + atual);
    }
}
