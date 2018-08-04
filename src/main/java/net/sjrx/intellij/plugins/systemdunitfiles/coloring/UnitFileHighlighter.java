package net.sjrx.intellij.plugins.systemdunitfiles.coloring;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder;
import net.sjrx.intellij.plugins.systemdunitfiles.lexer.UnitFileLexerAdapter;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;


public class UnitFileHighlighter extends SyntaxHighlighterBase {

  public static final TextAttributesKey SECTION
    = createTextAttributesKey("UNIT_FILE_SECTION", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final TextAttributesKey KEY
    = createTextAttributesKey("UNIT_FILE_KEY", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey SEPARATOR
    = createTextAttributesKey("UNIT_FILE_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey VALUE
    = createTextAttributesKey("UNIT_FILE_VALUE", DefaultLanguageHighlighterColors.STRING);
  private static final TextAttributesKey COMMENT
    = createTextAttributesKey("UNIT_FILE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
  private static final TextAttributesKey BAD_CHARACTER
    = createTextAttributesKey("UNIT_FILE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);



  private static final TextAttributesKey[] SECTION_KEYS = new TextAttributesKey[]{SECTION};
  private static final TextAttributesKey[] KEY_KEYS = new TextAttributesKey[]{KEY};
  private static final TextAttributesKey[] SEPARATOR_KEYS = new TextAttributesKey[]{SEPARATOR};
  private static final TextAttributesKey[] VALUE_KEYS = new TextAttributesKey[]{VALUE};
  private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
  private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
  private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};

  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new UnitFileLexerAdapter();
  }

  @NotNull
  @Override
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    if (tokenType.equals(UnitFileElementTypeHolder.SECTION)) {
      return SECTION_KEYS;
    } else if (tokenType.equals(UnitFileElementTypeHolder.SEPARATOR)) {
      return SEPARATOR_KEYS;
    } else if (tokenType.equals(UnitFileElementTypeHolder.KEY)) {
      return KEY_KEYS;
    } else if (tokenType.equals(UnitFileElementTypeHolder.VALUE)) {
      return VALUE_KEYS;
    } else if (tokenType.equals(UnitFileElementTypeHolder.COMMENT)) {
      return COMMENT_KEYS;
    } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
      return BAD_CHAR_KEYS;
    } else {
      return EMPTY_KEYS;
    }
  }
}

