package net.sjrx.intellij.plugins.systemdunitfiles.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileTokenType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UnitFileLexerTest extends LexerTestCase {

  public void testWithoutSection() {
    String sourceCode = "Key = Value \n";
    doTest(sourceCode,
            "UnitFileTokenType{KEY} ('Key')\n"
          + "UnitFileTokenType{SEPARATOR} (' = ')\n"
          + "UnitFileTokenType{COMPLETED_VALUE} ('Value \\n')"
    );
    checkCorrectRestart(sourceCode);
  }

  public void testEmptyValue() {
    String sourceCode = "EmptyValue =\n";
    doTest(sourceCode,
            "UnitFileTokenType{KEY} ('EmptyValue')\n"
          + "UnitFileTokenType{SEPARATOR} (' =')\n"
          + "UnitFileTokenType{COMPLETED_VALUE} ('\\n')"
    );
    checkCorrectRestart(sourceCode);
  }

  public void testInlineComments() {
    // Fixture Setup
    String sourceCode = "[One]\n"
                        + "Key=Value \\\n"
                        + ";comment\n"
                        + " Hello";
  
    // Exercise SUT
    String tokenStream = getTokenStream(sourceCode);
    
    // Verification
    assertEquals("SECTION, KEY, SEPARATOR, CONTINUING_VALUE, COMMENT, COMPLETED_VALUE", tokenStream);
  }

  public void testExampleCodeLexesProperly() {
    // Fixture Setup
    String sourceCode = "[Section A]\n"
                        + "KeyOne=value 1\n"
                        + "KeyTwo=value 2\n"
                        + "\n"
                        + "# a comment\n"
                        + "\n"
                        + "[Section B]\n"
                        + "Setting=\"something\" \"some thing\" \"…\"\n"
                        + "KeyTwo=value 2 \\\n"
                        + "       value 2 continued\n"
                        + "\n"
                        + "[Section C]\n"
                        + "KeyThree=value 2\\\n"
                        + "# this line is ignored\n"
                        + "; this line is ignored too\n"
                        + "       value 2 continued\n";

    // Exercise SUT
    String tokenStream = getTokenStream(sourceCode);
    
    // Verification
    assertEquals("SECTION, "
                 + "KEY, SEPARATOR, COMPLETED_VALUE, "
                 + "KEY, SEPARATOR, COMPLETED_VALUE, "
                 + "CRLF, "
                 + "COMMENT, "
                 + "CRLF, "
                 + "SECTION, "
                 + "KEY, SEPARATOR, COMPLETED_VALUE, "
                 + "KEY, SEPARATOR, CONTINUING_VALUE, "
                 + "COMPLETED_VALUE, "
                 + "CRLF, "
                 + "SECTION, "
                 + "KEY, SEPARATOR, CONTINUING_VALUE, "
                 + "COMMENT, "
                 + "COMMENT, "
                 + "COMPLETED_VALUE", tokenStream);
  }
  
  
  @NotNull
  private String getTokenStream(String sourceCode) {
    UnitFileLexerAdapter lexer = new UnitFileLexerAdapter();
    
    lexer.start(sourceCode);
    
    List<String> tokens = new ArrayList<>();
    
    while (lexer.getCurrentPosition().getOffset() != lexer.getBufferEnd()) {
  
      String tokenString = lexer.getTokenType().toString();
      tokenString = tokenString.replaceAll(UnitFileTokenType.class.getSimpleName(), "").replaceAll("[{}]", "");
      
      tokens.add(tokenString);
      lexer.advance();
    }
    
    return String.join(", ", tokens);
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
