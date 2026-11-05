package src.semantico;

import java.util.ArrayList;
import java.util.HashMap;
import src.tabla.Simbolo;
import src.tabla.SimboloArreglo;
import src.tabla.SimboloFuncion;
import src.tabla.TablaSimbolos;

/**
 * Comprobaciones semánticas que se ejecutan desde las acciones del parser:
 * declaraciones, tipos, funciones y flujo.
 *
 * <p>Las líneas y columnas que reciben los métodos llegan desde 0, como las
 * entrega CUP; en los mensajes y en la tabla se muestran desde 1.
 */
public class AnalizadorSemantico {
    private TablaSimbolos tabla = new TablaSimbolos();
    private HashMap<String, SimboloFuncion> funcionesDeclaradas = new HashMap<>();
    private boolean huboErrores = false;
    private SimboloFuncion funcionActual;
    private boolean funcionTieneReturn = false;
    private int erroresSintacticos = 0;
    private int erroresSintacticosAlIniciarFuncion = 0;
    private int ciclosAbiertos = 0;

    /** Guarda las firmas de funciones obtenidas antes del análisis. */
    public void setFuncionesDeclaradas(HashMap<String, SimboloFuncion> funcionesDeclaradas) {
        this.funcionesDeclaradas = funcionesDeclaradas;
    }

    /** Devuelve la tabla de símbolos construida durante el análisis. */
    public TablaSimbolos getTablaSimbolos() {
        return tabla;
    }

    /** Indica si se reportó algún error semántico. */
    public boolean huboErrores() {
        return huboErrores;
    }

    /** Lleva la cuenta de los errores sintácticos que reporta el parser. */
    public void contarErrorSintactico() {
        erroresSintacticos++;
    }

    /** Imprime un error semántico con su posición. */
    public void reportarError(String mensaje, int linea, int columna) {
        huboErrores = true;
        System.err.println("Error semantico en la linea " + (linea + 1)
                + ", columna " + (columna + 1) + ": " + mensaje);
    }

    /** Abre un alcance nuevo en la tabla de símbolos. */
    public void abrirAlcance(String nombre) {
        tabla.abrirAlcance(nombre);
    }

    /** Cierra el alcance actual de la tabla de símbolos. */
    public void cerrarAlcance() {
        tabla.cerrarAlcance();
    }

    /** Registra que empezó un while o un for. */
    public void entrarACiclo() {
        ciclosAbiertos++;
    }

    /** Registra que terminó un while o un for. */
    public void salirDeCiclo() {
        ciclosAbiertos--;
    }

    // Declaraciones

    /** Agrega una variable o parámetro, si no está repetido en el alcance actual. */
    public void agregarSimbolo(String nombre, String categoria, String tipo, int linea, int columna) {
        if (!estaDuplicado(nombre, linea, columna)) {
            tabla.agregar(new Simbolo(nombre, categoria, tipo, linea + 1, columna + 1));
        }
    }

    /** Agrega un arreglo, si no está repetido en el alcance actual. */
    public void agregarArreglo(String nombre, String tipo, int tamano, int linea, int columna) {
        if (!estaDuplicado(nombre, linea, columna)) {
            tabla.agregar(new SimboloArreglo(nombre, tipo, tamano, linea + 1, columna + 1));
        }
    }

    /** Agrega una función y la marca como la función que se está analizando. */
    public void agregarFuncion(String nombre, String tipoRetorno, int linea, int columna) {
        funcionActual = new SimboloFuncion(nombre, tipoRetorno, linea + 1, columna + 1);
        funcionTieneReturn = false;
        erroresSintacticosAlIniciarFuncion = erroresSintacticos;
        if (!estaDuplicado(nombre, linea, columna)) {
            tabla.agregar(funcionActual);
        }
    }

    /** Agrega el tipo de un parámetro a la función que se está analizando. */
    public void agregarTipoParametro(String tipo) {
        funcionActual.agregarTipoParametro(tipo);
    }

    private boolean estaDuplicado(String nombre, int linea, int columna) {
        if (tabla.buscarEnAlcanceActual(nombre) == null) {
            return false;
        }
        reportarError("el identificador '" + nombre + "' ya fue declarado en este alcance", linea, columna);
        return true;
    }

