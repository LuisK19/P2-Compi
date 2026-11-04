package src;

/**
 * Representa un identificador declarado en el programa fuente.
 *
 * <p>Las variables y los parámetros usan esta clase directamente. Los arreglos
 * y las funciones usan sus subclases porque guardan información adicional.
 */
public class Simbolo {
    private String nombre;
    private String categoria;
    private String tipo;
    private int linea;
    private int columna;

    /**
     * Crea un símbolo con su información básica.
     *
     * @param nombre nombre del identificador
     * @param categoria variable, arreglo, funcion o parametro
     * @param tipo tipo de dato declarado (o de retorno, en funciones)
     * @param linea línea de la declaración, empezando en 1
     * @param columna columna de la declaración, empezando en 1
     */
    public Simbolo(String nombre, String categoria, String tipo, int linea, int columna) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getTipo() {
        return tipo;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    /**
     * Devuelve la información propia de cada tipo de símbolo.
     *
     * @return texto vacío; las subclases lo sobrescriben
     */
    public String getInformacionExtra() {
        return "";
    }
}
