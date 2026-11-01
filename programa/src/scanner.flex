package generated;

import java_cup.runtime.*;

%%

/* Configuración del lexer generado y metadatos de posición para CUP. */
%class Lexer
%unicode
%cup
%line
%column
%public

%{
    // Construye símbolos CUP con la ubicación actual, conservada en base cero.
    private boolean huboErroresLexicos = false;
    private StringBuilder erroresLexicos = new StringBuilder();

    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }

    public boolean huboErroresLexicos() {
        return huboErroresLexicos;
    }

    public String obtenerErroresLexicos() {
        return erroresLexicos.toString();
    }
%}

/* Macros */
DIG             = [0-9]
DIGN            = [1-9]
CERO            = "0"

PARTE_ENTERA    = {DIGN}{DIG}*
PARTE_FLOTANTE  = {DIG}*{DIGN}

ENTERO          = {CERO} | {PARTE_ENTERA}
FLOTANTE        = ({CERO}\.{CERO}) | ({CERO}\.{PARTE_FLOTANTE}) | ({PARTE_ENTERA}\.{PARTE_FLOTANTE})

ID              = [a-zA-Z_][a-zA-Z0-9_]*

ID_INVALIDO = [0-9]+{ID}

CADENA          = \"[^\"]*\"
CARACTER        = \'[^\']\'
ESPACIOS        = [ \t\r\n]+

COMENTARIO_LINEA = "|"[^\n]* 
COMENTARIO_MULTILINEA = "¡"[^!]*"!"

%%

/* Sección 3: Reglas léxicas */

/* Espacios en blanco */
{ESPACIOS}                         { /* ignorar */ }
{COMENTARIO_LINEA}                 { /* ignorar */ }
{COMENTARIO_MULTILINEA}            { /* ignorar */ }

/* Palabras reservadas */
"val"           { return symbol(sym.VAL, yytext()); }
"int"           { return symbol(sym.INT, yytext()); }
"float"         { return symbol(sym.FLOAT, yytext()); }
"bool"          { return symbol(sym.BOOL, yytext()); }
"char"          { return symbol(sym.CHAR, yytext()); }
"str"           { return symbol(sym.STR, yytext()); }
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
"-"             { return symbol(sym.OP_RESTA, yytext()); }
"*"             { return symbol(sym.OP_MULT, yytext()); }
"//"            { return symbol(sym.OP_DIV_ENT, yytext()); }
"/"             { return symbol(sym.OP_DIV, yytext()); }
"%"             { return symbol(sym.OP_MOD, yytext()); }
"^"             { return symbol(sym.OP_POT, yytext()); }

/* Operadores relacionales */
"<="            { return symbol(sym.OP_MENIG, yytext()); }
">="            { return symbol(sym.OP_MAYIG, yytext()); }
"=="            { return symbol(sym.OP_IGUAL, yytext()); }
"!="            { return symbol(sym.OP_DIFER, yytext()); }
"<"             { return symbol(sym.OP_MENOR, yytext()); }
">"             { return symbol(sym.OP_MAYOR, yytext()); }

/* Operadores logicos */
"λ"             { return symbol(sym.OP_AND, yytext()); }
"θ"             { return symbol(sym.OP_OR, yytext()); }
"Σ"             { return symbol(sym.OP_NOT, yytext()); }

/* Simbolos especiales */
"¿:"            { return symbol(sym.BLOQUE_ABRE, yytext());}  
":?"            { return symbol(sym.BLOQUE_CIERRA, yytext());}
"ʃ:"            { return symbol(sym.INDICE_ABRE, yytext());}
":ʅ"            { return symbol(sym.INDICE_CIERRA, yytext());}
"є:"            { return symbol(sym.PAREN_ABRE, yytext());}
":э"            { return symbol(sym.PAREN_CIERRA, yytext());}
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

/* Error lexico */


{ID_INVALIDO} {
              huboErroresLexicos = true;
              erroresLexicos.append("Error lexico en linea ").append(yyline + 1)
                  .append(", columna ").append(yycolumn + 1)
                  .append(": identificador no puede comenzar con un digito '")
                  .append(yytext()).append("'")
                  .append(System.lineSeparator());
            }

[^]     { huboErroresLexicos = true;
              erroresLexicos.append("Error lexico en linea ").append(yyline + 1)
                  .append(", columna ").append(yycolumn + 1)
                  .append(": caracter no reconocido '").append(yytext()).append("'")
                  .append(System.lineSeparator()); }