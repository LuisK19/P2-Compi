package generated;

import java_cup.runtime.*;

%%

/* Configuración del lexer: clase generada, compatibilidad con CUP y posición de los tokens */
%class Lexer
%unicode
%cup
%line
%column
%public

%{
    private boolean huboErroresLexicos = false;
    private StringBuilder erroresLexicos = new StringBuilder();

    /** Construye un símbolo de CUP con la línea y columna actuales, desde 0. */
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }

    /** Indica si se reportó algún error léxico. */
    public boolean huboErroresLexicos() {
        return huboErroresLexicos;
    }

    /** Devuelve el texto de los errores léxicos encontrados. */
    public String obtenerErroresLexicos() {
        return erroresLexicos.toString();
    }

    /** Registra un error léxico con su línea y columna. */
    private void reportarError(String descripcion) {
        huboErroresLexicos = true;
        erroresLexicos.append("Error lexico en linea " + (yyline + 1)
                + ", columna " + (yycolumn + 1) + ": " + descripcion);
        erroresLexicos.append(System.lineSeparator());
    }
%}

/* Expresiones regulares de los tokens */
DIG             = [0-9]
DIGN            = [1-9]
CERO            = "0"

PARTE_ENTERA    = {DIGN}{DIG}*
PARTE_FLOTANTE  = {DIG}*{DIGN}

ENTERO          = {CERO} | {PARTE_ENTERA}
FLOTANTE        = ({CERO}\.{CERO}) | ({CERO}\.{PARTE_FLOTANTE}) | ({PARTE_ENTERA}\.{PARTE_FLOTANTE})

ID              = [a-zA-Z_][a-zA-Z0-9_]*

ID_INVALIDO = [0-9]+{ID}

CADENA          = \"[^\"\r\n]*\"
CADENA_SIN_CERRAR = \"[^\"\r\n]*
CARACTER        = \'[^\'\r\n]\'
ESPACIOS        = [ \t\r\n]+

COMENTARIO_LINEA = "|"[^\n]*
COMENTARIO_MULTILINEA = "¡"[^!]*"!"
COMENTARIO_SIN_CERRAR = "¡"[^!]*

%%

/* Reglas léxicas */

/* Espacios en blanco y comentarios */
{ESPACIOS}                         { /* ignorar */ }
{COMENTARIO_LINEA}                 { /* ignorar */ }
{COMENTARIO_MULTILINEA}            { /* ignorar */ }

/* Palabras reservadas */
"val"           { return symbol(sym.VAL, yytext()); }
"int"           { return symbol(sym.INT, yytext()); }
"float"         { return symbol(sym.FLOAT, yytext()); }
"bool"          { return symbol(sym.BOOL, yytext()); }
"char"          { return symbol(sym.CHAR, yytext()); }
"string"        { return symbol(sym.STR, yytext()); }
"true"          { return symbol(sym.TRUE, yytext()); }
"false"         { return symbol(sym.FALSE, yytext()); }
"if"            { return symbol(sym.IF, yytext()); }
"elif"          { return symbol(sym.ELIF, yytext()); }
"else"          { return symbol(sym.ELSE, yytext()); }
"while"         { return symbol(sym.WHILE, yytext()); }
"for"           { return symbol(sym.FOR, yytext()); }
"return"        { return symbol(sym.RETURN, yytext()); }
"break"         { return symbol(sym.BREAK, yytext()); }
"write"         { return symbol(sym.WRITE, yytext()); }
"read"          { return symbol(sym.READ, yytext()); }
"void"          { return symbol(sym.VOID, yytext()); }
"principal"     { return symbol(sym.PRINCIPAL, yytext()); }

/* Operadores aritméticos */
"++"            { return symbol(sym.OP_INC, yytext()); }
"--"            { return symbol(sym.OP_DEC, yytext()); }
"+"             { return symbol(sym.OP_SUMA, yytext()); }
"-"             { return symbol(sym.OP_MENOS, yytext()); }
"*"             { return symbol(sym.OP_MULT, yytext()); }
"//"            { return symbol(sym.OP_DIV_ENT, yytext()); }
"/"             { return symbol(sym.OP_DIV, yytext()); }
"mod"             { return symbol(sym.OP_MOD, yytext()); }
"pot"             { return symbol(sym.OP_POT, yytext()); }

/* Operadores relacionales */
"<="            { return symbol(sym.OP_MENIG, yytext()); }
">="            { return symbol(sym.OP_MAYIG, yytext()); }
"=="            { return symbol(sym.OP_IGUAL, yytext()); }
"!="            { return symbol(sym.OP_DIFER, yytext()); }
"<"             { return symbol(sym.OP_MENOR, yytext()); }
">"             { return symbol(sym.OP_MAYOR, yytext()); }

/* Operadores lógicos */
"λ"             { return symbol(sym.OP_AND, yytext()); }
"θ"             { return symbol(sym.OP_OR, yytext()); }
"Σ"             { return symbol(sym.OP_NOT, yytext()); }

/* Símbolos especiales */
"¿:"            { return symbol(sym.LLAVE_ABRE, yytext());}  
":?"            { return symbol(sym.LLAVE_CIERRA, yytext());}
"ʃ:"            { return symbol(sym.CORCHETE_ABRE, yytext());}
":ʅ"            { return symbol(sym.CORCHETE_CIERRA, yytext());}
"є:"            { return symbol(sym.PARENTESIS_ABRE, yytext());}
":э"            { return symbol(sym.PARENTESIS_CIERRA, yytext());}
"Ͱ"             { return symbol(sym.ASIGNACION, yytext());}
"»"             { return symbol(sym.FIN_SENT, yytext());}
","             { return symbol(sym.COMA, yytext());}

/* Literales */

{FLOTANTE}  { return symbol(sym.FLOTANTE, Double.parseDouble(yytext())); }
{ENTERO}    { return symbol(sym.ENTERO, Integer.parseInt(yytext())); }
{CADENA}    { return symbol(sym.CADENA, yytext().substring(1, yytext().length() - 1)); }
{CARACTER}  { return symbol(sym.CARACTER, yytext().substring(1, yytext().length() - 1)); }

/* Identificadores */

{ID}        { return symbol(sym.ID, yytext()); }

/* Errores léxicos */


{ID_INVALIDO}           { reportarError("identificador no puede comenzar con un digito '" + yytext() + "'"); }
{CADENA_SIN_CERRAR}     { reportarError("string sin cerrar en la misma linea " + yytext()); }
{COMENTARIO_SIN_CERRAR} { reportarError("comentario multilinea sin cerrar (falta '!')"); }
[^]                     { reportarError("caracter no reconocido '" + yytext() + "'"); }