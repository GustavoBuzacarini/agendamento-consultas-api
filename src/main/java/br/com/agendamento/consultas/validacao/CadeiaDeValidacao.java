package br.com.agendamento.consultas.validacao;

import java.util.List;

/**
 * Monta os elos na ordem recebida e dispara a validação pelo primeiro.
 * Ordem prevista: motivo, expediente, conflito de horário, antecedência, limite.
 */
public class CadeiaDeValidacao {

    private final ValidadorAgendamento primeiro;

    public CadeiaDeValidacao(List<ValidadorAgendamento> elos) {
        if (elos.isEmpty()) {
            throw new IllegalArgumentException("a cadeia precisa de ao menos um elo");
        }
        this.primeiro = elos.get(0);
        ValidadorAgendamento atual = primeiro;
        for (ValidadorAgendamento elo : elos.subList(1, elos.size())) {
            atual = atual.encadear(elo);
        }
    }

    public void validar(ContextoAgendamento contexto) {
        primeiro.validar(contexto);
    }
}
