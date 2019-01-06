package net.sjrx.intellij.plugins.systemdunitfiles.parser;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ParsingTestCase;

/**
 * Tests for parsing of systemd unit files.
 *
 * <p>Note: Unlike the tutorial (http://www.jetbrains.org/intellij/sdk/docs/tutorials/writing_tests_for_plugins/parsing_test.html) we
 * keep the source and result _in_ the test. The author feels this is more readable and easier to debug then a bunch of random
 * files lying around, even it breaks convention.
 * <p>
 * One idea might be to use a different language for this other than Java, the author played with Groovy, but didn't like the multi-line
 * support in IntellIJ. Scala has problems subtyping ParsingTestCase as protected static members can't be used. Kotlin was also tried
 * but gradle was finicky, and I guess this is something that can be changed in the future.
 *
 * </p>
 */
public class UnitFileParserTest extends ParsingTestCase {
  public UnitFileParserTest() {
    super("", "service", new UnitFileParserDefinition());
  }

  public void testEmptyFileParsesFine() {
    /*
     * Fixture Setup
     */
    String sourceCode = "";

    String expectedPsiTree = "unit configuration file (systemd)(0,0)\n"
                             + "  <empty list>";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testTwoNewLinesParsesFine() {
    // Fixture Setup
    String sourceCode = "\n\n";

    String expectedPsiTree = "unit configuration file (systemd)(0,2)\n"
                             + "  PsiElement(UnitFileTokenType{CRLF})('\\n\\n')(0,2)";


    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode);

    // Verification
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testSingleLineCommentParsesFine() {
    // Fixture Setup
    String sourceCode = "#Hello";

    String expectedPsiTree = "unit configuration file (systemd)(0,6)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Hello')(0,6)";

    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode);

    // Verification
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testSingleLineCommentWithSubsequentWhitespaceParsesFine() {

    // Fixture Setup
    String sourceCode = "#Hello  \n \n \n";

    String expectedPsiTree = "unit configuration file (systemd)(0,13)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Hello  \\n')(0,9)\n"
                             + "  PsiElement(UnitFileTokenType{CRLF})(' \\n \\n')(9,13)";

    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode);

    // Verification
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testSingleLineCommentWithPrecedingWhiteSpaceParsesFine() {
    // Fixture Setup
    String sourceCode = "\n \n \n#Hello  ";

    String expectedPsiTree = "unit configuration file (systemd)(0,13)\n"
                             + "  PsiElement(UnitFileTokenType{CRLF})('\\n \\n \\n')(0,5)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Hello  ')(5,13)";


    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode);

    // Verification
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testOneSectionAndOneKeyValuePairMatchesExpectedValue() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Value";

    String expectedPsiTree = "unit configuration file (systemd)(0,15)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,15)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(0,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,15)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      UnitFileValueImpl(VALUE)(10,15)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value')(10,15)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }
  
  public void testOneSectionAndTwoKeyValuePairMatchesExpectedValue() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Value\n"
                        + "SecondKey=SecondValue";
    
    String expectedPsiTree = "unit configuration file (systemd)(0,37)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,37)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(0,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,16)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      UnitFileValueImpl(VALUE)(10,16)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value\\n')(10,16)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(16,37)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('SecondKey')(16,25)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(25,26)\n"
                             + "      UnitFileValueImpl(VALUE)(26,37)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('SecondValue')(26,37)";
    /*
     * Exercise SUT
     */
    
    String parseTree = convertSourceToParseTree(sourceCode);
    
    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }
  
  
  /*
   * This test won't pass presently, because we pushed the new line processing into the Lexer.
   */
  private void ignoredTestOneSectionAndKeyValueOnSameLineResultsInAnError() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One] Key=Value";
    
    String expectedPsiTree = "unit configuration file (systemd)(0,37)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,37)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(0,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,16)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value\\n')(10,16)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(16,37)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('SecondKey')(16,25)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(25,26)\n"
                             + "      PsiElement(UnitFileTokenType{COMPLETED_VALUE})('SecondValue')(26,37)";
    /*
     * Exercise SUT
     */
    
    String parseTree = convertSourceToParseTree(sourceCode);
    
    /*
     * Verification
     */
    fail();
    //assertSameLines(expectedPsiTree, parseTree);
   
  }

  public void testLineContinuationSimple() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Value \\\n"
                        + "Hello";

    String expectedPsiTree = "unit configuration file (systemd)(0,23)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,23)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(0,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,23)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      UnitFileValueImpl(VALUE)(10,23)\n"
                             + "        PsiElement(UnitFileTokenType{CONTINUING_VALUE})('Value \\\\n')(10,18)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Hello')(18,23)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testLineContinuationWithTabInlineSimple() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Val\tue \\\n"
                        + "Hello";
    
    String expectedPsiTree = "unit configuration file (systemd)(0,24)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,24)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(0,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,24)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      UnitFileValueImpl(VALUE)(10,24)\n"
                             + "        PsiElement(UnitFileTokenType{CONTINUING_VALUE})('Val\\tue \\\\n')(10,19)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Hello')(19,24)";
    /*
     * Exercise SUT
     */
    
    String parseTree = convertSourceToParseTree(sourceCode);
    
    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }
  
  public void testLineContinuationWithWhitespaceStartingNextLine() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Value \\\n"
                        + "\tHello";
    
    String expectedPsiTree = "unit configuration file (systemd)(0,24)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,24)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(0,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,24)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      UnitFileValueImpl(VALUE)(10,24)\n"
                             + "        PsiElement(UnitFileTokenType{CONTINUING_VALUE})('Value \\\\n')(10,18)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('\\tHello')(18,24)";
    /*
     * Exercise SUT
     */
    
    String parseTree = convertSourceToParseTree(sourceCode);
    
    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }


  public void testLineContinuationWithKeys() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Value \\\n"
                        + "Foo=Bar";

    String expectedPsiTree = "unit configuration file (systemd)(0,25)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,25)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(0,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,25)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      UnitFileValueImpl(VALUE)(10,25)\n"
                             + "        PsiElement(UnitFileTokenType{CONTINUING_VALUE})('Value \\\\n')(10,18)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Foo=Bar')(18,25)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testLineContinuationWithComment() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Value \\\n"
                        + ";comment\n"
                        + "Hello";

    String expectedPsiTree = "unit configuration file (systemd)(0,32)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,32)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(0,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,32)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      UnitFileValueImpl(VALUE)(10,32)\n"
                             + "        PsiElement(UnitFileTokenType{CONTINUING_VALUE})('Value \\\\n')(10,18)\n"
                             + "        PsiComment(UnitFileTokenType{COMMENT})(';comment\\n')(18,27)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Hello')(27,32)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testLineContinuationWithSection() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Value \\\n"
                        + "[Hello]\n"
                        + "Foo=Bar";

    String expectedPsiTree = "unit configuration file (systemd)(0,33)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,33)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(0,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,26)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      UnitFileValueImpl(VALUE)(10,26)\n"
                             + "        PsiElement(UnitFileTokenType{CONTINUING_VALUE})('Value \\\\n')(10,18)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('[Hello]\\n')(18,26)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(26,33)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Foo')(26,29)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(29,30)\n"
                             + "      UnitFileValueImpl(VALUE)(30,33)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Bar')(30,33)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }


  public void testOneSectionAndOneKeyValuePairWithNewLineAtEndMatchesExpectedValue() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Value\n";

    String expectedPsiTree = "unit configuration file (systemd)(0,16)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,16)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(0,6)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(6,16)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "      UnitFileValueImpl(VALUE)(10,16)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value\\n')(10,16)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testCommentsInSectionParseSuccessfully() {
    /*
     * Fixture Setup
     */
    String sourceCode = "#Preamble\n"
                        + "[One]\n"
                        + "#One\n"
                        + "Key=Value\n\n\n"
                        + "#Two\n\n"
                        + "Second=Value2";


    String expectedPsiTree = "unit configuration file (systemd)(0,52)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Preamble\\n')(0,10)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(10,52)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(10,16)\n"
                             + "    PsiComment(UnitFileTokenType{COMMENT})('#One\\n')(16,21)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(21,31)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(21,24)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(24,25)\n"
                             + "      UnitFileValueImpl(VALUE)(25,31)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value\\n')(25,31)\n"
                             + "    PsiElement(UnitFileTokenType{CRLF})('\\n\\n')(31,33)\n"
                             + "    PsiComment(UnitFileTokenType{COMMENT})('#Two\\n')(33,38)\n"
                             + "    PsiElement(UnitFileTokenType{CRLF})('\\n')(38,39)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(39,52)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Second')(39,45)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(45,46)\n"
                             + "      UnitFileValueImpl(VALUE)(46,52)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value2')(46,52)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testSemiColonCommentsInSectionParseSuccessfully() {
    /*
     * Fixture Setup
     */
    String sourceCode = ";Preamble\n"
                        + "[One]\n"
                        + ";One\n"
                        + "Key=Value\n\n\n"
                        + ";Two\n\n"
                        + "Second=Value2";


    String expectedPsiTree = "unit configuration file (systemd)(0,52)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})(';Preamble\\n')(0,10)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(10,52)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(10,16)\n"
                             + "    PsiComment(UnitFileTokenType{COMMENT})(';One\\n')(16,21)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(21,31)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(21,24)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(24,25)\n"
                             + "      UnitFileValueImpl(VALUE)(25,31)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value\\n')(25,31)\n"
                             + "    PsiElement(UnitFileTokenType{CRLF})('\\n\\n')(31,33)\n"
                             + "    PsiComment(UnitFileTokenType{COMMENT})(';Two\\n')(33,38)\n"
                             + "    PsiElement(UnitFileTokenType{CRLF})('\\n')(38,39)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(39,52)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Second')(39,45)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(45,46)\n"
                             + "      UnitFileValueImpl(VALUE)(46,52)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value2')(46,52)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testMixedCommentStylesParsesSuccessfully() {
    /*
     * Fixture Setup
     */
    String sourceCode = ";Preamble\n"
                        + "\n"
                        + "#Preable 2\n"
                        + "[One]\n"
                        + "#One\n"
                        + ";Two\n"
                        + "Key=Value\n\n\n"
                        + ";Three\n\n"
                        + "#Four\n\n"
                        + "Second=Value2";


    String expectedPsiTree = "unit configuration file (systemd)(0,78)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})(';Preamble\\n')(0,10)\n"
                             + "  PsiElement(UnitFileTokenType{CRLF})('\\n')(10,11)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Preable 2\\n')(11,22)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(22,78)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[One]\\n')(22,28)\n"
                             + "    PsiComment(UnitFileTokenType{COMMENT})('#One\\n')(28,33)\n"
                             + "    PsiComment(UnitFileTokenType{COMMENT})(';Two\\n')(33,38)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(38,48)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Key')(38,41)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(41,42)\n"
                             + "      UnitFileValueImpl(VALUE)(42,48)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value\\n')(42,48)\n"
                             + "    PsiElement(UnitFileTokenType{CRLF})('\\n\\n')(48,50)\n"
                             + "    PsiComment(UnitFileTokenType{COMMENT})(';Three\\n')(50,57)\n"
                             + "    PsiElement(UnitFileTokenType{CRLF})('\\n')(57,58)\n"
                             + "    PsiComment(UnitFileTokenType{COMMENT})('#Four\\n')(58,64)\n"
                             + "    PsiElement(UnitFileTokenType{CRLF})('\\n')(64,65)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(65,78)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Second')(65,71)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(71,72)\n"
                             + "      UnitFileValueImpl(VALUE)(72,78)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('Value2')(72,78)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testEmptyValueInSectionWithStuffAfterItParsesSuccessfully() {
    String sourceCode = "[Unit]\n"
                        + "EmptyValue=\n"
                        + "Before=test";


    String expectedPsiTree = "unit configuration file (systemd)(0,30)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,30)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[Unit]\\n')(0,7)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(7,19)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('EmptyValue')(7,17)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(17,18)\n"
                             + "      UnitFileValueImpl(VALUE)(18,19)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('\\n')(18,19)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(19,30)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Before')(19,25)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(25,26)\n"
                             + "      UnitFileValueImpl(VALUE)(26,30)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('test')(26,30)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testEmptyValueBeforeEofParsesSuccessfully() {
    String sourceCode = "[Unit]\n"
                        + "EmptyValue=";


    String expectedPsiTree = "unit configuration file (systemd)(0,18)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,18)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[Unit]\\n')(0,7)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(7,18)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('EmptyValue')(7,17)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(17,18)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testEmptyValueBeforeNewLineAndEofParsesSuccessfully() {
    String sourceCode = "[Unit]\n"
                        + "EmptyValue=\n";


    String expectedPsiTree = "unit configuration file (systemd)(0,19)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,19)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[Unit]\\n')(0,7)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(7,19)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('EmptyValue')(7,17)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(17,18)\n"
                             + "      UnitFileValueImpl(VALUE)(18,19)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('\\n')(18,19)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testEmptyValueBeforeCommentParsesSuccessfully() {
    String sourceCode = "[Unit]\n"
                        + "EmptyValue=\n"
                        + "#Hello";


    String expectedPsiTree = "unit configuration file (systemd)(0,25)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,19)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[Unit]\\n')(0,7)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(7,19)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('EmptyValue')(7,17)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(17,18)\n"
                             + "      UnitFileValueImpl(VALUE)(18,19)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('\\n')(18,19)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Hello')(19,25)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testEmptyValueBeforeSectionParsesSuccessfully() {
    String sourceCode = "[Unit]\n"
                        + "EmptyValue=\n"
                        + "[Install]";


    String expectedPsiTree = "unit configuration file (systemd)(0,28)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,19)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[Unit]\\n')(0,7)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(7,19)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('EmptyValue')(7,17)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(17,18)\n"
                             + "      UnitFileValueImpl(VALUE)(18,19)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('\\n')(18,19)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(19,28)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[Install]')(19,28)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testIncompleteSectionHeaderHasError() {
    String sourceCode = "[Uni\n";

    String expectedPsiTree = "unit configuration file (systemd)(0,5)\n"
                             + "  PsiErrorElement:<comment>, <new line> or <section header> expected, got '[Uni'(0,5)\n"
                             + "    PsiElement(BAD_CHARACTER)('[Uni\\n')(0,5)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testKeyWithNoSeparatorHasError() {
    String sourceCode = "[Unit]\n"
                        + "Foo";

    String expectedPsiTree = "unit configuration file (systemd)(0,10)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,10)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[Unit]\\n')(0,7)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(7,10)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Foo')(7,10)\n"
                             + "      PsiErrorElement:<key-value separator (=)> expected(10,10)\n"
                             + "        <empty list>";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }
  
  public void testValueConcatenationWorksProperly() {
    fail("Not implemented");
  }

  
  
  public void testExampleCodeParsesSuccessfully() {


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

    String expectedPsiTree = "unit configuration file (systemd)(0,253)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,56)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[Section A]\\n')(0,12)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(12,27)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('KeyOne')(12,18)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(18,19)\n"
                             + "      UnitFileValueImpl(VALUE)(19,27)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('value 1\\n')(19,27)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(27,42)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('KeyTwo')(27,33)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(33,34)\n"
                             + "      UnitFileValueImpl(VALUE)(34,42)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('value 2\\n')(34,42)\n"
                             + "    PsiElement(UnitFileTokenType{CRLF})('\\n')(42,43)\n"
                             + "    PsiComment(UnitFileTokenType{COMMENT})('# a comment\\n')(43,55)\n"
                             + "    PsiElement(UnitFileTokenType{CRLF})('\\n')(55,56)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(56,148)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[Section B]\\n')(56,68)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(68,105)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('Setting')(68,75)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(75,76)\n"
                             + "      UnitFileValueImpl(VALUE)(76,105)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('\"something\" \"some thing\" \"…\"\\n')(76,105)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(105,147)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('KeyTwo')(105,111)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(111,112)\n"
                             + "      UnitFileValueImpl(VALUE)(112,147)\n"
                             + "        PsiElement(UnitFileTokenType{CONTINUING_VALUE})('value 2 \\\\n')(112,122)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('       value 2 continued\\n')(122,147)\n"
                             + "    PsiElement(UnitFileTokenType{CRLF})('\\n')(147,148)\n"
                             + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(148,253)\n"
                             + "    PsiElement(UnitFileTokenType{SECTION})('[Section C]\\n')(148,160)\n"
                             + "    UnitFilePropertyImpl(PROPERTY)(160,253)\n"
                             + "      PsiElement(UnitFileTokenType{KEY})('KeyThree')(160,168)\n"
                             + "      PsiElement(UnitFileTokenType{SEPARATOR})('=')(168,169)\n"
                             + "      UnitFileValueImpl(VALUE)(169,253)\n"
                             + "        PsiElement(UnitFileTokenType{CONTINUING_VALUE})('value 2\\\\n')(169,178)\n"
                             + "        PsiComment(UnitFileTokenType{COMMENT})('# this line is ignored\\n')(178,201)\n"
                             + "        PsiComment(UnitFileTokenType{COMMENT})('; this line is ignored too\\n')(201,228)\n"
                             + "        PsiElement(UnitFileTokenType{COMPLETED_VALUE})('       value 2 continued\\n')(228,253)";

    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }
  


  /**
   * Converts the source code supplied as an argument to a parse tree.
   *
   * @param sourceCode - the input source code to read
   * @return a string representation of the PsiTree
   */
  private String convertSourceToParseTree(String sourceCode) {
    PsiFile myFile = createPsiFile("a", sourceCode);
    ensureParsed(myFile);
    assertEquals(sourceCode, myFile.getText());

    return toParseTreeText(myFile, skipSpaces(), includeRanges());
  }

  @Override
  protected String getTestDataPath() {
    return "testData/code_samples";
  }

  @Override
  protected boolean skipSpaces() {
    return false;
  }

  @Override
  protected boolean includeRanges() {
    return true;
  }
}
