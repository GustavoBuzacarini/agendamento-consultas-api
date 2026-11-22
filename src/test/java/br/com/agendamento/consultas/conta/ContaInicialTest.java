package br.com.agendamento.consultas.conta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import br.com.agendamento.consultas.seguranca.Perfil;
import java.time.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class ContaInicialTest extends IntegrationTestBase {

    private static final String USUARIO = "recepcao-inicial";
    private static final String CREDENCIAL = "credencial-inicial-1";

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder codificador;

    @Autowired
    private Clock relogio;

    private ContaInicialRecepcao runner(String usuario, String credencial) {
        return new ContaInicialRecepcao(usuarios, codificador, new ContaInicialProperties(usuario, credencial),
                relogio);
    }

    private long contasDaRecepcao() {
        return jdbc.queryForObject("select count(*) from usuario where perfil = 'RECEPCAO'", Long.class);
    }

    @Test
    @DisplayName("@spec:AC-037 A conta inicial da recepção é criada na primeira subida e não se duplica")
    void contaInicialCriadaUmaVezSo() throws Exception {
        assertEquals(0, contasDaRecepcao());

        runner(USUARIO, CREDENCIAL).run(null);

        assertEquals(1, contasDaRecepcao());
        login(USUARIO, CREDENCIAL).andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("RECEPCAO"));

        // segunda subida, mesmo com outros valores: não cria outra conta nem muda a senha existente
        runner("outro-usuario", "credencial-diferente-2").run(null);

        assertEquals(1, contasDaRecepcao());
        login(USUARIO, CREDENCIAL).andExpect(status().isOk());
        login("outro-usuario", "credencial-diferente-2").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sem as variáveis, nenhuma conta é criada e a aplicação segue de pé")
    void semVariaveisNaoCriaConta() {
        runner("", "").run(null);
        runner(null, null).run(null);

        assertEquals(0, contasDaRecepcao());
    }

    @Test
    @DisplayName("Senha inicial fora de 8 a 72 caracteres faz a subida falhar")
    void senhaInicialInvalidaFazASubidaFalhar() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> runner(USUARIO, "curta").run(null));

        assertEquals("RECEPCAO_SENHA_INICIAL deve ter entre 8 e 72 caracteres", erro.getMessage());
        assertEquals(0, contasDaRecepcao());
    }
}
