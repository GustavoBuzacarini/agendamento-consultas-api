package br.com.agendamento.consultas.cep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Testa o cliente HTTP real contra um servidor local que imita o ViaCEP (sem internet). */
class ViaCepClientTest {

    private HttpServer servidor;
    private ViaCepClient cliente;

    @BeforeEach
    void subirServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        responder("/01001000/json/", 200, """
                {"cep":"01001-000","logradouro":"Praça da Sé","complemento":"lado ímpar","bairro":"Sé",
                 "localidade":"São Paulo","uf":"SP","ibge":"3550308"}""");
        responder("/99999999/json/", 200, "{\"erro\": \"true\"}");
        responder("/11111111/json/", 400, "<h1>Bad Request</h1>");
        responder("/22222222/json/", 500, "erro interno");
        servidor.start();
        cliente = new ViaCepClient("http://127.0.0.1:" + servidor.getAddress().getPort());
    }

    @AfterEach
    void pararServidor() {
        servidor.stop(0);
    }

    private void responder(String caminho, int codigo, String corpo) {
        servidor.createContext(caminho, troca -> {
            byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            troca.sendResponseHeaders(codigo, bytes.length);
            troca.getResponseBody().write(bytes);
            troca.close();
        });
    }

    @Test
    @DisplayName("CEP existente vira um Endereco com os campos do ViaCEP")
    void cepExistenteViraEndereco() {
        Optional<Endereco> endereco = cliente.consultar("01001000");

        assertEquals(new Endereco("01001000", "Praça da Sé", "Sé", "São Paulo", "SP"), endereco.orElseThrow());
    }

    @Test
    @DisplayName("Resposta com erro, ou 400 do ViaCEP, significa CEP não encontrado")
    void cepInexistenteVemVazio() {
        assertTrue(cliente.consultar("99999999").isEmpty());
        assertTrue(cliente.consultar("11111111").isEmpty());
    }

    @Test
    @DisplayName("Falha do servidor externo vira 503 com mensagem clara")
    void falhaDoServidorVira503() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class, () -> cliente.consultar("22222222"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, erro.getStatus());
        assertEquals("serviço de consulta de CEP indisponível, tente novamente", erro.getMessage());
    }

    @Test
    @DisplayName("Servidor inalcançável vira 503")
    void servidorInalcancavelVira503() {
        servidor.stop(0);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class, () -> cliente.consultar("01001000"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, erro.getStatus());
    }
}
