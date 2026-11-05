package src.codigo;

import java.math.BigDecimal;
import java.util.HashMap;
import src.semantico.ResultadoExpresion;

/**
 * Da los nombres de temporales y los números de etiqueta que usa el código
 * intermedio de tres direcciones.
 */
public class GeneradorCodigo {
    private HashMap<String, Integer> contadoresTemporales = new HashMap<>();
    private int contadorEtiquetas = 0;

    /**
     * Crea un temporal nuevo para un valor del tipo indicado. Cada tipo tiene
     * su propia letra y su propio contador.
     *
     * @param tipo tipo del valor que guarda el temporal
     * @return nombre del temporal (i1, f1, b1, c1, s1, ...)
     */
    public String nuevoTemporal(String tipo) {
        String prefijo = prefijoDeTipo(tipo);
        int numero = contadoresTemporales.getOrDefault(prefijo, 0) + 1;
        contadoresTemporales.put(prefijo, numero);
        return prefijo + numero;
    }

    /**
     * Letra con la que empiezan los temporales de cada tipo.
     *
     * @param tipo tipo del valor
     * @return la letra del tipo, o "t" si la expresión tiene error de tipo
     */
    private static String prefijoDeTipo(String tipo) {
        switch (tipo) {
            case "int":
                return "i";
            case "float":
                return "f";
            case "bool":
                return "b";
            case "char":
                return "c";
            case "string":
                return "s";
            default:
                return "t";
        }
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
     * Carga un literal o el valor de una variable en un temporal nuevo, por
     * ejemplo "i1 = 5" o "i2 = x".
     *
     * @param tipo tipo del valor cargado
     * @param valor literal o nombre de la variable
     * @return resultado con el temporal como dirección y la instrucción de carga
     */
    public ResultadoExpresion cargarEnTemporal(String tipo, String valor) {
        String temporal = nuevoTemporal(tipo);
        return new ResultadoExpresion(tipo, temporal, instruccion(temporal + " = " + valor));
    }

    /**
     * Genera una operación binaria: el código de ambos operandos y la
     * instrucción que guarda el resultado en un temporal nuevo.
     *
     * @param tipoResultado tipo del resultado de la operación
     * @param izquierdo operando izquierdo
     * @param operador símbolo del operador
     * @param derecho operando derecho
     * @return resultado con el temporal de la operación y todo su código
     */
    public ResultadoExpresion operacionBinaria(String tipoResultado, ResultadoExpresion izquierdo,
            String operador, ResultadoExpresion derecho) {
        String temporal = nuevoTemporal(tipoResultado);
        String operacion = temporal + " = " + izquierdo.getDireccion() + " " + operador + " "
                + derecho.getDireccion();
        String codigo = izquierdo.getCodigo() + derecho.getCodigo() + instruccion(operacion);
        return new ResultadoExpresion(tipoResultado, temporal, codigo);
    }

    /**
     * Genera una operación unaria: el código del operando y la instrucción que
     * guarda el resultado en un temporal nuevo.
     *
     * @param tipoResultado tipo del resultado de la operación
     * @param operador símbolo del operador
     * @param operando operando de la operación
     * @return resultado con el temporal de la operación y todo su código
     */
    public ResultadoExpresion operacionUnaria(String tipoResultado, String operador,
            ResultadoExpresion operando) {
        String temporal = nuevoTemporal(tipoResultado);
        String operacion = temporal + " = " + operador + " " + operando.getDireccion();
        String codigo = operando.getCodigo() + instruccion(operacion);
        return new ResultadoExpresion(tipoResultado, temporal, codigo);
    }

    /**
     * Genera un incremento o decremento de una variable. El valor de la
     * expresión es el que tenía la variable antes de cambiar.
     *
     * @param tipo tipo de la variable (int o float)
     * @param nombre nombre de la variable
     * @param operador "+" para ++ o "-" para --
     * @return resultado con el temporal del valor anterior y todo el código
     */
    public ResultadoExpresion incremento(String tipo, String nombre, String operador) {
        String textoUno = "1";
        if (tipo.equals("float")) {
            textoUno = "1.0";
        }
        ResultadoExpresion valorAnterior = cargarEnTemporal(tipo, nombre);
        ResultadoExpresion uno = cargarEnTemporal(tipo, textoUno);
        ResultadoExpresion valorNuevo = operacionBinaria(tipo, valorAnterior, operador, uno);
        String codigo = valorNuevo.getCodigo()
                + instruccion(nombre + " = " + valorNuevo.getDireccion());
        return new ResultadoExpresion(tipo, valorAnterior.getDireccion(), codigo);
    }

    /**
     * Escribe un literal flotante sin notación científica
     *
     * @param valor valor del literal
     * @return el literal como texto
     */
    public static String textoFlotante(Double valor) {
        return BigDecimal.valueOf(valor).toPlainString();
    }

    /**
     * Convierte un texto en una instrucción de código intermedio (una línea).
     *
     * @param texto instrucción, por ejemplo "i3 = i1 + i2"
     * @return la instrucción terminada en salto de línea
     */
    public static String instruccion(String texto) {
        return texto + "\n";
    }
}
