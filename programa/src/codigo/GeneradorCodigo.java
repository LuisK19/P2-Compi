package src.codigo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import src.semantico.ResultadoExpresion;

/**
 * Da los nombres de temporales y los números de etiqueta que usa el código
 * intermedio de tres direcciones.
 */
public class GeneradorCodigo {
    private HashMap<String, Integer> contadoresTemporales = new HashMap<>();
    private int contadorEtiquetas = 0;
    private Stack<Integer> numerosIf = new Stack<>();

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
        String codigo = asignacion(nombre, valorNuevo);
        return new ResultadoExpresion(tipo, valorAnterior.getDireccion(), codigo);
    }

    /**
     * Genera la asignación de una expresión a una variable: el código de la
     * expresión y la copia de su temporal a la variable.
     *
     * @param nombre nombre de la variable
     * @param valor expresión asignada
     * @return código de la asignación
     */
    public String asignacion(String nombre, ResultadoExpresion valor) {
        return valor.getCodigo() + instruccion(nombre + " = " + valor.getDireccion());
    }

    /**
     * Genera la asignación de una expresión a un elemento de arreglo: el código
     * del índice, el código del valor y la copia al elemento.
     *
     * @param nombre nombre del arreglo
     * @param indice expresión del índice
     * @param valor expresión asignada
     * @return código de la asignación
     */
    public String asignacionElemento(String nombre, ResultadoExpresion indice,
            ResultadoExpresion valor) {
        String copia = nombre + "[" + indice.getDireccion() + "] = " + valor.getDireccion();
        return indice.getCodigo() + valor.getCodigo() + instruccion(copia);
    }

    /**
     * Genera la asignación de los valores iniciales de un arreglo, un elemento
     * a la vez desde la posición 0. Las posiciones sin valor escrito reciben el
     * valor por defecto del tipo.
     *
     * @param nombre nombre del arreglo
     * @param tipo tipo de los elementos
     * @param tamano cantidad de elementos del arreglo
     * @param valores literales en el orden en que se escribieron (puede estar vacía)
     * @return código de las asignaciones
     */
    public String valoresIniciales(String nombre, String tipo, int tamano,
            ArrayList<String> valores) {
        String codigo = "";
        for (int posicion = 0; posicion < tamano; posicion++) {
            String textoValor = valorPorDefecto(tipo);
            if (posicion < valores.size()) {
                textoValor = valores.get(posicion);
            }
            ResultadoExpresion indice = cargarEnTemporal("int", String.valueOf(posicion));
            ResultadoExpresion valor = cargarEnTemporal(tipo, textoValor);
            codigo += asignacionElemento(nombre, indice, valor);
        }
        return codigo;
    }

    /**
     * Genera la instrucción que da el valor inicial a una variable declarada
     * sin valor.
     *
     * @param nombre nombre de la variable
     * @param tipo tipo de la variable
     * @return código de la inicialización
     */
    public String valorInicial(String nombre, String tipo) {
        return instruccion(nombre + " = " + valorPorDefecto(tipo));
    }

    /**
     * Valor con el que empieza una variable de cada tipo.
     *
     * @param tipo tipo de la variable
     * @return el valor por defecto como texto
     */
    private static String valorPorDefecto(String tipo) {
        switch (tipo) {
            case "float":
                return "0.0";
            case "bool":
                return "false";
            case "char":
                return "' '";
            case "string":
                return "\"\"";
            default:
                return "0";
        }
    }

    /**
     * Genera la lectura de un elemento de arreglo: el código del índice y la
     * instrucción que copia el elemento a un temporal nuevo.
     *
     * @param tipo tipo de los elementos del arreglo
     * @param nombre nombre del arreglo
     * @param indice expresión del índice
     * @return resultado con el temporal del elemento y todo su código
     */
    public ResultadoExpresion accesoArreglo(String tipo, String nombre, ResultadoExpresion indice) {
        String temporal = nuevoTemporal(tipo);
        String acceso = temporal + " = " + nombre + "[" + indice.getDireccion() + "]";
        return new ResultadoExpresion(tipo, temporal, indice.getCodigo() + instruccion(acceso));
    }

    /**
     * Reserva el número de etiquetas de un if que se empieza a leer. Se guarda
     * en una pila porque puede haber if dentro de otros if.
     */
    public void abrirIf() {
        numerosIf.push(nuevoNumeroEtiqueta());
    }

    /**
     * Genera un if completo con el número reservado en {@link #abrirIf()}. Si la
     * condición es verdadera se ejecuta el cuerpo; si no, las ramas elif y else.
     *
     * @param condicion expresión booleana de la condición
     * @param cuerpo código del bloque del if
     * @param ramasElif código de las ramas elif (vacío si no hay)
     * @param cuerpoElse código del bloque else (vacío si no hay)
     * @return código de toda la estructura
     */
    public String sentenciaIf(ResultadoExpresion condicion, String cuerpo, String ramasElif,
            String cuerpoElse) {
        int numero = numerosIf.pop();
        return condicion.getCodigo()
                + instruccion("if " + condicion.getDireccion() + " goto if_true_" + numero)
                + instruccion("goto if_false_" + numero)
                + etiqueta("if_true_" + numero)
                + cuerpo
                + instruccion("goto if_end_" + numero)
                + etiqueta("if_false_" + numero)
                + ramasElif
                + cuerpoElse
                + etiqueta("if_end_" + numero);
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

    /**
     * Convierte un nombre en una línea de etiqueta, por ejemplo "if_end_1:".
     *
     * @param nombre nombre de la etiqueta
     * @return la etiqueta terminada en salto de línea
     */
    public static String etiqueta(String nombre) {
        return instruccion(nombre + ":");
    }
}
