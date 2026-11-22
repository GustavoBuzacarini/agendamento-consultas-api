package br.com.agendamento.consultas.cep;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente HTTP do ViaCEP (https://viacep.com.br): GET {url}/{cep}/json/. */
@Component
public class ViaCepClient implements CepClient {

    private static final Duration LIMITE_DE_TEMPO = Duration.ofSeconds(3);

    private final RestClient http;

    public ViaCepClient(@Value("${cep.viacep.url:https://viacep.com.br/ws}") String url) {
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(LIMITE_DE_TEMPO).build());
        fabrica.setReadTimeout(LIMITE_DE_TEMPO);
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    @Override
    public Optional<Endereco> consultar(String cep) {
        Map<?, ?> resposta;
        try {
            resposta = http.get().uri("/{cep}/json/", cep).retrieve().body(Map.class);
        } catch (HttpClientErrorException e) {
            return Optional.empty(); // o ViaCEP responde 400 para CEP que não reconhece
        } catch (RestClientException e) {
            throw new RegraDeNegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                    "serviço de consulta de CEP indisponível, tente novamente");
        }
        if (resposta == null || resposta.containsKey("erro")) {
            return Optional.empty(); // CEP com formato certo, mas inexistente: {"erro": true}
        }
        return Optional.of(new Endereco(cep, texto(resposta, "logradouro"), texto(resposta, "bairro"),
                texto(resposta, "localidade"), texto(resposta, "uf")));
    }

    private static String texto(Map<?, ?> resposta, String campo) {
        Object valor = resposta.get(campo);
        return valor == null ? null : valor.toString();
    }
}
