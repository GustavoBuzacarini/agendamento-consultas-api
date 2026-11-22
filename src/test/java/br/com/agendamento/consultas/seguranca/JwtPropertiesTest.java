package br.com.agendamento.consultas.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class JwtPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(JwtProperties.class)
    static class Configuracao {
    }

    private final ApplicationContextRunner executor = new ApplicationContextRunner()
            .withUserConfiguration(Configuracao.class);

    @Test
    @DisplayName("@spec:AC-038 Sem chave de assinatura configurada, a aplicação não inicia")
    void semChaveAAplicacaoNaoInicia() {
        // ausente
        executor.run(contexto -> {
            assertThat(contexto).hasFailed();
            assertThat(contexto.getStartupFailure()).rootCause()
                    .hasMessageContaining("JWT_SECRET deve ter ao menos 32 caracteres");
        });
        // vazia (é o que acontece quando a variável de ambiente não existe)
        executor.withPropertyValues("seguranca.jwt.segredo=").run(contexto -> {
            assertThat(contexto).hasFailed();
            assertThat(contexto.getStartupFailure()).rootCause()
                    .hasMessageContaining("JWT_SECRET deve ter ao menos 32 caracteres");
        });
        // curta demais
        executor.withPropertyValues("seguranca.jwt.segredo=curta").run(contexto -> {
            assertThat(contexto).hasFailed();
            assertThat(contexto.getStartupFailure()).rootCause()
                    .hasMessageContaining("JWT_SECRET deve ter ao menos 32 caracteres");
        });
    }

    @Test
    @DisplayName("Chave com 32 caracteres ou mais é aceita, com validade padrão de 1 hora")
    void chaveDe32CaracteresEAceita() {
        executor.withPropertyValues("seguranca.jwt.segredo=" + "x".repeat(32)).run(contexto -> {
            assertThat(contexto).hasNotFailed();
            assertThat(contexto.getBean(JwtProperties.class).validade()).hasHours(1);
        });
    }
}
