package br.com.agendamento.consultas.seguranca;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import br.com.agendamento.consultas.RelogioDeTeste;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SegurancaTest extends IntegrationTestBase {

    private static final Instant INSTANTE_ATUAL = AGORA.atZone(RelogioDeTeste.ZONA).toInstant();

    @Test
    @DisplayName("@spec:AC-025 Sem token, a API recusa")
    void semTokenAApiRecusa() throws Exception {
        mvcAnonimo.perform(get("/pacientes/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("autenticação necessária"));
    }

    @Test
    @DisplayName("@spec:AC-026 Token vencido ou adulterado é recusado")
    void tokenVencidoOuAdulteradoERecusado() throws Exception {
        // emitido há 2 horas, com validade de 1 hora: venceu há 1 hora
        String vencido = jwt.emitir("recepcao-de-teste", Perfil.RECEPCAO, null,
                INSTANTE_ATUAL.minus(Duration.ofHours(2)), Duration.ofHours(1)).valor();
        mvcAnonimo.perform(comToken(get("/pacientes/1"), vencido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("token inválido ou expirado"));

        // conteúdo alterado (o paciente vira recepção) mantendo a assinatura antiga
        String[] partes = tokenDaRecepcao().split("\\.");
        String conteudoFalso = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"intruso\",\"perfil\":\"RECEPCAO\",\"exp\":9999999999}".getBytes(StandardCharsets.UTF_8));
        String adulterado = partes[0] + "." + conteudoFalso + "." + partes[2];
        mvcAnonimo.perform(comToken(get("/pacientes/1"), adulterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("token inválido ou expirado"));

        // um token ainda dentro da validade é aceito (o paciente 999 não existe: 404, não 401)
        String quaseVencendo = jwt.emitir("recepcao-de-teste", Perfil.RECEPCAO, null,
                INSTANTE_ATUAL.minus(Duration.ofMinutes(59)), Duration.ofHours(1)).valor();
        mvcAnonimo.perform(comToken(get("/pacientes/999"), quaseVencendo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-027 Login e saúde continuam abertos")
    void loginESaudeContinuamAbertos() throws Exception {
        mvcAnonimo.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // o login responde pelas credenciais (401 "usuário ou senha inválidos"), não por falta de token
        login("ninguem", "credencial-qualquer")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("usuário ou senha inválidos"));
    }
}
