package net.sjrx.intellij.plugins.systemdunitfiles.parser;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ParsingTestCase;

/**
 * Tests for parsing of systemd unit files.
 *
 * <p>
 * Note: Unlike the tutorial (http://www.jetbrains.org/intellij/sdk/docs/tutorials/writing_tests_for_plugins/parsing_test.html) we
 * keep the source and result _in_ the test. The author feels this is more readable and easier to debug then a bunch of random
 * files lying around, even it breaks convention.
 *
 * One idea might be to use a different language for this other than Java, the author played with Groovy, but didn't like the multi-line
 * support in IntellIJ. Scala has problems subtyping ParsingTestCase as protected static members can't be used. Kotlin was also tried
 * but gradle was finicky, and I guess this is something that can be changed in the future.
 *
 * </p>
 *
 *
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

    String expectedPsiTree = "systemd service unit configuration(0,0)\n"
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

    String expectedPsiTree = "systemd service unit configuration(0,2)\n"
                             + "  PsiWhiteSpace('\\n\\n')(0,2)";


    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode);

    // Verification
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testSingleLineCommentParsesFine() {
    // Fixture Setup
    String sourceCode = "#Hello";

    String expectedPsiTree = "systemd service unit configuration(0,6)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Hello')(0,6)";

    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode);

    // Verification
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testSingleLineCommentWithSubsequentWhitespaceParsesFine() {

    // Fixture Setup
    String sourceCode = "#Hello  \n \n \n";

    String expectedPsiTree = "systemd service unit configuration(0,13)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Hello  ')(0,8)\n"
                             + "  PsiWhiteSpace('\\n \\n \\n')(8,13)";

    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode);

    // Verification
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testSingleLineCommentWithPrecedingWhiteSpaceParsesFine() {
    // Fixture Setup
    String sourceCode = "\n \n \n#Hello  ";

    String expectedPsiTree = "systemd service unit configuration(0,13)\n"
                             + "  PsiWhiteSpace('\\n \\n \\n')(0,5)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Hello  ')(5,13)\n";


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

    String expectedPsiTree = "systemd service unit configuration(0,15)\n"
                             + "  PsiElement(UnitFileTokenType{SECTION})('[One]')(0,5)\n"
                             + "  PsiWhiteSpace('\\n')(5,6)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(6,15)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value')(10,15)\n";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testLineContinuationSimple() {
    /*
     * Fixture Setup
     */
    String sourceCode = "[One]\n"
                        + "Key=Value \\\n"
                        + "Hello";

    String expectedPsiTree = "systemd service unit configuration(0,23)\n"
                             + "  PsiElement(UnitFileTokenType{SECTION})('[One]')(0,5)\n"
                             + "  PsiWhiteSpace('\\n')(5,6)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(6,23)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value \\\\nHello')(10,23)";
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

    String expectedPsiTree = "systemd service unit configuration(0,25)\n"
                             + "  PsiElement(UnitFileTokenType{SECTION})('[One]')(0,5)\n"
                             + "  PsiWhiteSpace('\\n')(5,6)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(6,25)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value \\\\nFoo=Bar')(10,25)";
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
                        + ";comment";

    String expectedPsiTree = "systemd service unit configuration(0,26)\n"
                             + "  PsiElement(UnitFileTokenType{SECTION})('[One]')(0,5)\n"
                             + "  PsiWhiteSpace('\\n')(5,6)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(6,26)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value \\\\n;comment')(10,26)";
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

    String expectedPsiTree = "systemd service unit configuration(0,33)\n"
                             + "  PsiElement(UnitFileTokenType{SECTION})('[One]')(0,5)\n"
                             + "  PsiWhiteSpace('\\n')(5,6)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(6,25)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value \\\\n[Hello]')(10,25)\n"
                             + "  PsiWhiteSpace('\\n')(25,26)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(26,33)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Foo')(26,29)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(29,30)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Bar')(30,33)";
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

    String expectedPsiTree = "systemd service unit configuration(0,16)\n"
                             + "  PsiElement(UnitFileTokenType{SECTION})('[One]')(0,5)\n"
                             + "  PsiWhiteSpace('\\n')(5,6)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(6,15)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Key')(6,9)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(9,10)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value')(10,15)\n"
                             + "  PsiWhiteSpace('\\n')(15,16)";
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



    String expectedPsiTree = "systemd service unit configuration(0,52)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Preamble')(0,9)\n"
                             + "  PsiWhiteSpace('\\n')(9,10)\n"
                             + "  PsiElement(UnitFileTokenType{SECTION})('[One]')(10,15)\n"
                             + "  PsiWhiteSpace('\\n')(15,16)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#One')(16,20)\n"
                             + "  PsiWhiteSpace('\\n')(20,21)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(21,30)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Key')(21,24)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(24,25)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value')(25,30)\n"
                             + "  PsiWhiteSpace('\\n\\n\\n')(30,33)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Two')(33,37)\n"
                             + "  PsiWhiteSpace('\\n\\n')(37,39)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(39,52)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Second')(39,45)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(45,46)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value2')(46,52)";
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



    String expectedPsiTree = "systemd service unit configuration(0,52)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})(';Preamble')(0,9)\n"
                             + "  PsiWhiteSpace('\\n')(9,10)\n"
                             + "  PsiElement(UnitFileTokenType{SECTION})('[One]')(10,15)\n"
                             + "  PsiWhiteSpace('\\n')(15,16)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})(';One')(16,20)\n"
                             + "  PsiWhiteSpace('\\n')(20,21)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(21,30)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Key')(21,24)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(24,25)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value')(25,30)\n"
                             + "  PsiWhiteSpace('\\n\\n\\n')(30,33)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})(';Two')(33,37)\n"
                             + "  PsiWhiteSpace('\\n\\n')(37,39)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(39,52)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Second')(39,45)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(45,46)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value2')(46,52)";
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



    String expectedPsiTree = "systemd service unit configuration(0,78)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})(';Preamble')(0,9)\n"
                             + "  PsiWhiteSpace('\\n\\n')(9,11)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Preable 2')(11,21)\n"
                             + "  PsiWhiteSpace('\\n')(21,22)\n"
                             + "  PsiElement(UnitFileTokenType{SECTION})('[One]')(22,27)\n"
                             + "  PsiWhiteSpace('\\n')(27,28)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#One')(28,32)\n"
                             + "  PsiWhiteSpace('\\n')(32,33)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})(';Two')(33,37)\n"
                             + "  PsiWhiteSpace('\\n')(37,38)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(38,47)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Key')(38,41)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(41,42)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value')(42,47)\n"
                             + "  PsiWhiteSpace('\\n\\n\\n')(47,50)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})(';Three')(50,56)\n"
                             + "  PsiWhiteSpace('\\n\\n')(56,58)\n"
                             + "  PsiComment(UnitFileTokenType{COMMENT})('#Four')(58,63)\n"
                             + "  PsiWhiteSpace('\\n\\n')(63,65)\n"
                             + "  UnitFilePropertyImpl(PROPERTY)(65,78)\n"
                             + "    PsiElement(UnitFileTokenType{KEY})('Second')(65,71)\n"
                             + "    PsiElement(UnitFileTokenType{SEPARATOR})('=')(71,72)\n"
                             + "    PsiElement(UnitFileTokenType{VALUE})('Value2')(72,78)";
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
