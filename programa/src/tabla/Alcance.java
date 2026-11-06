package src.tabla;

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

    /**
     * Agrega un símbolo a este alcance.
     *
     * @param simbolo símbolo por agregar
     */
    public void agregar(Simbolo simbolo) {
        simbolos.add(simbolo);
    }

    /**
     * Busca un símbolo declarado directamente en este alcance.
     *
     * @param nombreBuscado nombre del símbolo
     * @return el símbolo encontrado, o {@code null} si no está en este alcance
     */
    public Simbolo buscar(String nombreBuscado) {
        for (Simbolo simbolo : simbolos) {
            if (simbolo.getNombre().equals(nombreBuscado)) {
                return simbolo;
            }
        }
        return null;
    }

    /** Devuelve el nombre descriptivo del alcance. */
    public String getNombre() {
        return nombre;
    }

    /** Devuelve el alcance que contiene a este, o {@code null} si es el global. */
    public Alcance getPadre() {
        return padre;
    }

    /** Devuelve los símbolos declarados en este alcance. */
    public ArrayList<Simbolo> getSimbolos() {
        return simbolos;
    }
}
