package src.tabla;

import generated.sym;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java_cup.runtime.Symbol;

/**
 * Reporte CSV de los tokens encontrados en el archivo fuente.
 *
 * <p>Guarda la tabla a la que pertenece cada identificador según su posición,
 * porque los tokens se leen antes de conocer los alcances.
 */
public class ReporteTokens {
    private HashMap<String, String> tablasDeTokens = new HashMap<>();

    /**
     * Registra el alcance al que pertenece el identificador de una posición.
     *
     * @param linea línea del token, empezando en 0
     * @param columna columna del token, empezando en 0
     * @param alcance nombre del alcance donde está declarado
     */
    public void registrarTabla(int linea, int columna, String alcance) {
        tablasDeTokens.put(linea + ":" + columna, alcance);
    }

    /**
     * Devuelve la tabla del token en una posición.
     *
     * @param linea línea del token, empezando en 0
     * @param columna columna del token, empezando en 0
     * @return nombre del alcance, o Ninguna si el token no pertenece a una tabla
     */
    public String tablaDeToken(int linea, int columna) {
        return tablasDeTokens.getOrDefault(linea + ":" + columna, "Ninguna");
    }

    /**
     * Genera un CSV con los tokens léxicos, la tabla a la que pertenece cada uno
     * y la información que se almacena.
     *
     * @param tokens tokens leídos del archivo de entrada
     * @param rutaSalida ruta del archivo CSV que se creará
     * @throws IOException si no se puede escribir el archivo
     */
    public void escribir(ArrayList<Symbol> tokens, String rutaSalida) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(Path.of(rutaSalida), StandardCharsets.UTF_8)) {
            escritor.write("id_token,token,lexema,categoria,tabla,informacion");
            escritor.newLine();

            for (Symbol token : tokens) {
                String categoria = clasificarToken(token.sym);

                escritor.write(token.sym + ","
                        + sym.terminalNames[token.sym] + ","
                        + escaparCampoCsv(token.value.toString()) + ","
                        + escaparCampoCsv(categoria) + ","
                        + escaparCampoCsv(tablaDeToken(token.left, token.right)) + ","
                        + escaparCampoCsv(informacionAlmacenada(categoria)));
                escritor.newLine();
            }
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
            case sym.VAL, sym.IF, sym.ELIF, sym.ELSE, sym.WHILE, sym.FOR,
                    sym.RETURN, sym.BREAK, sym.WRITE, sym.READ, sym.VOID, sym.PRINCIPAL:
                return "Palabra reservada";

            case sym.INT, sym.FLOAT, sym.BOOL, sym.CHAR, sym.STR:
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
            case sym.TRUE, sym.FALSE:
                return "Literal boolean";

            case sym.OP_SUMA, sym.OP_MENOS, sym.OP_MULT, sym.OP_DIV, sym.OP_DIV_ENT,
                    sym.OP_MOD, sym.OP_POT, sym.OP_INC, sym.OP_DEC,
                    sym.OP_MENOR, sym.OP_MAYOR, sym.OP_MENIG, sym.OP_MAYIG, sym.OP_IGUAL, sym.OP_DIFER,
                    sym.OP_AND, sym.OP_OR, sym.OP_NOT, sym.ASIGNACION:
                return "Operador";

            case sym.LLAVE_ABRE, sym.LLAVE_CIERRA, sym.CORCHETE_ABRE, sym.CORCHETE_CIERRA,
                    sym.PARENTESIS_ABRE, sym.PARENTESIS_CIERRA, sym.FIN_SENT, sym.COMA:
                return "Simbolo";

            default:
                return "Token no clasificado";
        }
    }

    /**
     * Indica qué información se guarda de un token según su categoría.
     *
     * @param categoria categoría dada por {@link #clasificarToken(int)}
     * @return descripción de la información almacenada
     */
    private static String informacionAlmacenada(String categoria) {
        if (categoria.equals("Identificador")) {
            return "nombre, categoria, tipo, linea, columna";
        }
        if (categoria.startsWith("Literal")) {
            return "No se almacena; se usa directo en el codigo intermedio";
        }
        return "No se almacena";
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
}
