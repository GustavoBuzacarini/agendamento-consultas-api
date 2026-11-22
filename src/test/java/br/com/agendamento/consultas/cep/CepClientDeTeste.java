package br.com.agendamento.consultas.cep;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;

/** Serviço de CEP de mentira para os testes de API: não usa a internet. */
@TestConfiguration
public class CepClientDeTeste {

    public static final String CEP_EXISTENTE = "01001000";
    public static final String CEP_INEXISTENTE = "99999999";
    public static final String CEP_COM_SERVICO_FORA_DO_AR = "00000000";

    @Bean
    @Primary
    CepClient cepClientFalso() {
        return cep -> switch (cep) {
            case CEP_EXISTENTE -> Optional.of(new Endereco(cep, "Praça da Sé", "Sé", "São Paulo", "SP"));
            case CEP_COM_SERVICO_FORA_DO_AR -> throw new RegraDeNegocioException(
                    HttpStatus.SERVICE_UNAVAILABLE, "serviço de consulta de CEP indisponível, tente novamente");
            default -> Optional.empty();
        };
    }
}
