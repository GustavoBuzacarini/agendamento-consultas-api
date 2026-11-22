package br.com.agendamento.consultas.cep;

import java.util.Optional;

/**
 * Acesso a um serviço externo de CEP. Devolve vazio quando o CEP não existe e lança
 * RegraDeNegocioException (503) quando o serviço está indisponível.
 */
public interface CepClient {

    /** @param cep somente os 8 dígitos */
    Optional<Endereco> consultar(String cep);
}
