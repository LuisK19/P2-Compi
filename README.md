# P2: Análisis Semántico y Generación de Código Intermedio

**Curso:** Compiladores e Intérpretes - Semestre II, 2026  
**Profesor:** Allan Rodríguez Dávila  
**Integrantes:**
- Luis Trejos Rivera, 2022437816
- Roymar Castillo Carvajal, 2022045111

**Fecha de entrega:** 9-11-2026

**Estatus de la entrega:** Buena

## Descripción

Compilador para un lenguaje imperativo con símbolos personalizados. Toma un archivo fuente,
realiza el análisis léxico, sintáctico y semántico, reporta los errores encontrados y continúa con
el resto del archivo. Si el programa no tiene errores, genera su código intermedio de tres
direcciones.

## Requisitos

- **Java JDK 14** o superior
- **JFlex:** `jflex-full-1.9.1.jar`
- **CUP:** `java-cup-11b.jar` + `java-cup-11b-runtime.jar`

Los tres `.jar` ya están incluidos en la carpeta `programa/tools/`.

## Estructura del proyecto

```
├── documentacion/              # Documentación del proyecto
└── programa/
    ├── tools/                  # .jar de JFlex y CUP
    ├── src/
    │   ├── scanner.flex        # Definición del lexer (JFlex)
    │   ├── parser.cup          # Definición del parser (CUP)
    │   ├── App.java            # Punto de entrada: genera, compila y ejecuta
    │   ├── Analizador.java     # Ejecuta las fases sobre un archivo fuente
    │   ├── GeneradorAnalizadores.java
    │   ├── semantico/          # Validaciones semánticas
    │   ├── codigo/             # Generación de código intermedio
    │   └── tabla/              # Tabla de símbolos y reporte de tokens
    ├── tests/
    │   ├── validos/            # Programas que deben aceptarse
    │   └── invalidos/          # Programas con errores
    ├── generated/              # Archivos generados por JFlex y CUP
    ├── out/                    # Clases compiladas (.class)
    └── salida/                 # Reportes generados
```

## Compilación y ejecución

Todos los comandos se ejecutan desde la carpeta `programa`.

En Windows, para ver bien los símbolos del lenguaje en la consola:

```
chcp 65001
```

### Forma rápida

`App` analiza el archivo indicado. Solo genera el lexer y el parser si `scanner.flex` o `parser.cup`
cambiaron, y solo compila si hubo cambios en el código. Para forzar ambos pasos, borra las carpetas
`generated` y `out`.

```
javac --release 14 -cp "tools/*" -d out src/App.java src/GeneradorAnalizadores.java
java -cp "out;tools/*" src.App tests/validos/programa_completo.txt
```

### Comandos manuales

#### 1. Generar el Lexer

```
java -jar tools\jflex-full-1.9.1.jar -d generated src\scanner.flex
```

#### 2. Generar el Parser

```
java -jar tools\java-cup-11b.jar -destdir generated -parser Parser src\parser.cup
```

#### 3. Compilar

```
javac --release 14 -cp tools\java-cup-11b-runtime.jar -d out generated\*.java src\codigo\*.java src\semantico\*.java src\tabla\*.java src\Analizador.java
```

#### 4. Ejecutar

Cambia la ruta por la del archivo que quieras procesar.

```
java -cp "out;tools\java-cup-11b-runtime.jar" src.Analizador tests\validos\programa_completo.txt
```

### Archivos de salida

| Archivo                       | Contenido                                                     |
|-------------------------------|---------------------------------------------------------------|
| `salida/tokens_encontrados.csv` | Tokens con su identificador, lexema, categoría, tabla e información |
| `salida/tabla_simbolos.txt`   | Símbolos de cada alcance                                      |
| `salida/codigo_intermedio.txt`| Código de tres direcciones; solo se genera si no hay errores  |

Los errores se muestran en la consola con su línea. Al final se indica si el archivo puede ser
generado por la gramática.

---

## Referencia del lenguaje

### Símbolos especiales

| Función                      | Símbolo | Equivalente típico |
|------------------------------|---------|--------------------|
| Abrir bloque                 | `¿:`    | `{`                |
| Cerrar bloque                | `:?`    | `}`                |
| Abrir índice o relacional    | `ʃ:`    | `[`                |
| Cerrar índice o relacional   | `:ʅ`    | `]`                |
| Abrir paréntesis             | `є:`    | `(`                |
| Cerrar paréntesis            | `:э`    | `)`                |
| Asignación                   | `Ͱ`     | `=`                |
| Fin de sentencia             | `»`     | `;`                |
| Comentario de línea          | `\|`    | `//`               |
| Abrir comentario multilínea  | `¡`     | `/*`               |
| Cerrar comentario multilínea | `!`     | `*/`               |

### Operadores aritméticos

De mayor a menor precedencia:

| Operador                               | Símbolo        | Tipos         |
|----------------------------------------|----------------|---------------|
| Potencia                               | `pot`          | `int`         |
| Suma y resta                           | `+`, `-`       | `int`, `float`|
| Multiplicación                         | `*`            | `int`, `float`|
| División decimal                       | `/`            | `float`       |
| División entera                        | `//`           | `int`         |
| Módulo                                 | `mod`          | `int`         |

También existen el negativo (`-`), solo sobre literales, y `++` y `--`, solo después de una variable
`int` o `float`.

### Operadores relacionales

`<`, `<=`, `>`, `>=`, `==` y `!=`. Cada operando va entre `ʃ:` y `:ʅ`, y `==` y `!=` aceptan además
`bool` y `char`.

### Operadores lógicos

| Operador | Símbolo |
|----------|---------|
| AND      | `λ`     |
| OR       | `θ`     |
| NOT      | `Σ`     |

Cada operando va entre `є:` y `:э`.

### Tipos de datos

| Tipo     | Descripción                                  |
|----------|----------------------------------------------|
| `int`    | Entero; también permite arreglos             |
| `float`  | Flotante; también permite arreglos           |
| `bool`   | Booleano                                     |
| `char`   | Carácter                                     |
| `string` | Cadena de texto                              |

### Ejemplo de programa válido

```
val int limite Ͱ 3 »

int doble є: int x :э ¿:
  return x * 2 »
:?

void principal є: :э ¿:
  val int i Ͱ 0 »
  while є: ʃ: i :ʅ < ʃ: limite :ʅ :э ¿:
    val int resultado Ͱ doble є: i :э »
    write є: resultado :э »
    i++ »
  :?
:?
```

## Notas

- Los archivos generados (`Lexer.java`, `sym.java`, `Parser.java`) **no** se editan a mano;
  siempre se regeneran desde `scanner.flex` y `parser.cup`.
- Las variables globales y las funciones pueden ir en cualquier orden, con exactamente un
  `principal`. Una variable debe declararse antes de usarse; una función se puede llamar aunque esté
  declarada más abajo.
- En el código intermedio, las variables globales llevan el sufijo `_global` y una variable local que
  oculta a otra local lleva `_local`.
