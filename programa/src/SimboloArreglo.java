package src;

/**
 * Símbolo de un arreglo estático; además de la información básica guarda su tamaño.
 */
public class SimboloArreglo extends Simbolo {
    private int tamano;

    /**
     * Crea el símbolo de un arreglo.
     *
     * @param nombre nombre del arreglo
     * @param tipo tipo de sus elementos
     * @param tamano cantidad de elementos
     * @param linea línea de la declaración, empezando en 1
     * @param columna columna de la declaración, empezando en 1
     */
    public SimboloArreglo(String nombre, String tipo, int tamano, int linea, int columna) {
        super(nombre, "arreglo", tipo, linea, columna);
        this.tamano = tamano;
    }

    @Override
    public String getInformacionExtra() {
        return "tamano: " + tamano;
    }
}
