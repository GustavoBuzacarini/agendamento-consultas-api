package br.com.agendamento.consultas.conta;

import java.security.SecureRandom;

/** Gera a senha provisória (10 caracteres, sem letras e números que se confundem, como 0/O e 1/l). */
public final class SenhaProvisoria {

    static final int TAMANHO = 10;
    private static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private SenhaProvisoria() {
    }

    public static String gerar() {
        StringBuilder senha = new StringBuilder(TAMANHO);
        for (int i = 0; i < TAMANHO; i++) {
            senha.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return senha.toString();
    }
}
