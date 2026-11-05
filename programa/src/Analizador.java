package src;

import generated.Lexer;
import generated.Parser;
import generated.sym;
import java_cup.runtime.Symbol;
import src.semantico.RecolectorFunciones;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Ejecuta el análisis léxico y sintáctico de un archivo fuente.
 *
 * <p>También genera un reporte CSV con los tokens encontrados durante el
 * análisis léxico.
 */
public class Analizador {

    /**
     * Inicia el análisis usando las rutas indicadas en los argumentos.
     *
     * <p>El primer argumento es el archivo de entrada, el segundo la ruta del
     * reporte CSV de tokens, el tercero la ruta del reporte de la tabla de
     * símbolos y el cuarto la ruta del código intermedio. Si no se
     * proporcionan, los reportes se guardan en la carpeta salida.
     *
     * @param args rutas opcionales de entrada y de los reportes
     * @throws IOException si no se pueden crear las carpetas de los reportes
     */
    public static void main(String[] args) throws IOException {
        usarSalidaUtf8();

        String archivoEntrada = args.length > 0 ? args[0] : "tests/validos/semantico_funciones.txt";
        String archivoSalidaTokens = args.length > 1 ? args[1] : "salida/tokens_encontrados.csv";
        String archivoSalidaTabla = args.length > 2 ? args[2] : "salida/tabla_simbolos.txt";
        String archivoSalidaCodigo = args.length > 3 ? args[3] : "salida/codigo_intermedio.txt";

        if (!new File(archivoEntrada).exists()) {
            System.err.println("No se encontro el archivo de entrada: " + archivoEntrada);
            return;
        }

        crearCarpetaDe(archivoSalidaTokens);
        crearCarpetaDe(archivoSalidaTabla);
        crearCarpetaDe(archivoSalidaCodigo);

        System.out.println("--- INICIANDO COMPILADOR ---");
        System.out.println("Archivo de entrada: " + archivoEntrada);

        if (!generarArchivoDeTokens(archivoEntrada, archivoSalidaTokens)) {
            System.out.println("\nRESULTADO: No se pudo leer el archivo o generar el reporte CSV.");
            return;
        }

        ejecutarAnalisisSintactico(archivoEntrada, archivoSalidaTabla, archivoSalidaCodigo);
    }

    /**
     * Crea la carpeta donde va un archivo de salida, si todavía no existe.
     *
     * @param rutaArchivo ruta del archivo que se va a escribir
     * @throws IOException si no se puede crear la carpeta
     */
    private static void crearCarpetaDe(String rutaArchivo) throws IOException {
        Path carpeta = Path.of(rutaArchivo).getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
    }

    /**
     * Configura la salida de consola en UTF-8.
     */
    private static void usarSalidaUtf8() {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
    }

