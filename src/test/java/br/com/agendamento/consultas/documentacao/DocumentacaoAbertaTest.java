package br.com.agendamento.consultas.documentacao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Documentação ligada por configuração (DOCUMENTACAO_ABERTA=true): abre sem token. */
@TestPropertySource(properties = "documentacao.aberta=true")
class DocumentacaoAbertaTest extends IntegrationTestBase {

    @Test
    @DisplayName("@spec:AC-040 A documentação lista os endereços e prevê o uso do token")
    void documentacaoListaOsEnderecosEPrevOUsoDoToken() throws Exception {
        mvcAnonimo.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de Agendamento de Consultas"))
                .andExpect(jsonPath("$.paths['/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/pacientes']").exists())
                .andExpect(jsonPath("$.paths['/consultas']").exists())
                .andExpect(jsonPath("$.paths['/agenda']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"));
    }

    @Test
    @DisplayName("@spec:AC-041 A página interativa abre sem login quando a documentação está aberta")
    void paginaInterativaAbreSemLogin() throws Exception {
        mvcAnonimo.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Com a documentação aberta, os endereços de dados continuam exigindo login")
    void enderecosDeDadosContinuamProtegidos() throws Exception {
        mvcAnonimo.perform(get("/pacientes/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("autenticação necessária"));
    }
}
