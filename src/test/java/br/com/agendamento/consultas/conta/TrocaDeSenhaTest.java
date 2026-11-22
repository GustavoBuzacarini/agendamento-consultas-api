package br.com.agendamento.consultas.conta;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import br.com.agendamento.consultas.seguranca.Perfil;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

class TrocaDeSenhaTest extends IntegrationTestBase {

    private static final String USUARIO = "recepcao";
    private static final String CREDENCIAL_ATUAL = "credencial-atual-1";
    private static final String CREDENCIAL_NOVA = "credencial-nova-2";

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder codificador;

    private String token;

    @BeforeEach
    void entrar() throws Exception {
        usuarios.save(new Usuario(USUARIO, codificador.encode(CREDENCIAL_ATUAL), Perfil.RECEPCAO, null, AGORA));
        token = JsonPath.read(login(USUARIO, CREDENCIAL_ATUAL).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.token");
    }

    private ResultActions trocar(String atual, String nova) throws Exception {
        String corpo = """
                {"senhaAtual":"%s","novaSenha":"%s"}""".formatted(atual, nova);
        return mvcAnonimo.perform(comToken(post("/auth/senha"), token)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    @Test
    @DisplayName("@spec:AC-035 Usuário troca a própria senha informando a atual")
    void usuarioTrocaAPropriaSenha() throws Exception {
        trocar(CREDENCIAL_ATUAL, CREDENCIAL_NOVA).andExpect(status().isNoContent());

        login(USUARIO, CREDENCIAL_NOVA).andExpect(status().isOk());
        login(USUARIO, CREDENCIAL_ATUAL).andExpect(status().isUnauthorized());

        // o que fica no banco é só o hash da senha nova
        String hash = jdbc.queryForObject("select senha_hash from usuario where usuario = ?", String.class, USUARIO);
        assertTrue(hash.startsWith("$2"), "deve ser um hash BCrypt");
        assertNotEquals(CREDENCIAL_NOVA, hash);
    }

    @Test
    @DisplayName("@spec:AC-036 Troca com senha atual errada ou senha nova fraca é recusada")
    void trocaComSenhaAtualErradaOuNovaFracaERecusada() throws Exception {
        trocar("credencial-errada-9", CREDENCIAL_NOVA)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("senha atual incorreta"));

        trocar(CREDENCIAL_ATUAL, "curta")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("novaSenha")));

        // nada mudou: a senha de antes continua valendo e a nova não
        login(USUARIO, CREDENCIAL_ATUAL).andExpect(status().isOk());
        login(USUARIO, CREDENCIAL_NOVA).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sem token, trocar a senha é recusado")
    void semTokenNaoTrocaASenha() throws Exception {
        mvcAnonimo.perform(post("/auth/senha").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"x\",\"novaSenha\":\"yyyyyyyy\"}"))
                .andExpect(status().isUnauthorized());
    }
}
