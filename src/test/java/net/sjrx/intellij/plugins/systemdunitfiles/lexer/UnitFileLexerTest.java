package net.sjrx.intellij.plugins.systemdunitfiles.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;

public class UnitFileLexerTest extends LexerTestCase {

  public void testWithoutSection() {
    String sourceCode = "Key = Value \n";
    doTest(sourceCode,
            "KEY ('Key')\n" +
                    "WHITE_SPACE (' ')\n" +
                    "SEPARATOR ('=')\n" +
                    "COMPLETED_VALUE (' Value ')\n" +
                    "WHITE_SPACE ('\\n')\n"
    );
    checkCorrectRestart(sourceCode);
  }

  public void testEmptyValue() {
    String sourceCode = "EmptyValue =\n";
    doTest(sourceCode,
            "KEY ('EmptyValue')\n" +
                    "WHITE_SPACE (' ')\n" +
                    "SEPARATOR ('=')\n" +
                    "WHITE_SPACE ('\\n')\n"
    );
    checkCorrectRestart(sourceCode);
  }

  public void testUnfinishedSectionName() {
    String sourceCode = "[Name\n";
    doTest(sourceCode,
            "SECTION ('[Name')\n" +
                    "WHITE_SPACE ('\\n')\n"
    );
    checkCorrectRestart(sourceCode);
  }

  public void testInlineComments() {
    // Fixture Setup
    String sourceCode = "[One] \n"
            + "Key =Value \\\n"
            + ";comment\n"
            + " Hello";
    doTest(sourceCode,
            "SECTION ('[One]')\n" +
                    "WHITE_SPACE (' \\n')\n" +
                    "KEY ('Key')\n" +
                    "WHITE_SPACE (' ')\n" +
                    "SEPARATOR ('=')\n" +
                    "CONTINUING_VALUE ('Value \\')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "COMMENT (';comment')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "COMPLETED_VALUE (' Hello')");
    checkCorrectRestart(sourceCode);
  }

  public void testExampleCode() {
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

    doTest(sourceCode,
            "SECTION ('[Section A]')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "KEY ('KeyOne')\n" +
                    "SEPARATOR ('=')\n" +
                    "COMPLETED_VALUE ('value 1')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "KEY ('KeyTwo')\n" +
                    "SEPARATOR ('=')\n" +
                    "COMPLETED_VALUE ('value 2')\n" +
                    "WHITE_SPACE ('\\n\\n')\n" +
                    "COMMENT ('# a comment')\n" +
                    "WHITE_SPACE ('\\n\\n')\n" +
                    "SECTION ('[Section B]')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "KEY ('Setting')\n" +
                    "SEPARATOR ('=')\n" +
                    "COMPLETED_VALUE ('\"something\" \"some thing\" \"…\"')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "KEY ('KeyTwo')\n" +
                    "SEPARATOR ('=')\n" +
                    "CONTINUING_VALUE ('value 2 \\')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "COMPLETED_VALUE ('       value 2 continued')\n" +
                    "WHITE_SPACE ('\\n\\n')\n" +
                    "SECTION ('[Section C]')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "KEY ('KeyThree')\n" +
                    "SEPARATOR ('=')\n" +
                    "CONTINUING_VALUE ('value 2\\')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "COMMENT ('# this line is ignored')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "COMMENT ('; this line is ignored too')\n" +
                    "WHITE_SPACE ('\\n')\n" +
                    "COMPLETED_VALUE ('       value 2 continued')\n" +
                    "WHITE_SPACE ('\\n')"
    );
    checkCorrectRestart(sourceCode);
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
