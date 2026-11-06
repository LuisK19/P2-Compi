package src.semantico;

import java.util.ArrayList;
import java.util.HashMap;
import src.tabla.ReporteTokens;
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
    private ReporteTokens reporteTokens = new ReporteTokens();
    private boolean huboErrores = false;
    private SimboloFuncion funcionActual;
    private boolean funcionTieneReturn = false;
    private int erroresSintacticos = 0;
    private int erroresSintacticosAlIniciarFuncion = 0;
    private int ciclosAbiertos = 0;

    /**
     * Guarda las firmas de funciones obtenidas antes del análisis.
     *
     * @param funcionesDeclaradas funciones del archivo, por nombre
     */
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

    /**
     * Imprime un error semántico con su posición.
     *
     * @param mensaje descripción del error
     * @param linea línea del error
     * @param columna columna del error
     */
    public void reportarError(String mensaje, int linea, int columna) {
        huboErrores = true;
        System.err.println("Error semantico en la linea " + (linea + 1)
                + ", columna " + (columna + 1) + ": " + mensaje);
    }

    /**
     * Imprime un error semántico que solo se puede ubicar por su línea.
     *
     * @param mensaje descripción del error
     * @param linea línea del error
     */
    private void reportarErrorDeLinea(String mensaje, int linea) {
        huboErrores = true;
        System.err.println("Error semantico en la linea " + (linea + 1) + ": " + mensaje);
    }

    /** Devuelve el reporte donde se registra la tabla de cada identificador. */
    public ReporteTokens getReporteTokens() {
        return reporteTokens;
    }

    /**
     * Abre un alcance nuevo en la tabla de símbolos.
     *
     * @param nombre nombre descriptivo del alcance
     */
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

    /**
     * Agrega una variable o parámetro, si no está repetido en el alcance actual.
     *
     * @param nombre nombre del identificador
     * @param categoria variable o parametro
     * @param tipo tipo de dato declarado
     * @param linea línea de la declaración
     * @param columna columna de la declaración
     */
    public void agregarSimbolo(String nombre, String categoria, String tipo, int linea, int columna) {
        if (!estaDuplicado(nombre, linea, columna)) {
            agregarVariable(new Simbolo(nombre, categoria, tipo, linea + 1, columna + 1));
        }
    }

    /**
     * Agrega un arreglo, si no está repetido en el alcance actual.
     *
     * @param nombre nombre del arreglo
     * @param tipo tipo de los elementos
     * @param tamano cantidad de elementos
     * @param linea línea de la declaración
     * @param columna columna de la declaración
     */
    public void agregarArreglo(String nombre, String tipo, int tamano, int linea, int columna) {
        if (!estaDuplicado(nombre, linea, columna)) {
            agregarVariable(new SimboloArreglo(nombre, tipo, tamano, linea + 1, columna + 1));
        }
    }

    /**
     * Agrega un símbolo y define su nombre en el código intermedio: las
     * globales llevan _global y una local que oculta a otra local lleva _local.
     *
     * @param simbolo símbolo por agregar
     */
    private void agregarVariable(Simbolo simbolo) {
        Simbolo oculto = tabla.buscar(simbolo.getNombre());
        if (tabla.estaEnAlcanceGlobal()) {
            simbolo.setNombreEnCodigo(simbolo.getNombre() + "_global");
        } else if (oculto != null && !oculto.getAlcance().equals("global")) {
            simbolo.setNombreEnCodigo(oculto.getNombreEnCodigo() + "_local");
        }
        tabla.agregar(simbolo);
        reporteTokens.registrarTabla(simbolo.getLinea() - 1, simbolo.getColumna() - 1, simbolo.getAlcance());
    }

    /**
     * Devuelve el nombre que tiene en el código intermedio el símbolo visible.
     *
     * @param nombre nombre del identificador en el código fuente
     * @return nombre en el código intermedio, o el mismo nombre si no está declarado
     */
    public String nombreEnCodigo(String nombre) {
        Simbolo simbolo = tabla.buscar(nombre);
        if (simbolo == null) {
            return nombre;
        }
        return simbolo.getNombreEnCodigo();
    }

    /**
     * Agrega una función y la marca como la función que se está analizando.
     *
     * @param nombre nombre de la función
     * @param tipoRetorno tipo que retorna la función
     * @param linea línea de la declaración
     * @param columna columna de la declaración
     */
    public void agregarFuncion(String nombre, String tipoRetorno, int linea, int columna) {
        funcionActual = new SimboloFuncion(nombre, tipoRetorno, linea + 1, columna + 1);
        funcionTieneReturn = false;
        erroresSintacticosAlIniciarFuncion = erroresSintacticos;
        if (!estaDuplicado(nombre, linea, columna)) {
            tabla.agregar(funcionActual);
            reporteTokens.registrarTabla(linea, columna, funcionActual.getAlcance());
        }
    }

    /**
     * Agrega el tipo de un parámetro a la función que se está analizando.
     *
     * @param tipo tipo del parámetro
     */
    public void agregarTipoParametro(String tipo) {
        funcionActual.agregarTipoParametro(tipo);
    }

    /**
     * Revisa si un nombre ya existe en el alcance actual y reporta el error.
     *
     * @param nombre nombre del identificador
     * @param linea línea de la declaración
     * @param columna columna de la declaración
     * @return {@code true} si el nombre ya estaba declarado
     */
    private boolean estaDuplicado(String nombre, int linea, int columna) {
        if (tabla.buscarEnAlcanceActual(nombre) == null) {
            return false;
        }
        reportarError("el identificador '" + nombre + "' ya fue declarado en este alcance", linea, columna);
        return true;
    }

    /**
     * Valida que un arreglo sea int o float y que su tamaño sea mayor que 0.
     *
     * @param nombre nombre del arreglo
     * @param tipo tipo de los elementos
     * @param tamano cantidad de elementos
     * @param linea línea de la declaración
     * @param columna columna de la declaración
     */
    public void validarArreglo(String nombre, String tipo, int tamano, int linea, int columna) {
        if (!esTipoDeArreglo(tipo)) {
            reportarError("el arreglo '" + nombre + "' solo puede ser de tipo int o float", linea, columna);
        }
        if (tamano <= 0) {
            reportarError("el tamano del arreglo '" + nombre + "' debe ser mayor que 0", linea, columna);
        }
    }

    /**
     * Valida que los valores iniciales de un arreglo sean de su tipo y no
     * excedan su tamaño.
     *
     * @param nombre nombre del arreglo
     * @param tipo tipo de los elementos
     * @param tamano cantidad de elementos
     * @param tipoValores tipo de los valores iniciales
     * @param cantidad cantidad de valores iniciales
     * @param linea línea de la declaración
     * @param columna columna de la declaración
     */
    public void validarValoresArreglo(String nombre, String tipo, int tamano, String tipoValores, int cantidad,
            int linea, int columna) {
        if (esTipoDeArreglo(tipo) && !tipo.equals(tipoValores)) {
            reportarError("el arreglo '" + nombre + "' es de tipo " + tipo
                    + " pero sus valores son de tipo " + tipoValores, linea, columna);
        }
        if (tamano > 0 && cantidad > tamano) {
            reportarError("el arreglo '" + nombre + "' tiene tamano " + tamano
                    + " pero recibe " + cantidad + " valores", linea, columna);
        }
    }

    /**
     * Indica si un tipo se puede usar en un arreglo.
     *
     * @param tipo tipo por revisar
     * @return {@code true} si es int o float
     */
    private boolean esTipoDeArreglo(String tipo) {
        return tipo.equals("int") || tipo.equals("float");
    }

    /**
     * Valida que una función retorne int, float, char o bool.
     *
     * @param nombre nombre de la función
     * @param tipo tipo de retorno declarado
     * @param linea línea de la declaración
     * @param columna columna de la declaración
     */
    public void validarTipoRetorno(String nombre, String tipo, int linea, int columna) {
        if (!tipo.equals("int") && !tipo.equals("float") && !tipo.equals("char") && !tipo.equals("bool")) {
            reportarError("la funcion '" + nombre + "' no puede retornar " + tipo
                    + "; solo se permite int, float, char o bool", linea, columna);
        }
    }

    // Uso de identificadores

    /**
     * Busca un identificador visible y registra su tabla en el reporte de tokens.
     *
     * @param nombre nombre del identificador
     * @param linea línea donde se usa
     * @param columna columna donde se usa
     * @return el símbolo encontrado, o {@code null} si no está declarado
     */
    private Simbolo buscarDeclarado(String nombre, int linea, int columna) {
        Simbolo simbolo = tabla.buscar(nombre);
        if (simbolo == null) {
            reportarError("el identificador '" + nombre + "' no ha sido declarado", linea, columna);
            return null;
        }
        reporteTokens.registrarTabla(linea, columna, simbolo.getAlcance());
        return simbolo;
    }

    /**
     * Devuelve el tipo de una variable o parámetro usado sin índice.
     *
     * @param nombre nombre del identificador
     * @param linea línea donde se usa
     * @param columna columna donde se usa
     * @return tipo del símbolo, o {@link ResultadoExpresion#TIPO_ERROR} si no se puede usar así
     */
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

    /**
     * Devuelve el tipo de los elementos de un arreglo usado con índice.
     *
     * @param nombre nombre del identificador
     * @param linea línea donde se usa
     * @param columna columna donde se usa
     * @return tipo de los elementos, o {@link ResultadoExpresion#TIPO_ERROR} si no es un arreglo
     */
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

    /**
     * Indica si un operador aritmético acepta un tipo: / solo float; //, mod y
     * pot solo int; los demás int o float.
     *
     * @param operador símbolo del operador
     * @param tipo tipo de los operandos
     * @return {@code true} si el operador acepta el tipo
     */
    private boolean aceptaTipoAritmetico(String operador, String tipo) {
        if (operador.equals("/")) {
            return tipo.equals("float");
        }
        if (operador.equals("//") || operador.equals("mod") || operador.equals("pot")) {
            return tipo.equals("int");
        }
        return tipo.equals("int") || tipo.equals("float");
    }

    /**
     * Indica si un operador relacional acepta un tipo: == y != aceptan también
     * bool y char; los demás solo int o float.
     *
     * @param operador símbolo del operador
     * @param tipo tipo de los operandos
     * @return {@code true} si el operador acepta el tipo
     */
    private boolean aceptaTipoRelacional(String operador, String tipo) {
        if (operador.equals("==") || operador.equals("!=")) {
            return tipo.equals("int") || tipo.equals("float") || tipo.equals("bool") || tipo.equals("char");
        }
        return tipo.equals("int") || tipo.equals("float");
    }

    /**
     * Reporta que un operador no acepta los tipos de sus operandos.
     *
     * @param operador símbolo del operador
     * @param izquierdo operando izquierdo
     * @param derecho operando derecho
     * @param linea línea del operador
     * @param columna columna del operador
     * @return resultado con tipo de error
     */
    private ResultadoExpresion errorDeOperandos(String operador, ResultadoExpresion izquierdo,
            ResultadoExpresion derecho, int linea, int columna) {
        reportarError("el operador '" + operador + "' no acepta operandos de tipo "
                + izquierdo.getTipo() + " y " + derecho.getTipo(), linea, columna);
        return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
    }

    /**
     * Valida una operación aritmética binaria. Los dos operandos deben ser del
     * mismo tipo.
     *
     * @param operador símbolo del operador
     * @param izquierdo operando izquierdo
     * @param derecho operando derecho
     * @param linea línea del operador
     * @param columna columna del operador
     * @return resultado con el tipo de la operación, o con tipo de error
     */
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

    /**
     * Valida una potencia: ambos operandos int y exponente que no sea un literal
     * negativo.
     *
     * @param base base de la potencia
     * @param exponente exponente de la potencia
     * @param linea línea del operador
     * @param columna columna del operador
     * @return resultado con el tipo de la operación, o con tipo de error
     */
    public ResultadoExpresion operacionPotencia(ResultadoExpresion base, ResultadoExpresion exponente,
            int linea, int columna) {
        ResultadoExpresion resultado = operacionAritmetica("pot", base, exponente, linea, columna);
        if (!resultado.esError() && exponente.esLiteralNegativo()) {
            reportarError("el exponente de 'pot' no puede ser negativo", linea, columna);
            return new ResultadoExpresion(ResultadoExpresion.TIPO_ERROR);
        }
        return resultado;
    }

    /**
     * Valida una operación relacional. Los dos operandos deben ser del mismo tipo.
     *
     * @param operador símbolo del operador
     * @param izquierdo operando izquierdo
     * @param derecho operando derecho
     * @param linea línea del operador
     * @param columna columna del operador
     * @return resultado de tipo bool, o con tipo de error
     */
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

    /**
     * Valida una conjunción o disyunción. Los dos operandos deben ser bool.
     *
     * @param operador símbolo del operador
     * @param izquierdo operando izquierdo
     * @param derecho operando derecho
     * @param linea línea del operador
     * @param columna columna del operador
     * @return resultado de tipo bool, o con tipo de error
     */
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

    /**
     * Valida una negación lógica. El operando debe ser bool.
     *
     * @param operando operando de la negación
     * @param linea línea del operador
     * @param columna columna del operador
     * @return resultado de tipo bool, o con tipo de error
     */
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

    /**
     * Valida ++ o -- sobre una variable int o float.
     *
     * @param operador ++ o --
     * @param nombre nombre de la variable
     * @param linea línea de la variable
     * @param columna columna de la variable
     * @return resultado con el tipo de la variable, o con tipo de error
     */
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

    /**
     * Valida que el valor asignado sea del tipo de la variable.
     *
     * @param nombre nombre de la variable
     * @param tipoDestino tipo de la variable
     * @param valor expresión asignada
     * @param linea línea de la asignación
     * @param columna columna de la asignación
     */
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

    /**
     * Valida que el índice de un arreglo sea int.
     *
     * @param indice expresión del índice
     * @param linea línea del índice
     * @param columna columna del índice
     */
    public void validarIndice(ResultadoExpresion indice, int linea, int columna) {
        if (!indice.esError() && !indice.getTipo().equals("int")) {
            reportarError("el indice de un arreglo debe ser de tipo int, no " + indice.getTipo(), linea, columna);
        }
    }

    /**
     * Valida que la condición de if, elif, while o for sea bool.
     *
     * @param condicion expresión de la condición
     * @param linea línea de la condición
     * @param columna columna de la condición
     */
    public void validarCondicion(ResultadoExpresion condicion, int linea, int columna) {
        if (!condicion.esError() && !condicion.getTipo().equals("bool")) {
            reportarError("la condicion debe ser de tipo bool, no " + condicion.getTipo(), linea, columna);
        }
    }

    /**
     * Valida que break esté dentro de un ciclo.
     *
     * @param linea línea del break
     * @param columna columna del break
     */
    public void validarBreak(int linea, int columna) {
        if (ciclosAbiertos == 0) {
            reportarError("break solo se puede usar dentro de un while o un for", linea, columna);
        }
    }

    /**
     * Valida que read lea una variable int, float o char.
     *
     * @param nombre nombre de la variable
     * @param tipo tipo de la variable
     * @param linea línea de la variable
     * @param columna columna de la variable
     */
    public void validarTipoRead(String nombre, String tipo, int linea, int columna) {
        if (tipo.equals("string") || tipo.equals("bool")) {
            reportarError("read solo puede leer variables int, float o char y '" + nombre
                    + "' es de tipo " + tipo, linea, columna);
        }
    }

    /**
     * Valida que write no escriba una variable char.
     *
     * @param nombre nombre de la variable
     * @param tipo tipo de la variable
     * @param linea línea de la variable
     * @param columna columna de la variable
     */
    public void validarTipoWrite(String nombre, String tipo, int linea, int columna) {
        if (tipo.equals("char")) {
            reportarError("write solo puede escribir string, int, bool o float y '" + nombre
                    + "' es de tipo char", linea, columna);
        }
    }

    // Funciones

    /**
     * Valida una llamada a función y sus argumentos.
     *
     * @param nombre nombre de la función
     * @param argumentos argumentos en el orden de la llamada
     * @param linea línea de la llamada
     * @param columna columna de la llamada
     * @return resultado con el tipo de retorno, o con tipo de error
     */
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
        reporteTokens.registrarTabla(linea, columna, "global");
        validarArgumentos(funcion, argumentos, linea, columna);
        return new ResultadoExpresion(funcion.getTipo());
    }

    /**
     * Valida la cantidad, el orden y el tipo de los argumentos de una llamada.
     *
     * @param funcion función llamada
     * @param argumentos argumentos en el orden de la llamada
     * @param linea línea de la llamada
     * @param columna columna de la llamada
     */
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

    /**
     * Valida que el valor de return sea del tipo de la función actual. Una
     * función void no puede tener return.
     *
     * @param valor expresión retornada
     * @param linea línea del return
     * @param columna columna del return
     */
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
     * Reporta como inaccesible la línea que sigue a otra que corta el flujo.
     *
     * @param linea línea anterior del bloque
     * @param lineaSiguiente número de la línea que le sigue
     */
    public void validarLineaAlcanzable(ResultadoSentencia linea, int lineaSiguiente) {
        if (linea.cortaFlujo()) {
            reportarErrorDeLinea("codigo inaccesible, la ejecucion nunca llega a esta linea", lineaSiguiente);
        }
    }

    /**
     * Valida que la función que termina haya tenido al menos un return. Si hubo
     * errores de sintaxis en la función no se revisa, porque el return pudo
     * haberse descartado al recuperarse del error.
     */
    public void validarFuncionConReturn() {
        boolean huboErroresEnFuncion = erroresSintacticos > erroresSintacticosAlIniciarFuncion;
        boolean esVoid = funcionActual.getTipo().equals("void");
        if (!funcionTieneReturn && !huboErroresEnFuncion && !esVoid) {
            reportarError("la funcion '" + funcionActual.getNombre() + "' no tiene ningun return",
                    funcionActual.getLinea() - 1, funcionActual.getColumna() - 1);
        }
    }
}
