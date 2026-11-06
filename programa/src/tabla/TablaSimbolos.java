package src.tabla;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Tabla de símbolos organizada por alcances.
 *
 * <p>Los alcances abiertos se manejan con una pila: al entrar a una función o
 * bloque se abre uno nuevo y al salir se cierra. Además se guardan todos los
 * alcances creados para poder escribir el reporte al final del análisis.
 */
public class TablaSimbolos {
    private static final int ANCHO_REPORTE = 63;

    private Stack<Alcance> alcancesAbiertos = new Stack<>();
    private ArrayList<Alcance> todosLosAlcances = new ArrayList<>();

    /**
     * Crea la tabla con el alcance global abierto.
     */
    public TablaSimbolos() {
        abrirAlcance("global");
    }

    /**
     * Abre un alcance nuevo dentro del alcance actual.
     *
     * @param nombre nombre descriptivo del alcance
     */
    public void abrirAlcance(String nombre) {
        Alcance padre = alcancesAbiertos.isEmpty() ? null : alcancesAbiertos.peek();
        Alcance nuevo = new Alcance(nombre, padre);
        alcancesAbiertos.push(nuevo);
        todosLosAlcances.add(nuevo);
    }

    /**
     * Cierra el alcance actual. El alcance global nunca se cierra.
     */
    public void cerrarAlcance() {
        if (alcancesAbiertos.size() > 1) {
            alcancesAbiertos.pop();
        }
    }

    /**
     * Indica si el alcance actual es el global.
     *
     * @return {@code true} si no hay una función o bloque abierto
     */
    public boolean estaEnAlcanceGlobal() {
        return alcancesAbiertos.size() == 1;
    }

    /**
     * Agrega un símbolo al alcance actual.
     *
     * @param simbolo símbolo a registrar
     */
    public void agregar(Simbolo simbolo) {
        simbolo.setAlcance(alcancesAbiertos.peek().getNombre());
        alcancesAbiertos.peek().agregar(simbolo);
    }

    /**
     * Busca un símbolo desde el alcance actual hacia los alcances que lo contienen,
     * terminando en el global.
     *
     * @param nombre nombre del símbolo
     * @return el símbolo visible con ese nombre, o {@code null} si no existe
     */
    public Simbolo buscar(String nombre) {
        Alcance alcance = alcancesAbiertos.peek();
        while (alcance != null) {
            Simbolo encontrado = alcance.buscar(nombre);
            if (encontrado != null) {
                return encontrado;
            }
            alcance = alcance.getPadre();
        }
        return null;
    }

    /**
     * Busca un símbolo solamente en el alcance actual.
     *
     * @param nombre nombre del símbolo
     * @return el símbolo encontrado, o {@code null} si no está en el alcance actual
     */
    public Simbolo buscarEnAlcanceActual(String nombre) {
        return alcancesAbiertos.peek().buscar(nombre);
    }

    /**
     * Escribe todos los alcances y sus símbolos en un archivo de texto.
     *
     * @param rutaSalida ruta del archivo que se creará
     * @throws IOException si no se puede escribir el archivo
     */
    public void escribirReporte(String rutaSalida) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(Path.of(rutaSalida), StandardCharsets.UTF_8)) {
            for (Alcance alcance : todosLosAlcances) {
                escribirTitulo(escritor, alcance);

                if (alcance.getSimbolos().isEmpty()) {
                    escritor.write("(sin simbolos)");
                    escritor.newLine();
                } else {
                    escribirEncabezadoColumnas(escritor);
                    for (Simbolo simbolo : alcance.getSimbolos()) {
                        escribirFila(escritor, simbolo);
                    }
                }
                escribirSeparador(escritor);
                escritor.newLine();
            }
        }
    }

    /**
     * Escribe el título de un alcance, con el alcance que lo contiene.
     *
     * @param escritor destino del reporte
     * @param alcance alcance del título
     * @throws IOException si no se puede escribir
     */
    private void escribirTitulo(BufferedWriter escritor, Alcance alcance) throws IOException {
        String titulo = "======= Alcance: " + alcance.getNombre();
        if (alcance.getPadre() != null) {
            titulo += " (dentro de: " + alcance.getPadre().getNombre() + ")";
        }
        titulo += " ";

        int igualesFaltantes = Math.max(3, ANCHO_REPORTE - titulo.length());
        escritor.write(titulo + "=".repeat(igualesFaltantes));
        escritor.newLine();
    }

    /**
     * Escribe los nombres de las columnas.
     *
     * @param escritor destino del reporte
     * @throws IOException si no se puede escribir
     */
    private void escribirEncabezadoColumnas(BufferedWriter escritor) throws IOException {
        escritor.write(String.format("%-15s %-10s %-6s %-6s %-8s %s",
                "nombre", "categoria", "tipo", "linea", "columna", "informacion"));
        escritor.newLine();
        escribirSeparador(escritor);
    }

    /**
     * Escribe una línea separadora.
     *
     * @param escritor destino del reporte
     * @throws IOException si no se puede escribir
     */
    private void escribirSeparador(BufferedWriter escritor) throws IOException {
        escritor.write("-".repeat(ANCHO_REPORTE));
        escritor.newLine();
    }

    /**
     * Escribe la información de un símbolo.
     *
     * @param escritor destino del reporte
     * @param simbolo símbolo de la fila
     * @throws IOException si no se puede escribir
     */
    private void escribirFila(BufferedWriter escritor, Simbolo simbolo) throws IOException {
        escritor.write(String.format("%-15s %-10s %-6s %-6d %-8d %s",
                simbolo.getNombre(), simbolo.getCategoria(), simbolo.getTipo(),
                simbolo.getLinea(), simbolo.getColumna(), simbolo.getInformacionExtra()));
        escritor.newLine();
    }
}
