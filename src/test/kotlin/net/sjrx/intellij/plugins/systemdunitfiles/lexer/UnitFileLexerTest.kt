package net.sjrx.intellij.plugins.systemdunitfiles.lexer

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase

class UnitFileLexerTest : LexerTestCase() {
  fun testWithoutSection() {
    val sourceCode = "Key = Value \n"
    doTest(
      sourceCode,
      """
                 KEY ('Key')
                 WHITE_SPACE (' ')
                 SEPARATOR ('=')
                 COMPLETED_VALUE (' Value ')
                 WHITE_SPACE ('\n')
                 
                 """.trimIndent()
    )
    checkCorrectRestart(sourceCode)
  }

  fun testEmptyValue() {
    val sourceCode = "EmptyValue =\n"
    doTest(
      sourceCode,
      """
                 KEY ('EmptyValue')
                 WHITE_SPACE (' ')
                 SEPARATOR ('=')
                 WHITE_SPACE ('\n')
                 
                 """.trimIndent()
    )
    checkCorrectRestart(sourceCode)
  }

  fun testUnfinishedSectionName() {
    val sourceCode = "[Name\n"
    doTest(
      sourceCode,
      """
                 SECTION ('[Name')
                 WHITE_SPACE ('\n')
                 
                 """.trimIndent()
    )
    checkCorrectRestart(sourceCode)
  }

  fun testInlineComments() {
    // Fixture Setup
    val sourceCode = ("[One] \n"
      + "Key =Value \\\n"
      + ";comment\n"
      + " Hello")
    doTest(
      sourceCode,
      """
                 SECTION ('[One]')
                 WHITE_SPACE (' \n')
                 KEY ('Key')
                 WHITE_SPACE (' ')
                 SEPARATOR ('=')
                 CONTINUING_VALUE ('Value \')
                 WHITE_SPACE ('\n')
                 COMMENT (';comment')
                 WHITE_SPACE ('\n')
                 COMPLETED_VALUE (' Hello')
                 """.trimIndent()
    )
    checkCorrectRestart(sourceCode)
  }

  fun testExampleCode() {
    // Fixture Setup
    val sourceCode = """[Section A]
KeyOne=value 1
KeyTwo=value 2

# a comment

[Section B]
Setting="something" "some thing" "."
KeyTwo=value 2 \
       value 2 continued

[Section C]
KeyThree=value 2\
# this line is ignored
; this line is ignored too
       value 2 continued
"""
    doTest(
      sourceCode,
      """SECTION ('[Section A]')
WHITE_SPACE ('\n')
KEY ('KeyOne')
SEPARATOR ('=')
COMPLETED_VALUE ('value 1')
WHITE_SPACE ('\n')
KEY ('KeyTwo')
SEPARATOR ('=')
COMPLETED_VALUE ('value 2')
WHITE_SPACE ('\n\n')
COMMENT ('# a comment')
WHITE_SPACE ('\n\n')
SECTION ('[Section B]')
WHITE_SPACE ('\n')
KEY ('Setting')
SEPARATOR ('=')
COMPLETED_VALUE ('"something" "some thing" "."')
WHITE_SPACE ('\n')
KEY ('KeyTwo')
SEPARATOR ('=')
CONTINUING_VALUE ('value 2 \')
WHITE_SPACE ('\n')
COMPLETED_VALUE ('       value 2 continued')
WHITE_SPACE ('\n\n')
SECTION ('[Section C]')
WHITE_SPACE ('\n')
KEY ('KeyThree')
SEPARATOR ('=')
CONTINUING_VALUE ('value 2\')
WHITE_SPACE ('\n')
COMMENT ('# this line is ignored')
WHITE_SPACE ('\n')
COMMENT ('; this line is ignored too')
WHITE_SPACE ('\n')
COMPLETED_VALUE ('       value 2 continued')
WHITE_SPACE ('\n')"""
    )
    checkCorrectRestart(sourceCode)
  }

  override fun createLexer(): Lexer {
    return UnitFileLexerAdapter()
  }

  override fun getDirPath(): String {
    return "/tmp/"
  }
}
