package src.semantico;

/**
 * Información que devuelve cada línea de un bloque en el parser.
 *
 * <p>Además del código intermedio indica si la línea corta el flujo, es decir,
 * si la ejecución nunca puede seguir a la línea siguiente.
 */
public class ResultadoSentencia {
    private String codigo;
    private boolean cortaFlujo;

    /**
     * Crea el resultado de una línea.
     *
     * @param codigo código intermedio de la línea
     * @param cortaFlujo {@code true} si la ejecución no puede seguir después de la línea
     */
    public ResultadoSentencia(String codigo, boolean cortaFlujo) {
        this.codigo = codigo;
        this.cortaFlujo = cortaFlujo;
    }

    /** Devuelve el código intermedio de la línea. */
    public String getCodigo() {
        return codigo;
    }

    /** Indica si la ejecución no puede seguir después de la línea. */
    public boolean cortaFlujo() {
        return cortaFlujo;
    }
}
