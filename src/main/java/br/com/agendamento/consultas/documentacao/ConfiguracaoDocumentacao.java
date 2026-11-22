package br.com.agendamento.consultas.documentacao;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Texto da documentação da API e o esquema de login (Bearer JWT) para o botão "Authorize" da página. */
@Configuration
public class ConfiguracaoDocumentacao {

    static final String ESQUEMA_DE_LOGIN = "bearerAuth";

    @Bean
    OpenAPI documentacaoDaApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Agendamento de Consultas")
                        .version("fase 2")
                        .description("""
                                Agendamento de consultas de UBS ou clínica, com prioridade de atendimento \
                                (idoso 60+, gestante, pessoa com deficiência e gravidade por cor: VERDE, AMARELO, \
                                VERMELHO) e busca de endereço por CEP.

                                **Como testar:** entre em `POST /auth/login`, copie o `token` da resposta, clique em \
                                **Authorize** e cole o token. Perfis: RECEPCAO faz tudo; PACIENTE vê e cancela só as \
                                próprias consultas. A senha provisória de um paciente aparece uma única vez, quando a \
                                recepção cria a conta."""))
                .components(new Components().addSecuritySchemes(ESQUEMA_DE_LOGIN, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_DE_LOGIN));
    }
}