    /**
     * Genera un CSV con los tokens léxicos del archivo de entrada.
     *
     * <p>Los errores léxicos se registran en la salida de errores y no impiden
     * generar el reporte con los demás tokens reconocidos.
     *
     * @param rutaEntrada ruta del archivo fuente que se analizará
     * @param rutaSalida ruta del archivo CSV que se creará
     * @return {@code true} si se pudo leer la entrada y escribir el CSV;
     *     {@code false} si ocurrió un error de lectura o escritura
     */
    public static boolean generarArchivoDeTokens(String rutaEntrada, String rutaSalida) {
        try (BufferedReader lector = Files.newBufferedReader(Path.of(rutaEntrada), StandardCharsets.UTF_8);
                BufferedWriter escritor = Files.newBufferedWriter(Path.of(rutaSalida), StandardCharsets.UTF_8)) {

            Lexer analizadorLexico = new Lexer(lector);
            Symbol tokenActual;

            escritor.write("token,lexema,categoria");
            escritor.newLine();

            while ((tokenActual = analizadorLexico.next_token()).sym != sym.EOF) {
                String nombreToken = sym.terminalNames[tokenActual.sym];
                String lexema = tokenActual.value.toString();
                String categoria = clasificarToken(tokenActual.sym);

                escritor.write(nombreToken + ","
                        + escaparCampoCsv(lexema) + ","
                        + escaparCampoCsv(categoria));
                escritor.newLine();
            }

            if (analizadorLexico.huboErroresLexicos()) {
                System.err.print(analizadorLexico.obtenerErroresLexicos());
                System.out.println("ERROR LEXICO: Se encontraron errores lexicos. Tokens validos en -> " + rutaSalida);
            } else {
                System.out.println("EXITO: Archivo de tokens generado en -> " + rutaSalida);
            }
            return true;
        } catch (Exception e) {
            System.out.println("ERROR LEXICO/ARCHIVO: No se pudo generar los tokens.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Devuelve la categoría general asociada con el identificador de token.
     *
     * @param idToken identificador definido por CUP
     * @return nombre de la categoría del token
     */
    private static String clasificarToken(int idToken) {
        switch (idToken) {
            case sym.VAL:
            case sym.IF:
            case sym.ELIF:
            case sym.ELSE:
            case sym.WHILE:
            case sym.FOR:
            case sym.RETURN:
            case sym.BREAK:
            case sym.WRITE:
            case sym.READ:
            case sym.VOID:
            case sym.PRINCIPAL:
                return "Palabra reservada";

            case sym.INT:
            case sym.FLOAT:
            case sym.BOOL:
            case sym.CHAR:
            case sym.STR:
                return "Tipo de dato";

            case sym.ID:
                return "Identificador";

            case sym.ENTERO:
                return "Literal int";
            case sym.FLOTANTE:
                return "Literal float";
            case sym.CADENA:
                return "Literal de string";
            case sym.CARACTER:
                return "Literal de char";
            case sym.TRUE:
            case sym.FALSE:
                return "Literal boolean";

            case sym.OP_SUMA:
            case sym.OP_MENOS:
            case sym.OP_MULT:
            case sym.OP_DIV:
            case sym.OP_DIV_ENT:
            case sym.OP_MOD:
            case sym.OP_POT:
            case sym.OP_INC:
            case sym.OP_DEC:
            case sym.OP_MENOR:
            case sym.OP_MAYOR:
            case sym.OP_MENIG:
            case sym.OP_MAYIG:
            case sym.OP_IGUAL:
            case sym.OP_DIFER:
            case sym.OP_AND:
            case sym.OP_OR:
            case sym.OP_NOT:
            case sym.ASIGNACION:
                return "Operador";

            case sym.LLAVE_ABRE:
            case sym.LLAVE_CIERRA:
            case sym.CORCHETE_ABRE:
            case sym.CORCHETE_CIERRA:
            case sym.PARENTESIS_ABRE:
            case sym.PARENTESIS_CIERRA:
            case sym.FIN_SENT:
            case sym.COMA:
                return "Simbolo";

            default:
                return "Token no clasificado";
        }
    }

    /**
     * Escapa un valor para incluirlo como campo entre comillas en un CSV.
     *
     * @param valor texto que se escribirá en el campo
     * @return texto entre comillas, con las comillas internas duplicadas
     */
    private static String escaparCampoCsv(String valor) {
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }

    /**
     * Ejecuta el parser, escribe la tabla de símbolos, comunica si la entrada
     * cumple la gramática y, si no hubo errores, escribe el código intermedio.
     *
     * @param rutaEntrada ruta del archivo fuente que se analizará
     * @param rutaTabla ruta del archivo donde se escribirá la tabla de símbolos
     * @param rutaCodigo ruta del archivo donde se escribirá el código intermedio
     */
    public static void ejecutarAnalisisSintactico(String rutaEntrada, String rutaTabla, String rutaCodigo) {
        try (BufferedReader lector = Files.newBufferedReader(Path.of(rutaEntrada), StandardCharsets.UTF_8)) {
            Lexer analizadorLexico = new Lexer(lector);
            Parser analizadorSintactico = new Parser(analizadorLexico);
            analizadorSintactico.setFuncionesDeclaradas(RecolectorFunciones.recolectar(rutaEntrada));

            System.out.println("\nIniciando analisis sintactico...");
            analizadorSintactico.parse();

            analizadorSintactico.getTablaSimbolos().escribirReporte(rutaTabla);
            System.out.println("Tabla de simbolos generada en -> " + rutaTabla);

            if (analizadorLexico.huboErroresLexicos()
                    || analizadorSintactico.huboErroresSintacticos()
                    || analizadorSintactico.huboErroresSemanticos()) {
                System.out.println("No se genero el codigo intermedio porque el archivo fuente tiene errores.");
                System.out.println(
                        "\nRESULTADO: El archivo fuente NO puede ser generado por la gramatica (Errores encontrados).");
                return;
            }

            Files.writeString(Path.of(rutaCodigo), analizadorSintactico.getCodigoIntermedio(), StandardCharsets.UTF_8);
            System.out.println("Codigo intermedio generado en -> " + rutaCodigo);
            System.out.println("\nRESULTADO: El archivo fuente SI puede ser generado por la gramatica.");
        } catch (Exception e) {
            System.err.println("Error durante el analisis: " + e.getMessage());
            System.out.println(
                    "\nRESULTADO: El archivo fuente NO puede ser generado por la gramatica (Errores encontrados).");
        }
    }
}