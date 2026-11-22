package br.com.agendamento.consultas.conta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import br.com.agendamento.consultas.seguranca.Perfil;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class LoginTest extends IntegrationTestBase {

    private static final String USUARIO = "recepcao";
    private static final String CREDENCIAL = "credencial-correta-1";

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder codificador;

    @Autowired
    private JwtDecoder decodificador;

    private void criarContaDaRecepcao() {
        usuarios.save(new Usuario(USUARIO, codificador.encode(CREDENCIAL), Perfil.RECEPCAO, null, AGORA));
    }

    @Test
    @DisplayName("@spec:AC-023 Login correto devolve um token de acesso de 1 hora")
    void loginCorretoDevolveTokenDe1Hora() throws Exception {
        criarContaDaRecepcao();

        String corpo = login(USUARIO, CREDENCIAL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.perfil").value("RECEPCAO"))
                // "agora" fixo dos testes = 10:00 em São Paulo (13:00 UTC); a validade é de 1 hora
                .andExpect(jsonPath("$.expiraEm").value("2026-12-01T14:00:00Z"))
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(corpo, "$.token");
        assertEquals("RECEPCAO", decodificador.decode(token).getClaimAsString("perfil"));

        // o token funciona de verdade: o paciente 999 não existe (404), mas a requisição foi aceita (não 401)
        mvcAnonimo.perform(comToken(get("/pacientes/999"), token)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-024 Usuário ou senha errados são recusados sem dar pistas")
    void credenciaisErradasSaoRecusadasSemPistas() throws Exception {
        criarContaDaRecepcao();

        login(USUARIO, "credencial-errada-9")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("usuário ou senha inválidos"));

        login("usuario-que-nao-existe", CREDENCIAL)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("usuário ou senha inválidos"));
    }

    @Test
    @DisplayName("Mede o tempo do login com BCrypt de custo 12 (só informativo)")
    void medeOTempoDoLogin() throws Exception {
        criarContaDaRecepcao();
        login(USUARIO, CREDENCIAL).andExpect(status().isOk()); // aquece a JVM

        int repeticoes = 5;
        long inicio = System.nanoTime();
        for (int i = 0; i < repeticoes; i++) {
            login(USUARIO, CREDENCIAL).andExpect(status().isOk());
        }
        long milissegundos = (System.nanoTime() - inicio) / 1_000_000 / repeticoes;
        System.out.println("[medida] login com BCrypt custo 12: " + milissegundos + " ms por login (média de "
                + repeticoes + ")");
    }
}
