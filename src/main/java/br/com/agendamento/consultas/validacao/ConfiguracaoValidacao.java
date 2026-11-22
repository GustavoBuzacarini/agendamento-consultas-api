package br.com.agendamento.consultas.validacao;

import br.com.agendamento.consultas.motivo.MotivoRepository;
import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Monta a cadeia de validação na ordem das regras: motivo, expediente, conflito, antecedência, limite. */
@Configuration
public class ConfiguracaoValidacao {

    @Bean
    CadeiaDeValidacao cadeiaDeValidacao(MotivoRepository motivos, AgendaConsultas agenda,
                                        AgendamentoProperties propriedades, Clock relogio) {
        return new CadeiaDeValidacao(List.of(
                new ValidaMotivoExistente(motivos),
                new ValidaExpediente(),
                new ValidaConflitoHorario(agenda),
                new ValidaAntecedenciaMinima(propriedades, relogio),
                new ValidaLimiteConsultas(propriedades, agenda, relogio)));
    }
}