    /** Valida el tipo y el tamaño de un arreglo. */
    public void validarArreglo(String nombre, String tipo, int tamano, int linea, int columna) {
        if (!esTipoDeArreglo(tipo)) {
            reportarError("el arreglo '" + nombre + "' solo puede ser de tipo int o float", linea, columna);
        }
        if (tamano <= 0) {
            reportarError("el tamano del arreglo '" + nombre + "' debe ser mayor que 0", linea, columna);
        }
    }

    /** Valida que los valores iniciales de un arreglo sean de su tipo y llenen su tamaño. */
    public void validarValoresArreglo(String nombre, String tipo, int tamano, String tipoValores, int cantidad,
            int linea, int columna) {
        if (esTipoDeArreglo(tipo) && !tipo.equals(tipoValores)) {
            reportarError("el arreglo '" + nombre + "' es de tipo " + tipo
                    + " pero sus valores son de tipo " + tipoValores, linea, columna);
        }
        if (tamano > 0 && cantidad != tamano) {
            reportarError("el arreglo '" + nombre + "' tiene tamano " + tamano
                    + " pero recibe " + cantidad + " valores", linea, columna);
        }
    }

    private boolean esTipoDeArreglo(String tipo) {
        return tipo.equals("int") || tipo.equals("float");
    }

    /** Valida que una función retorne int, float, char o bool. */
    public void validarTipoRetorno(String nombre, String tipo, int linea, int columna) {
        if (!tipo.equals("int") && !tipo.equals("float") && !tipo.equals("char") && !tipo.equals("bool")) {
            reportarError("la funcion '" + nombre + "' no puede retornar " + tipo
                    + "; solo se permite int, float, char o bool", linea, columna);
        }
    }

    // Uso de identificadores

    private Simbolo buscarDeclarado(String nombre, int linea, int columna) {
        Simbolo simbolo = tabla.buscar(nombre);
        if (simbolo == null) {
            reportarError("el identificador '" + nombre + "' no ha sido declarado", linea, columna);
        }
        return simbolo;
    }

    /** Devuelve el tipo de una variable o parámetro usado sin índice, o error. */
    public String tipoDeVariable(String nombre, int linea, int columna) {
        Simbolo simbolo = buscarDeclarado(nombre, linea, columna);
        if (simbolo == null) {
            return ResultadoExpresion.TIPO_ERROR;
        }
        if (simbolo.getCategoria().equals("arreglo")) {
            reportarError("el arreglo '" + nombre + "' debe usarse con un indice", linea, columna);
            return ResultadoExpresion.TIPO_ERROR;
        }
        if (simbolo.getCategoria().equals("funcion")) {
            reportarError("'" + nombre + "' es una funcion y no puede usarse como variable", linea, columna);
            return ResultadoExpresion.TIPO_ERROR;
        }
        return simbolo.getTipo();
    }

    /** Devuelve el tipo de los elementos de un arreglo usado con índice, o error. */
    public String tipoDeArreglo(String nombre, int linea, int columna) {
        Simbolo simbolo = buscarDeclarado(nombre, linea, columna);
        if (simbolo == null) {
            return ResultadoExpresion.TIPO_ERROR;
        }
        if (!simbolo.getCategoria().equals("arreglo")) {
            reportarError("'" + nombre + "' no es un arreglo", linea, columna);
            return ResultadoExpresion.TIPO_ERROR;
        }
        return simbolo.getTipo();
    }

    // Operaciones

    private boolean aceptaTipoAritmetico(String operador, String tipo) {
        if (operador.equals("/")) {
            return tipo.equals("float");
        }
        if (operador.equals("//") || operador.equals("mod") || operador.equals("pot")) {
            return tipo.equals("int");
        }
        return tipo.equals("int") || tipo.equals("float");
    }

    private boolean aceptaTipoRelacional(String operador, String tipo) {
        if (operador.equals("==") || operador.equals("!=")) {
            return tipo.equals("int") || tipo.equals("float") || tipo.equals("bool") || tipo.equals("char");
        }
        return tipo.equals("int") || tipo.equals("float");
    }

    private ResultadoExpresion errorDeOperandos(String operador, ResultadoExpresion izquierdo,
            ResultadoExpresion derecho, int linea, int columna) {
        reportarError("el operador '" + operador + "' no acepta operandos de tipo "
                + izquierdo.getTipo() + " y " + derecho.getTipo(), linea, columna);
        return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
    }

