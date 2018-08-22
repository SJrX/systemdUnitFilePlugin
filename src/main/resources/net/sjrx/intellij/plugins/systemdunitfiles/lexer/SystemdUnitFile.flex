/*
* Much of this was adapted from the example for Java properties files which are similar to systemd.syntax
* (http://www.jetbrains.org/intellij/sdk/docs/tutorials/custom_language_support/lexer_and_parser_definition.html)
*
* Other links:
* https://www.freedesktop.org/software/systemd/man/systemd.syntax.html#
* https://www.freedesktop.org/software/systemd/man/systemd.unit.html#
* https://specifications.freedesktop.org/desktop-entry-spec/latest/ar01s03.html
*
*/

/*
* User code section: http://jflex.de/manual.html#user-code
*/
package net.sjrx.intellij.plugins.systemdunitfiles.generated;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;


/*
 * Options http://jflex.de/manual.html#options-and-declarations
 */
%%

%class UnitFileLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{ return;
%eof}
%public


// New Line Character
CRLF=\R

// White space
SAME_LINE_WHITESPACE = [\ \t]
WHITE_SPACE=[\ \n\t\f]

// Headers are [ Non-ASCII Control characters ]
// TODO FIX this should also allow ASCII Characters, maybe try [:cntrl:]
// https://specifications.freedesktop.org/desktop-entry-spec/latest/ar01s03.html
// The \p{Cc} is a control character class (see Mastering Regular Expressions, p. 123)
SECTION_HEADER= \[[^\]]+\]

// We don't allow whitespace or the separator in the key characters
KEY_CHARACTER=[^=\ \n\t\f\\] | "\\ "

// First value doesn't allow spaces
VALUE_CHARACTER=[^\n\t\f\\] | "\\"{CRLF} | "\\".

// Comments can start with either ; or a #
COMMENT=("#"|";")[^\n]*
SEPARATOR=[=]

%state IN_SECTION

%state WAITING_FOR_SEPARATOR
%state WAITING_FOR_VALUE

%%

/*
 * Lexical rules http://jflex.de/manual.html#LexRules
 */

<YYINITIAL, IN_SECTION> {COMMENT}                               { return UnitFileElementTypeHolder.COMMENT; }

<YYINITIAL, IN_SECTION> {SECTION_HEADER}                        { yybegin(IN_SECTION); return UnitFileElementTypeHolder.SECTION; }

<YYINITIAL, IN_SECTION> {CRLF}({CRLF}|{WHITE_SPACE})+           { return TokenType.WHITE_SPACE; }

<IN_SECTION> {KEY_CHARACTER}+                                   { yybegin(WAITING_FOR_SEPARATOR); return UnitFileElementTypeHolder.KEY; }

<WAITING_FOR_SEPARATOR> {SAME_LINE_WHITESPACE}*{SEPARATOR}{SAME_LINE_WHITESPACE}* { yybegin(WAITING_FOR_VALUE); return UnitFileElementTypeHolder.SEPARATOR; }

<WAITING_FOR_VALUE> {VALUE_CHARACTER}*                          { yybegin(IN_SECTION); return UnitFileElementTypeHolder.VALUE; }

<YYINITIAL, IN_SECTION>({CRLF}|{WHITE_SPACE})+                  { return TokenType.WHITE_SPACE; }


.                                                               { return TokenType.BAD_CHARACTER; }