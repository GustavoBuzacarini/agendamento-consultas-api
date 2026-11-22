package br.com.agendamento.consultas.seguranca;

/** Perfil de acesso: a recepção faz tudo; o paciente vê e cancela só as próprias consultas. */
public enum Perfil {
    RECEPCAO,
    PACIENTE
}