    /** Valida una operación aritmética binaria y devuelve su tipo. */
    public ResultadoExpresion operacionAritmetica(String operador, ResultadoExpresion izquierdo,
            ResultadoExpresion derecho, int linea, int columna) {
        if (izquierdo.esError() || derecho.esError()) {
            return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
        }
        String tipo = izquierdo.getTipo();
        if (!tipo.equals(derecho.getTipo()) || !aceptaTipoAritmetico(operador, tipo)) {
            return errorDeOperandos(operador, izquierdo, derecho, linea, columna);
        }
        return new ResultadoExpresion(tipo);
    }

    /** Valida una potencia: ambos int y exponente que no sea un literal negativo. */
    public ResultadoExpresion operacionPotencia(ResultadoExpresion base, ResultadoExpresion exponente,
            int linea, int columna) {
        ResultadoExpresion resultado = operacionAritmetica("pot", base, exponente, linea, columna);
        if (!resultado.esError() && exponente.esLiteralNegativo()) {
            reportarError("el exponente de 'pot' no puede ser negativo", linea, columna);
            return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
        }
        return resultado;
    }

    /** Valida una operación relacional; el resultado es bool. */
    public ResultadoExpresion operacionRelacional(String operador, ResultadoExpresion izquierdo,
            ResultadoExpresion derecho, int linea, int columna) {
        if (izquierdo.esError() || derecho.esError()) {
            return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
        }
        String tipo = izquierdo.getTipo();
        if (!tipo.equals(derecho.getTipo()) || !aceptaTipoRelacional(operador, tipo)) {
            return errorDeOperandos(operador, izquierdo, derecho, linea, columna);
        }
        return new ResultadoExpresion("bool");
    }

    /** Valida una conjunción o disyunción; el resultado es bool. */
    public ResultadoExpresion operacionLogica(String operador, ResultadoExpresion izquierdo,
            ResultadoExpresion derecho, int linea, int columna) {
        if (izquierdo.esError() || derecho.esError()) {
            return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
        }
        if (!izquierdo.getTipo().equals("bool") || !derecho.getTipo().equals("bool")) {
            return errorDeOperandos(operador, izquierdo, derecho, linea, columna);
        }
        return new ResultadoExpresion("bool");
    }

    /** Valida una negación lógica; el resultado es bool. */
    public ResultadoExpresion negacionLogica(ResultadoExpresion operando, int linea, int columna) {
        if (operando.esError()) {
            return operando;
        }
        if (!operando.getTipo().equals("bool")) {
            reportarError("el operador 'Σ' no acepta un operando de tipo " + operando.getTipo(), linea, columna);
            return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
        }
        return new ResultadoExpresion("bool");
    }

    /** Valida ++ o -- sobre una variable int o float. */
    public ResultadoExpresion operacionIncremento(String operador, String nombre, int linea, int columna) {
        String tipo = tipoDeVariable(nombre, linea, columna);
        if (tipo.equals(ResultadoExpresion.TIPO_ERROR)) {
            return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
        }
        if (!tipo.equals("int") && !tipo.equals("float")) {
            reportarError("el operador '" + operador + "' solo se aplica a variables int o float y '"
                    + nombre + "' es de tipo " + tipo, linea, columna);
            return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
        }
        return new ResultadoExpresion(tipo);
    }

    // Sentencias

    /** Valida que el valor asignado sea del tipo de la variable. */
    public void validarAsignacion(String nombre, String tipoDestino, ResultadoExpresion valor,
            int linea, int columna) {
        if (tipoDestino.equals(ResultadoExpresion.TIPO_ERROR) || valor.esError()) {
            return;
        }
        if (!tipoDestino.equals(valor.getTipo())) {
            reportarError("no se puede asignar un valor de tipo " + valor.getTipo() + " a '"
                    + nombre + "', que es de tipo " + tipoDestino, linea, columna);
        }
    }

    /** Valida que el índice de un arreglo sea int. */
    public void validarIndice(ResultadoExpresion indice, int linea, int columna) {
        if (!indice.esError() && !indice.getTipo().equals("int")) {
            reportarError("el indice de un arreglo debe ser de tipo int, no " + indice.getTipo(), linea, columna);
        }
    }

