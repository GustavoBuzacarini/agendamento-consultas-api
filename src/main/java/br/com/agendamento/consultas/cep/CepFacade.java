package br.com.agendamento.consultas.cep;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Padrão Facade (como no lab do curso): uma operação simples, {@code buscar(cep)}, que esconde
 * a normalização do CEP, a chamada HTTP ao serviço externo, a leitura da resposta e os erros.
 */
@Service
public class CepFacade {

    private final CepClient cliente;

    public CepFacade(CepClient cliente) {
        this.cliente = cliente;
    }

    /** Aceita "01001-000" ou "01001000"; recusa CEP inexistente (422) ou serviço fora do ar (503). */
    public Endereco buscar(String cep) {
        String digitos = cep.replaceAll("\\D", "");
        if (digitos.length() != 8) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST, "CEP deve ter 8 dígitos");
        }
        return cliente.consultar(digitos)
                .orElseThrow(() -> new RegraDeNegocioException(HttpStatus.UNPROCESSABLE_CONTENT, "CEP não encontrado"));
    }
}
