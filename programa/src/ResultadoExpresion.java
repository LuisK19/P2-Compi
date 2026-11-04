package src;

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

    public String getTipo() {
        return tipo;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCodigo() {
        return codigo;
    }
}
