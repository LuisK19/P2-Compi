package src;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Genera los archivos Java del lexer y del parser a partir de sus fuentes.
 */
public class GeneradorAnalizadores {

    /**
     * Prepara la carpeta de salida y genera ambos analizadores.
     *
     * <p>Antes de generar, elimina los archivos Lexer, Parser y sym previos
     * para evitar conservar resultados obsoletos.
     *
     * @param rutaLexer ruta del archivo fuente de JFlex
     * @param rutaParser ruta del archivo fuente de CUP
     * @throws Exception si falla la limpieza o la ejecución de JFlex/CUP
     */
    public void generarLexerYParser(String rutaLexer, String rutaParser) throws Exception {
        File carpetaGenerada = new File("generated");
        if (!carpetaGenerada.exists() && !carpetaGenerada.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta generated.");
        }

        eliminarArchivosAnteriores(carpetaGenerada);
        generarLexer(rutaLexer, carpetaGenerada.getPath());
        generarParser(rutaParser, carpetaGenerada.getPath());
    }

    /**
     * Elimina los archivos generados por JFlex y CUP si existen.
     *
     * @param carpetaGenerada carpeta que contiene los archivos generados
     * @throws IOException si no se puede eliminar alguno de los archivos
     */
    private void eliminarArchivosAnteriores(File carpetaGenerada) throws IOException {
        Files.deleteIfExists(new File(carpetaGenerada, "Lexer.java").toPath());
        Files.deleteIfExists(new File(carpetaGenerada, "Parser.java").toPath());
        Files.deleteIfExists(new File(carpetaGenerada, "sym.java").toPath());
    }

    /**
     * Invoca JFlex para generar el lexer en la carpeta indicada.
     *
     * @param ruta ruta del archivo {@code .flex}
     * @param destino carpeta de salida
     * @throws Exception si JFlex no puede procesar la especificación
     */
    private void generarLexer(String ruta, String destino) throws Exception {
        String[] parametros = {"-d", destino, ruta};
        jflex.Main.main(parametros);
    }

    /**
     * Invoca CUP para generar el parser y la clase de símbolos.
     *
     * @param ruta ruta del archivo {@code .cup}
     * @param destino carpeta de salida
     * @throws Exception si CUP no puede procesar la gramática
     */
    private void generarParser(String ruta, String destino) throws Exception {
        String[] parametros = {"-destdir", destino, "-parser", "Parser", ruta};
        java_cup.Main.main(parametros);
    }
}