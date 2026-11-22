package br.com.agendamento.consultas;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Congela "agora" em terça-feira, 2026-12-01, 10:00 (America/Sao_Paulo), para testes determinísticos. */
@TestConfiguration
public class RelogioDeTeste {

    public static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    @Bean
    @Primary
    Clock relogioFixo() {
        return Clock.fixed(IntegrationTestBase.AGORA.atZone(ZONA).toInstant(), ZONA);
    }
}
