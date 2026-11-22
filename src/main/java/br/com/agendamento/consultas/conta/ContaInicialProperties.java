package br.com.agendamento.consultas.conta;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Usuário e senha da primeira conta da recepção, vindos do ambiente (RECEPCAO_USUARIO_INICIAL/SENHA_INICIAL). */
@ConfigurationProperties(prefix = "recepcao.inicial")
public record ContaInicialProperties(String usuario, String senha) {
}
