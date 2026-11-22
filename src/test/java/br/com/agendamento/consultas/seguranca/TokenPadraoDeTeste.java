package br.com.agendamento.consultas.seguranca;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

/**
 * Faz o MockMvc padrão dos testes enviar o token da recepção em toda requisição. Assim os testes da
 * fase 1 continuam provando o mesmo comportamento, agora com a API protegida. Testes sem token ou
 * com outro perfil usam {@code mvcAnonimo} da IntegrationTestBase.
 */
@TestConfiguration
public class TokenPadraoDeTeste {

    @Bean
    MockMvcBuilderCustomizer tokenDaRecepcaoPorPadrao(JwtService jwt) {
        String token = jwt.emitir("recepcao-de-teste", Perfil.RECEPCAO, null).valor();
        return construtor -> construtor.defaultRequest(get("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }
}
