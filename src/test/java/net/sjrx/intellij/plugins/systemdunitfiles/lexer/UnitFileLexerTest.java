package net.sjrx.intellij.plugins.systemdunitfiles.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;

public class UnitFileLexerTest extends LexerTestCase {
  
  public void testLineContinuationSimple() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Value \\\n"
                        + ";comment\n"
                        + " Hello";
  
    String expectedPsiTree = "unit configuration file (systemd)(0,23)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,23)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]')(0,5)\n"
                             + "    PsiWhiteSpace('\\n')(5,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,23)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value \\\\nHello')(10,23)";
    /*
     * Exercise SUT
     */
  
    UnitFileLexerAdapter lexer = new UnitFileLexerAdapter();
    
    lexer.start(sourceCode);
  
    while (lexer.getCurrentPosition().getOffset() != lexer.getBufferEnd()) {
      System.out.println(lexer.getTokenType());
      lexer.advance();
    }
    
    lexer.advance();

  }
  
  @Override
  protected Lexer createLexer() {
    return new UnitFileLexerAdapter();
  }
  
  @Override
  protected String getDirPath() {
    return null;
  }
}
