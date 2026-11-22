package br.com.agendamento.consultas.paciente;

import br.com.agendamento.consultas.cep.Endereco;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    /** Somente os 11 dígitos, sem pontuação. */
    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private boolean gestante;

    @Column(name = "pessoa_com_deficiencia", nullable = false)
    private boolean pessoaComDeficiencia;

    /** Opcional; preenchido pela consulta de CEP. */
    @Embedded
    private Endereco endereco;

    protected Paciente() {
    }

    public Paciente(String nome, String cpf, LocalDate dataNascimento, boolean gestante, boolean pessoaComDeficiencia) {
        this(nome, cpf, dataNascimento, gestante, pessoaComDeficiencia, null);
    }

    public Paciente(String nome, String cpf, LocalDate dataNascimento, boolean gestante, boolean pessoaComDeficiencia,
                    Endereco endereco) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.gestante = gestante;
        this.pessoaComDeficiencia = pessoaComDeficiencia;
        this.endereco = endereco;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    /** Idade completa na data informada. */
    public int idadeEm(LocalDate data) {
        return java.time.Period.between(dataNascimento, data).getYears();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public boolean isGestante() {
        return gestante;
    }

    public boolean isPessoaComDeficiencia() {
        return pessoaComDeficiencia;
    }
}
