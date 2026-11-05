package src.tabla;

import java.util.ArrayList;

/**
 * Símbolo de una función; además de la información básica guarda los tipos de
 * sus parámetros en el orden en que se declaran.
 */
public class SimboloFuncion extends Simbolo {
    private ArrayList<String> tiposParametros = new ArrayList<>();

    /**
     * Crea el símbolo de una función sin parámetros; estos se agregan después.
     *
     * @param nombre nombre de la función
     * @param tipoRetorno tipo que devuelve la función
     * @param linea línea de la declaración, empezando en 1
     * @param columna columna de la declaración, empezando en 1
     */
    public SimboloFuncion(String nombre, String tipoRetorno, int linea, int columna) {
        super(nombre, "funcion", tipoRetorno, linea, columna);
    }

    public void agregarTipoParametro(String tipoParametro) {
        tiposParametros.add(tipoParametro);
    }

    public ArrayList<String> getTiposParametros() {
        return tiposParametros;
    }

    @Override
    public String getInformacionExtra() {
        if (tiposParametros.isEmpty()) {
            return "parametros: ninguno";
        }
        return "parametros: " + String.join(", ", tiposParametros);
    }
}
