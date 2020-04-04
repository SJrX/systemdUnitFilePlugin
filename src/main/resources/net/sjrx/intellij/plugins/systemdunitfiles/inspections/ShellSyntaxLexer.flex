/*
* This is an attempt at a lexer which can detect shell commands, in particular the main purpose is to detect cases of
*
* <, << , >, >>, |, & in the ExecStart= and related commands, since Systemd doesn't support them.
*/

/*
* User code section: http://jflex.de/manual.html#user-code
*/
package foo;

/*
 * Options http://jflex.de/manual.html#options-and-declarations
 */
%%

%class ExecCommandLineLexer
%unicode
%function advance
%eof{ return;
%eof}
%public

// White space
SAME_LINE_WHITESPACE=[\ \t]+

SINGLE_QUOTE=[']
DOUBLE_QUOTE=[\"]

UNESCAPED_TOKEN=[^'\"]\S*




%state NOT_QUOTED

%state IN_SINGLE_QUOTE
%state IN_DOUBLE_QUOTE



%%

/*
 * Lexical rules http://jflex.de/manual.html#LexRules
 */

<YYINITIAL, IN_SECTION, VALUE_CONTINUATION> {COMMENT}                                { return UnitFileElementTypeHolder.COMMENT; }

<YYINITIAL, IN_SECTION> {SECTION_HEADER}                                             { yybegin(IN_SECTION); return UnitFileElementTypeHolder.SECTION; }

<YYINITIAL, IN_SECTION> {INCOMPLETE_SECTION_HEADER}                                  { return TokenType.BAD_CHARACTER; }

<YYINITIAL, IN_SECTION, VALUE_CONTINUATION> {CRLF}({CRLF}|{WHITE_SPACE})+                                { return UnitFileElementTypeHolder.CRLF; }

<IN_SECTION> {KEY_CHARACTER}+                                                        { yybegin(WAITING_FOR_SEPARATOR); return UnitFileElementTypeHolder.KEY; }

<WAITING_FOR_SEPARATOR> {SAME_LINE_WHITESPACE}*{SEPARATOR}{SAME_LINE_WHITESPACE}*    { yybegin(WAITING_FOR_VALUE); return UnitFileElementTypeHolder.SEPARATOR; }

<WAITING_FOR_VALUE, VALUE_CONTINUATION> {CONTINUING_VALUE}                           { yybegin(VALUE_CONTINUATION); return UnitFileElementTypeHolder.CONTINUING_VALUE; }

// Pull a value character or really any character and mark it as its value, this is really a hack :(
<WAITING_FOR_VALUE, VALUE_CONTINUATION> {COMPLETED_VALUE}                           { yybegin(IN_SECTION); return UnitFileElementTypeHolder.COMPLETED_VALUE; }

<YYINITIAL, IN_SECTION, VALUE_CONTINUATION>({CRLF}|{WHITE_SPACE})+                                       { return UnitFileElementTypeHolder.CRLF; }

[^]                                                                                  { return TokenType.BAD_CHARACTER; }