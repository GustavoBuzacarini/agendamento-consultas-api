package br.com.agendamento.consultas.conta;

import br.com.agendamento.consultas.seguranca.Perfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Conta de acesso. Só guarda o hash da senha, nunca a senha. */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String usuario;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    /** Só para o perfil PACIENTE: o cadastro de paciente a que esta conta pertence. */
    @Column(name = "paciente_id", unique = true)
    private Long pacienteId;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Usuario() {
    }

    public Usuario(String usuario, String senhaHash, Perfil perfil, Long pacienteId, LocalDateTime criadoEm) {
        this.usuario = usuario;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.pacienteId = pacienteId;
        this.criadoEm = criadoEm;
    }

    public void definirSenhaHash(String novoHash) {
        this.senhaHash = novoHash;
    }

    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public Long getPacienteId() {
        return pacienteId;
    }
}