    /** Valida que la condición de if, elif, while o for sea bool. */
    public void validarCondicion(ResultadoExpresion condicion, int linea, int columna) {
        if (!condicion.esError() && !condicion.getTipo().equals("bool")) {
            reportarError("la condicion debe ser de tipo bool, no " + condicion.getTipo(), linea, columna);
        }
    }

    /** Valida que break esté dentro de un ciclo. */
    public void validarBreak(int linea, int columna) {
        if (ciclosAbiertos == 0) {
            reportarError("break solo se puede usar dentro de un while o un for", linea, columna);
        }
    }

    /** Valida que read lea una variable int, float o char. */
    public void validarTipoRead(String nombre, String tipo, int linea, int columna) {
        if (tipo.equals("string") || tipo.equals("bool")) {
            reportarError("read solo puede leer variables int, float o char y '" + nombre
                    + "' es de tipo " + tipo, linea, columna);
        }
    }

    /** Valida que write no escriba una variable char. */
    public void validarTipoWrite(String nombre, String tipo, int linea, int columna) {
        if (tipo.equals("char")) {
            reportarError("write solo puede escribir string, int, bool o float y '" + nombre
                    + "' es de tipo char", linea, columna);
        }
    }

    // Funciones

    /** Valida una llamada a función y devuelve el tipo de retorno de la función. */
    public ResultadoExpresion llamadaFuncion(String nombre, ArrayList<ResultadoExpresion> argumentos,
            int linea, int columna) {
        Simbolo simbolo = tabla.buscar(nombre);
        if (simbolo != null && !simbolo.getCategoria().equals("funcion")) {
            reportarError("'" + nombre + "' no es una funcion", linea, columna);
            return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
        }
        SimboloFuncion funcion = funcionesDeclaradas.get(nombre);
        if (funcion == null) {
            reportarError("la funcion '" + nombre + "' no ha sido declarada", linea, columna);
            return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
        }
        validarArgumentos(funcion, argumentos, linea, columna);
        return new ResultadoExpresion(funcion.getTipo());
    }

    private void validarArgumentos(SimboloFuncion funcion, ArrayList<ResultadoExpresion> argumentos,
            int linea, int columna) {
        ArrayList<String> tiposParametros = funcion.getTiposParametros();
        if (argumentos.size() != tiposParametros.size()) {
            reportarError("la funcion '" + funcion.getNombre() + "' espera " + tiposParametros.size()
                    + " argumentos y recibe " + argumentos.size(), linea, columna);
            return;
        }
        for (int i = 0; i < argumentos.size(); i++) {
            ResultadoExpresion argumento = argumentos.get(i);
            String tipoEsperado = tiposParametros.get(i);
            if (!argumento.esError() && !argumento.getTipo().equals(tipoEsperado)) {
                reportarError("el argumento " + (i + 1) + " de '" + funcion.getNombre()
                        + "' debe ser de tipo " + tipoEsperado + ", no " + argumento.getTipo(), linea, columna);
            }
        }
    }

    /** Valida que el valor de return sea del tipo de la función actual. */
    public void validarReturn(ResultadoExpresion valor, int linea, int columna) {
        funcionTieneReturn = true;
        String tipoFuncion = funcionActual.getTipo();
        if (tipoFuncion.equals("void")) {
            reportarError("'" + funcionActual.getNombre() + "' es de tipo void y no puede tener return",
                    linea, columna);
            return;
        }
        if (!valor.esError() && !valor.getTipo().equals(tipoFuncion)) {
            reportarError("la funcion '" + funcionActual.getNombre() + "' debe retornar " + tipoFuncion
                    + ", no " + valor.getTipo(), linea, columna);
        }
    }

    /**
     * Valida que la función que termina haya tenido al menos un return. Si hubo
     * errores de sintaxis en la función no se revisa, porque el return pudo
     * haberse descartado al recuperarse del error.
     */
    public void validarFuncionConReturn() {
        boolean huboErroresEnFuncion = erroresSintacticos > erroresSintacticosAlIniciarFuncion;
        if (!funcionTieneReturn && !huboErroresEnFuncion) {
            reportarError("la funcion '" + funcionActual.getNombre() + "' no tiene ningun return",
                    funcionActual.getLinea() - 1, funcionActual.getColumna() - 1);
        }
    }
}
