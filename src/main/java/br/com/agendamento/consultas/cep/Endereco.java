package br.com.agendamento.consultas.cep;

import jakarta.persistence.Embeddable;

/** Endereço obtido pela consulta de CEP; guardado junto do paciente. */
@Embeddable
public record Endereco(String cep, String logradouro, String bairro, String cidade, String uf) {
}
