package src;

/**
 * Da los nombres de temporales y los números de etiqueta que usa el código
 * intermedio de tres direcciones.
 */
public class GeneradorCodigo {
    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;

    /**
     * Crea un temporal nuevo.
     *
     * @return nombre del temporal (t1, t2, ...)
     */
    public String nuevoTemporal() {
        contadorTemporales++;
        return "t" + contadorTemporales;
    }

    /**
     * Reserva un número para las etiquetas de una estructura de control.
     *
     * <p>Todas las etiquetas de una misma estructura usan el mismo número, por
     * ejemplo while_2, while_body_2 y while_end_2.
     *
     * @return número de etiqueta que no se ha usado
     */
    public int nuevoNumeroEtiqueta() {
        contadorEtiquetas++;
        return contadorEtiquetas;
    }

    /**
     * Convierte un texto en una instrucción de código intermedio (una línea).
     *
     * @param texto instrucción, por ejemplo "t3 = t1 + t2"
     * @return la instrucción terminada en salto de línea
     */
    public static String instruccion(String texto) {
        return texto + "\n";
    }
}
