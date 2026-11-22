package br.com.agendamento.consultas.validacao;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Relógio da clínica (America/Sao_Paulo, ASM-007) e ativação das propriedades de agendamento. */
@Configuration
@EnableConfigurationProperties(AgendamentoProperties.class)
public class ConfiguracaoAgendamento {

    @Bean
    Clock relogio() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
