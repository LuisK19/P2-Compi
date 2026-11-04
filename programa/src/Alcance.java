package src;

import java.util.ArrayList;

/**
 * Agrupa los símbolos declarados dentro de un mismo alcance.
 */
public class Alcance {
    private String nombre;
    private Alcance padre;
    private ArrayList<Simbolo> simbolos = new ArrayList<>();

    /**
     * Crea un alcance vacío.
     *
     * @param nombre nombre descriptivo del alcance (global, nombre de función, bloque...)
     * @param padre alcance que lo contiene, o {@code null} si es el global
     */
    public Alcance(String nombre, Alcance padre) {
        this.nombre = nombre;
        this.padre = padre;
    }

    public void agregar(Simbolo simbolo) {
        simbolos.add(simbolo);
    }

    public String getNombre() {
        return nombre;
    }

    public Alcance getPadre() {
        return padre;
    }

    public ArrayList<Simbolo> getSimbolos() {
        return simbolos;
    }
}
