package src.codigo;

import src.semantico.ResultadoExpresion;

/**
 * Partes del encabezado de un for que se acomodan alrededor del cuerpo al
 * generar el código del ciclo.
 */
public class EncabezadoFor {
    private String inicio;
    private ResultadoExpresion condicion;
    private String actualizacion;

    /**
     * Crea el encabezado de un for.
     *
     * @param inicio código de la declaración o asignación inicial
     * @param condicion expresión booleana que se evalúa en cada vuelta
     * @param actualizacion código que se ejecuta al final de cada vuelta
     */
    public EncabezadoFor(String inicio, ResultadoExpresion condicion, String actualizacion) {
        this.inicio = inicio;
        this.condicion = condicion;
        this.actualizacion = actualizacion;
    }

    public String getInicio() {
        return inicio;
    }

    public ResultadoExpresion getCondicion() {
        return condicion;
    }

    public String getActualizacion() {
        return actualizacion;
    }
}
