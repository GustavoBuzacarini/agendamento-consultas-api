package br.com.agendamento.consultas.profissional;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profissional")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String especialidade;

    @Column(name = "duracao_consulta_min", nullable = false)
    private int duracaoConsultaMin;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expediente", joinColumns = @JoinColumn(name = "profissional_id"))
    private List<Expediente> expediente = new ArrayList<>();

    protected Profissional() {
    }

    public Profissional(String nome, String especialidade, int duracaoConsultaMin, List<Expediente> expediente) {
        this.nome = nome;
        this.especialidade = especialidade;
        this.duracaoConsultaMin = duracaoConsultaMin;
        this.expediente = new ArrayList<>(expediente);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public int getDuracaoConsultaMin() {
        return duracaoConsultaMin;
    }

    public List<Expediente> getExpediente() {
        return List.copyOf(expediente);
    }
}
