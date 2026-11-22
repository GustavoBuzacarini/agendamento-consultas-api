package br.com.agendamento.consultas.conta;

import br.com.agendamento.consultas.seguranca.Perfil;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    Optional<Usuario> findByPacienteId(Long pacienteId);

    boolean existsByPerfil(Perfil perfil);
}
