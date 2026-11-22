package br.com.agendamento.consultas.motivo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MotivoRepository extends JpaRepository<MotivoAtendimento, Long> {

    boolean existsByDescricaoIgnoreCase(String descricao);
}
