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

NONCRLF=[^\r\n\u2028\u2029\u000B\u000C\u0085]

// White space
EOL="\r\n"|"\r"|"\n"
LINE_WS=[\ \t\f]
WHITE_SPACE=({LINE_WS}|{EOL})+

// The section header should actually be more restricted than this, however we will allow anything as a character
// and use an annotator to flag the error.

SECTION_HEADER=\[[^\]\r\n\u2028\u2029\u000B\u000C\u0085]*\]?

// We don't allow whitespace or the separator in the key characters
KEY_CHARACTER=[^=\ \n\t\f\\] | "\\ "

// First value doesn't allow spaces
VALUE_CHARACTERS={NONCRLF}*

LINE_CONTINUATION="\\"

CONTINUING_VALUE={VALUE_CHARACTERS}{LINE_CONTINUATION}
COMPLETED_VALUE={VALUE_CHARACTERS}

// Comments can start with either ; or a #
COMMENT=[#;]{NONCRLF}*

SEPARATOR=[=]

%state WAITING_FOR_SEPARATOR
%state WAITING_FOR_VALUE
%state VALUE_CONTINUATION

%%

/*
 * Lexical rules http://jflex.de/manual.html#LexRules
 */
<YYINITIAL> {WHITE_SPACE}                        { return TokenType.WHITE_SPACE; }
<YYINITIAL> {COMMENT}                            { return UnitFileElementTypeHolder.COMMENT; }
<YYINITIAL> {SECTION_HEADER}                     { return UnitFileElementTypeHolder.SECTION; }
<YYINITIAL> {KEY_CHARACTER}+                     { yybegin(WAITING_FOR_SEPARATOR); return UnitFileElementTypeHolder.KEY; }

<WAITING_FOR_SEPARATOR> {LINE_WS}                { return TokenType.WHITE_SPACE; }
<WAITING_FOR_SEPARATOR> {SEPARATOR}              { yybegin(WAITING_FOR_VALUE); return UnitFileElementTypeHolder.SEPARATOR; }
// fallbacks to not beak whole file on text editing
<WAITING_FOR_SEPARATOR> {SECTION_HEADER}         { yybegin(YYINITIAL); return UnitFileElementTypeHolder.SECTION; }
<WAITING_FOR_SEPARATOR> {COMMENT}                { yybegin(YYINITIAL); return UnitFileElementTypeHolder.COMMENT; }
<WAITING_FOR_SEPARATOR> {WHITE_SPACE}            { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

// Empty value
<WAITING_FOR_VALUE> {EOL}                                            { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
// Empty lines and lines starting with "#" or ";" are ignored
<VALUE_CONTINUATION> {COMMENT}                                       { return UnitFileElementTypeHolder.COMMENT; }
<VALUE_CONTINUATION> {EOL}+                                          { return TokenType.WHITE_SPACE; }
<WAITING_FOR_VALUE, VALUE_CONTINUATION> {LINE_WS}*{CONTINUING_VALUE} { yybegin(VALUE_CONTINUATION); return UnitFileElementTypeHolder.CONTINUING_VALUE; }
<WAITING_FOR_VALUE, VALUE_CONTINUATION> {LINE_WS}*{COMPLETED_VALUE}  { yybegin(YYINITIAL); return UnitFileElementTypeHolder.COMPLETED_VALUE; }

[^]                                              { return TokenType.BAD_CHARACTER; }
