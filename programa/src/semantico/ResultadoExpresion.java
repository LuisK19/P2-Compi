package src.semantico;

/**
 * Información que devuelve cada regla de expresión del parser.
 *
 * <p>El tipo se usa en el análisis semántico; la dirección y el código se usan
 * en la generación de código intermedio.
 */
public class ResultadoExpresion {
    public static final String TIPO_ERROR = "error";

    private String tipo;
    private String direccion;
    private String codigo;
    private boolean literalNegativo = false;
    private Integer valorLiteral = null;

    /**
     * Crea un resultado que solo tiene tipo.
     *
     * @param tipo tipo de la expresión
     */
    public ResultadoExpresion(String tipo) {
        this(tipo, "", "");
    }

    /**
     * Crea un resultado completo.
     *
     * @param tipo tipo de la expresión
     * @param direccion temporal o nombre donde queda el valor de la expresión
     * @param codigo código intermedio que calcula la expresión
     */
    public ResultadoExpresion(String tipo, String direccion, String codigo) {
        this.tipo = tipo;
        this.direccion = direccion;
        this.codigo = codigo;
    }

    /** Devuelve el tipo de la expresión. */
    public String getTipo() {
        return tipo;
    }

    /** Devuelve el temporal o nombre donde queda el valor. */
    public String getDireccion() {
        return direccion;
    }

    /** Devuelve el código intermedio que calcula la expresión. */
    public String getCodigo() {
        return codigo;
    }

    /** Indica si la expresión tiene un error de tipo. */
    public boolean esError() {
        return tipo.equals(TIPO_ERROR);
    }

    /** Indica si la expresión es un literal negativo. */
    public boolean esLiteralNegativo() {
        return literalNegativo;
    }

    /** Marca la expresión como un literal negativo. */
    public void marcarLiteralNegativo() {
        literalNegativo = true;
    }

    /** Devuelve el valor si la expresión es un literal entero, o {@code null} si no lo es. */
    public Integer getValorLiteral() {
        return valorLiteral;
    }

    /** Guarda el valor de la expresión cuando es un literal entero. */
    public void marcarValorLiteral(int valor) {
        valorLiteral = valor;
    }
}
