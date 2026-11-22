package br.com.agendamento.consultas.validacao;

/**
 * Padrão Chain of Responsibility: cada elo verifica uma regra e passa o pedido ao próximo.
 * Qualquer elo pode interromper a cadeia lançando uma RegraDeNegocioException.
 */
public abstract class ValidadorAgendamento {

    private ValidadorAgendamento proximo;

    /** Liga o próximo elo e o devolve, para encadear em sequência. */
    ValidadorAgendamento encadear(ValidadorAgendamento proximo) {
        this.proximo = proximo;
        return proximo;
    }

    public final void validar(ContextoAgendamento contexto) {
        verificar(contexto);
        if (proximo != null) {
            proximo.validar(contexto);
        }
    }

    protected abstract void verificar(ContextoAgendamento contexto);
}
