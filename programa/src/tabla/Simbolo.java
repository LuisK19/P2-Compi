package src.tabla;

/**
 * Representa un identificador declarado en el programa fuente.
 *
 * <p>Las variables y los parámetros usan esta clase directamente. Los arreglos
 * y las funciones usan sus subclases porque guardan información adicional.
 */
public class Simbolo {
    private String nombre;
    private String nombreEnCodigo;
    private String alcance;
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
        this.nombreEnCodigo = nombre;
        this.categoria = categoria;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
    }

    /** Devuelve el nombre del identificador en el código fuente. */
    public String getNombre() {
        return nombre;
    }

    /** Devuelve el nombre del identificador en el código intermedio. */
    public String getNombreEnCodigo() {
        return nombreEnCodigo;
    }

    /**
     * Cambia el nombre con el que el símbolo aparece en el código intermedio.
     *
     * @param nombreEnCodigo nombre que se usará en el código intermedio
     */
    public void setNombreEnCodigo(String nombreEnCodigo) {
        this.nombreEnCodigo = nombreEnCodigo;
    }

    /** Devuelve el nombre del alcance donde se declaró el símbolo. */
    public String getAlcance() {
        return alcance;
    }

    /**
     * Guarda el alcance donde se declaró el símbolo.
     *
     * @param alcance nombre del alcance
     */
    public void setAlcance(String alcance) {
        this.alcance = alcance;
    }

    /** Devuelve la categoría: variable, arreglo, funcion o parametro. */
    public String getCategoria() {
        return categoria;
    }

    /** Devuelve el tipo de dato declarado o, en funciones, el de retorno. */
    public String getTipo() {
        return tipo;
    }

    /** Devuelve la línea de la declaración, empezando en 1. */
    public int getLinea() {
        return linea;
    }

    /** Devuelve la columna de la declaración, empezando en 1. */
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
