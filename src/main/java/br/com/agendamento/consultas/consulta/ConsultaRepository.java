package br.com.agendamento.consultas.consulta;

import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    java.util.List<Consulta> findByPacienteIdOrderByInicio(Long pacienteId);

    @Query("""
            select count(c) > 0 from Consulta c
            where c.profissionalId = :profissionalId and c.status in :ativas
              and c.inicio < :fim and c.fim > :inicio""")
    boolean existeAtivaNoPeriodo(@Param("profissionalId") Long profissionalId,
                                 @Param("inicio") LocalDateTime inicio,
                                 @Param("fim") LocalDateTime fim,
                                 @Param("ativas") Collection<StatusConsulta> ativas);

    @Query("""
            select count(c) from Consulta c
            where c.pacienteId = :pacienteId and c.status in :ativas and c.inicio >= :agora""")
    long contarAtivasFuturas(@Param("pacienteId") Long pacienteId,
                             @Param("agora") LocalDateTime agora,
                             @Param("ativas") Collection<StatusConsulta> ativas);
}
