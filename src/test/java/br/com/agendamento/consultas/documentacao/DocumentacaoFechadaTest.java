package br.com.agendamento.consultas.documentacao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.agendamento.consultas.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Configuração padrão (documentação não aberta): exige login como o resto da API. */
class DocumentacaoFechadaTest extends IntegrationTestBase {

    @Test
    @DisplayName("@spec:AC-042 Por padrão a documentação exige login")
    void porPadraoADocumentacaoExigeLogin() throws Exception {
        mvcAnonimo.perform(get("/v3/api-docs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("autenticação necessária"));
        mvcAnonimo.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("autenticação necessária"));

        // com o token da recepção, o resumo da API é entregue
        mvcAnonimo.perform(comToken(get("/v3/api-docs"), tokenDaRecepcao()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de Agendamento de Consultas"));
    }
}
