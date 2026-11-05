package src;

import generated.Lexer;
import generated.sym;
import java_cup.runtime.Symbol;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Recorre los tokens de un archivo fuente antes del análisis sintáctico para
 * registrar la firma de cada función (nombre, tipo de retorno y tipos de sus
 * parámetros).
 *
 * <p>Esto permite validar una llamada aunque la función esté declarada más
 * abajo en el archivo.
 */
public class RecolectorFunciones {

    /**
     * Obtiene las firmas de las funciones declaradas en el archivo.
     *
     * @param rutaEntrada ruta del archivo fuente
     * @return funciones encontradas, por nombre; si un nombre se repite se
     *     conserva la primera declaración
     * @throws IOException si no se puede leer el archivo
     */
    public static HashMap<String, SimboloFuncion> recolectar(String rutaEntrada) throws IOException {
        ArrayList<Symbol> tokens = leerTokens(rutaEntrada);
        HashMap<String, SimboloFuncion> funciones = new HashMap<>();
        int profundidadLlaves = 0;

        for (int i = 0; i < tokens.size(); i++) {
            int tipoToken = tokens.get(i).sym;
            if (tipoToken == sym.LLAVE_ABRE) {
                profundidadLlaves++;
            } else if (tipoToken == sym.LLAVE_CIERRA) {
                profundidadLlaves--;
            } else if (profundidadLlaves == 0 && esEncabezadoFuncion(tokens, i)) {
                SimboloFuncion funcion = leerEncabezado(tokens, i);
                if (!funciones.containsKey(funcion.getNombre())) {
                    funciones.put(funcion.getNombre(), funcion);
                }
            }
        }
        return funciones;
    }

    private static ArrayList<Symbol> leerTokens(String rutaEntrada) throws IOException {
        ArrayList<Symbol> tokens = new ArrayList<>();
        try (BufferedReader lector = Files.newBufferedReader(Path.of(rutaEntrada), StandardCharsets.UTF_8)) {
            Lexer analizadorLexico = new Lexer(lector);
            Symbol token = analizadorLexico.next_token();
            while (token.sym != sym.EOF) {
                tokens.add(token);
                token = analizadorLexico.next_token();
            }
        }
        return tokens;
    }

    // Un encabezado tiene la forma: tipo nombre є: ... (las variables globales empiezan con val).
    private static boolean esEncabezadoFuncion(ArrayList<Symbol> tokens, int posicion) {
        if (posicion + 2 >= tokens.size() || nombreDeTipo(tokens.get(posicion).sym) == null) {
            return false;
        }
        if (posicion > 0 && tokens.get(posicion - 1).sym == sym.VAL) {
            return false;
        }
        return tokens.get(posicion + 1).sym == sym.ID
                && tokens.get(posicion + 2).sym == sym.PARENTESIS_ABRE;
    }

    private static SimboloFuncion leerEncabezado(ArrayList<Symbol> tokens, int posicion) {
        Symbol tokenNombre = tokens.get(posicion + 1);
        String tipoRetorno = nombreDeTipo(tokens.get(posicion).sym);
        SimboloFuncion funcion = new SimboloFuncion((String) tokenNombre.value, tipoRetorno,
                tokenNombre.left + 1, tokenNombre.right + 1);

        int actual = posicion + 3;
        while (actual < tokens.size() && tokens.get(actual).sym != sym.PARENTESIS_CIERRA) {
            String tipoParametro = nombreDeTipo(tokens.get(actual).sym);
            if (tipoParametro != null) {
                funcion.agregarTipoParametro(tipoParametro);
            }
            actual++;
        }
        return funcion;
    }

    private static String nombreDeTipo(int tipoToken) {
        switch (tipoToken) {
            case sym.INT:
                return "int";
            case sym.FLOAT:
                return "float";
            case sym.BOOL:
                return "bool";
            case sym.CHAR:
                return "char";
            case sym.STR:
                return "string";
            default:
                return null;
        }
    }
}
