package br.com.agendamento.consultas.conta;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import br.com.agendamento.consultas.paciente.Paciente;
import br.com.agendamento.consultas.paciente.PacienteService;
import br.com.agendamento.consultas.seguranca.Perfil;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A recepção cria a conta do paciente e gera novas senhas provisórias (US-009). */
@Service
public class ContaPacienteService {

    /** Usuário e senha provisória, mostrados uma única vez (a senha nunca é guardada em texto). */
    public record ContaCriada(String usuario, String senhaProvisoria) {
    }

    private final UsuarioRepository usuarios;
    private final PacienteService pacientes;
    private final PasswordEncoder codificador;
    private final Clock relogio;

    public ContaPacienteService(UsuarioRepository usuarios, PacienteService pacientes,
                                PasswordEncoder codificador, Clock relogio) {
        this.usuarios = usuarios;
        this.pacientes = pacientes;
        this.codificador = codificador;
        this.relogio = relogio;
    }

    @Transactional
    public ContaCriada criar(Long pacienteId) {
        Paciente paciente = pacientes.buscar(pacienteId); // 404 se não existir
        if (usuarios.findByPacienteId(pacienteId).isPresent()) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT, "paciente já possui conta");
        }
        String senha = SenhaProvisoria.gerar();
        usuarios.saveAndFlush(new Usuario(paciente.getCpf(), codificador.encode(senha), Perfil.PACIENTE,
                pacienteId, LocalDateTime.now(relogio)));
        return new ContaCriada(paciente.getCpf(), senha);
    }

    @Transactional
    public ContaCriada redefinirSenha(Long pacienteId) {
        pacientes.buscar(pacienteId); // 404 se o paciente não existir
        Usuario usuario = usuarios.findByPacienteId(pacienteId).orElseThrow(() -> new RegraDeNegocioException(
                HttpStatus.NOT_FOUND, "paciente ainda não possui conta"));
        String senha = SenhaProvisoria.gerar();
        usuario.definirSenhaHash(codificador.encode(senha));
        return new ContaCriada(usuario.getUsuario(), senha);
    }
}
