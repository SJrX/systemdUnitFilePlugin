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

// New Line Character (See JFlex manual)
CRLF=\n

NONCRLF=[^\r\n\u2028\u2029\u000B\u000C\u0085]

// White space
SAME_LINE_WHITESPACE=[\ \t]*

WHITE_SPACE=[\ \n\t\f]


// The section header should actually be more restricted than this, however we will allow anything as a character
// and use an annotator to flag the error.

SECTION_HEADER=\[[^\]]+\]{SAME_LINE_WHITESPACE}{CRLF}?

/* Keep track of incomplete section headers because fixing bad character sequences don't
  cause enough relexing

  https://intellij-support.jetbrains.com/hc/en-us/community/posts/206123189-Syntax-Highlighting-gets-stuck
 */
INCOMPLETE_SECTION_HEADER = \[[^\]\n]*{CRLF}?

// We don't allow whitespace or the separator in the key characters
KEY_CHARACTER=[^=\ \n\t\f\\] | "\\ "

// First value doesn't allow spaces
VALUE_CHARACTERS={NONCRLF}*

LINE_CONTINUATION="\\"

CONTINUING_VALUE={VALUE_CHARACTERS}*{LINE_CONTINUATION}{CRLF}?
COMPLETED_VALUE={VALUE_CHARACTERS}*{CRLF}?

// Comments can start with either ; or a #
COMMENT=[#;]{NONCRLF}*{CRLF}?

SEPARATOR=[=]

%state WAITING_FOR_SEPARATOR
%state WAITING_FOR_VALUE
%state VALUE_CONTINUATION

%%

/*
 * Lexical rules http://jflex.de/manual.html#LexRules
 */

<YYINITIAL, VALUE_CONTINUATION> {COMMENT}                                { return UnitFileElementTypeHolder.COMMENT; }

<YYINITIAL> {SECTION_HEADER}                                             { return UnitFileElementTypeHolder.SECTION; }

<YYINITIAL> {INCOMPLETE_SECTION_HEADER}                                  { return TokenType.BAD_CHARACTER; }

<YYINITIAL, VALUE_CONTINUATION> {CRLF}({CRLF}|{WHITE_SPACE})+                                { return UnitFileElementTypeHolder.CRLF; }

<YYINITIAL> {KEY_CHARACTER}+                                                        { yybegin(WAITING_FOR_SEPARATOR); return UnitFileElementTypeHolder.KEY; }

<WAITING_FOR_SEPARATOR> {SAME_LINE_WHITESPACE}*{SEPARATOR}{SAME_LINE_WHITESPACE}*    { yybegin(WAITING_FOR_VALUE); return UnitFileElementTypeHolder.SEPARATOR; }

<WAITING_FOR_VALUE, VALUE_CONTINUATION> {CONTINUING_VALUE}                           { yybegin(VALUE_CONTINUATION); return UnitFileElementTypeHolder.CONTINUING_VALUE; }

// Pull a value character or really any character and mark it as its value, this is really a hack :(
<WAITING_FOR_VALUE, VALUE_CONTINUATION> {COMPLETED_VALUE}                           { yybegin(YYINITIAL); return UnitFileElementTypeHolder.COMPLETED_VALUE; }

<YYINITIAL, VALUE_CONTINUATION>({CRLF}|{WHITE_SPACE})+                                       { return UnitFileElementTypeHolder.CRLF; }

[^]                                                                                  { return TokenType.BAD_CHARACTER; }