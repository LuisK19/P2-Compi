package src;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Coordina la generación, compilación y ejecución de los analizadores.
 *
 * <p>Este punto de entrada genera Lexer y Parser desde sus fuentes, compila
 * esos archivos junto con {@link Analizador} y lanza el análisis en otro
 * proceso de Java.
 */
public class App {
    private static final String VERSION_JAVA = "14";
    private static final File CARPETA_PROYECTO = new File(System.getProperty("user.dir"));
    private static final File CARPETA_SALIDA = new File(CARPETA_PROYECTO, "out");
    private static final String RUNTIME_CUP =
            new File(CARPETA_PROYECTO, "tools/java-cup-11b-runtime.jar").getPath();

    /**
     * Ejecuta secuencialmente todas las etapas del analizador.
     *
     * @param args argumentos que se reenvían a {@link Analizador#main(String[])}
     * @throws Exception si falla la generación, compilación o ejecución
     */
    public static void main(String[] args) throws Exception {
        usarSalidaUtf8();
        boolean seGeneraron = generarLexerParser();
        if (seGeneraron || hayCambiosEnFuentes()) {
            compilarAnalizador();
        }
        ejecutarAnalizador(args);
    }

    /**
     * Configura la salida de consola en UTF-8.
     */
    private static void usarSalidaUtf8() {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
    }

    /**
     * Genera los archivos Java del lexer y del parser si hace falta.
     *
     * @return {@code true} si se generaron los analizadores
     * @throws Exception si no se pueden generar los analizadores
     */
    private static boolean generarLexerParser() throws Exception {
        String rutaLexer = new File(CARPETA_PROYECTO, "src/scanner.flex").getPath();
        String rutaParser = new File(CARPETA_PROYECTO, "src/parser.cup").getPath();
        GeneradorAnalizadores generador = new GeneradorAnalizadores();

        boolean seGeneraron = generador.generarSiHayCambios(rutaLexer, rutaParser);
        if (seGeneraron) {
            System.out.println("¡Generacion completada con exito en la carpeta 'generated'!");
        }
        return seGeneraron;
    }

    /**
     * Indica si hay que compilar porque faltan las clases o cambió algún archivo Java.
     *
     * @return {@code true} si se debe compilar
     */
    private static boolean hayCambiosEnFuentes() {
        File claseAnalizador = new File(CARPETA_SALIDA, "src/Analizador.class");
        if (!claseAnalizador.exists()) {
            return true;
        }
        return hayJavaMasReciente(new File(CARPETA_PROYECTO, "src"), claseAnalizador.lastModified());
    }

    /**
     * Busca en una carpeta y sus subcarpetas un archivo Java modificado después
     * de una fecha.
     *
     * @param carpeta carpeta donde se busca
     * @param fecha fecha de comparación, en milisegundos
     * @return {@code true} si existe un archivo Java más reciente
     */
    private static boolean hayJavaMasReciente(File carpeta, long fecha) {
        for (File archivo : carpeta.listFiles()) {
            if (archivo.isDirectory()) {
                if (hayJavaMasReciente(archivo, fecha)) {
                    return true;
                }
            } else if (archivo.getName().endsWith(".java") && archivo.lastModified() > fecha) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compila los analizadores generados y la clase que los ejecuta.
     *
     * @throws IllegalStateException si no hay un JDK disponible, no se puede
     *     crear la carpeta de salida o falla la compilación
     */
    private static void compilarAnalizador() {
        JavaCompiler compilador = ToolProvider.getSystemJavaCompiler();
        if (compilador == null) {
            throw new IllegalStateException("Se necesita un JDK para compilar el analizador.");
        }

        if (!CARPETA_SALIDA.exists() && !CARPETA_SALIDA.mkdirs()) {
            throw new IllegalStateException("No se pudo crear la carpeta out.");
        }

        int resultado = compilador.run(null, System.out, System.err,
            "--release", VERSION_JAVA,
            "-classpath", RUNTIME_CUP,
            "-d", CARPETA_SALIDA.getAbsolutePath(),
                "generated/Lexer.java",
                "generated/Parser.java",
                "generated/sym.java",
                "src/tabla/Simbolo.java",
                "src/tabla/SimboloArreglo.java",
                "src/tabla/SimboloFuncion.java",
                "src/tabla/Alcance.java",
                "src/tabla/TablaSimbolos.java",
                "src/tabla/ReporteTokens.java",
                "src/semantico/ResultadoExpresion.java",
                "src/semantico/ResultadoSentencia.java",
                "src/semantico/AnalizadorSemantico.java",
                "src/semantico/RecolectorFunciones.java",
                "src/codigo/GeneradorCodigo.java",
                "src/codigo/EncabezadoFor.java",
                "src/Analizador.java");

        if (resultado != 0) {
            throw new IllegalStateException("No se pudo compilar los analizadores.");
        }
    }

    /**
     * Lanza {@link Analizador} en un proceso separado y reenvía sus argumentos.
     *
     * @param args argumentos recibidos por esta aplicación
     * @throws Exception si no se puede iniciar el proceso o el análisis termina
     *     con un código distinto de cero
     */
    private static void ejecutarAnalizador(String[] args) throws Exception {
        String javaCommand = System.getProperty("os.name").startsWith("Windows")
                ? "java.exe"
                : "java";
        File javaExecutable = new File(System.getProperty("java.home"), "bin/" + javaCommand);

        List<String> command = new ArrayList<>();
        command.add(javaExecutable.getAbsolutePath());
        command.add("-cp");
        command.add(CARPETA_SALIDA.getAbsolutePath() + File.pathSeparator + RUNTIME_CUP);
        command.add("src.Analizador");
        Collections.addAll(command, args);

        Process proceso = new ProcessBuilder(command).inheritIO().start();
        int resultado = proceso.waitFor();
        if (resultado != 0) {
            throw new IllegalStateException("El analisis termino con error.");
        }
    }
}
