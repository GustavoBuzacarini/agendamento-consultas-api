package br.com.agendamento.consultas.validacao;

import java.time.LocalDateTime;

/**
 * O que a cadeia de validação precisa saber sobre as consultas já existentes. É implementada
 * pela persistência de consultas (T-008); nos testes da cadeia, por uma versão em memória.
 * Consulta ativa = AGENDADA ou CONFIRMADA (ASM-008).
 */
public interface AgendaConsultas {

    /** Há consulta ativa do profissional que se sobreponha ao período [inicio, fim)? */
    boolean existeConsultaAtivaNoPeriodo(Long profissionalId, LocalDateTime inicio, LocalDateTime fim);

    /** Quantas consultas ativas futuras (início a partir de {@code agora}) o paciente tem. */
    long contarConsultasAtivasFuturas(Long pacienteId, LocalDateTime agora);
}
