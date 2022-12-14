package net.sjrx.intellij.plugins.systemdunitfiles.parser;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ParsingTestCase;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileProperty;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileValueType;

/**
 * Tests for parsing of systemd unit files.
 *
 * <p>Note: Unlike the tutorial (http://www.jetbrains.org/intellij/sdk/docs/tutorials/writing_tests_for_plugins/parsing_test.html) we
 * keep the source and result _in_ the test. The author feels this is more readable and easier to debug then a bunch of random
 * files lying around, even it breaks convention.
 * <p>
 * One idea might be to use a different language for this other than Java, the author played with Groovy, but didn't like the multi-line
 * support in IntelliJ. Scala has problems subtyping ParsingTestCase as protected static members can't be used. Kotlin was also tried
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
            + "  PsiWhiteSpace('\\n\\n')(0,2)";


    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode, false);

    // Verification
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testSingleLineCommentParsesFine() {
    // Fixture Setup
    String sourceCode = "#Hello";

    String expectedPsiTree = "unit configuration file (systemd)(0,6)\n"
            + "  PsiComment(COMMENT)('#Hello')(0,6)";

    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode);

    // Verification
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testSingleLineCommentWithSubsequentWhitespaceParsesFine() {

    // Fixture Setup
    String sourceCode = "#Hello  \n \n \n";

    String expectedPsiTree = "unit configuration file (systemd)(0,13)\n"
            + "  PsiComment(COMMENT)('#Hello  ')(0,8)\n"
            + "  PsiWhiteSpace('\\n \\n \\n')(8,13)";

    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode, false);

    // Verification
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testSingleLineCommentWithPrecedingWhiteSpaceParsesFine() {
    // Fixture Setup
    String sourceCode = "\n \n \n#Hello  ";

    String expectedPsiTree = "unit configuration file (systemd)(0,13)\n"
            + "  PsiWhiteSpace('\\n \\n \\n')(0,5)\n"
            + "  PsiComment(COMMENT)('#Hello  ')(5,13)";


    // Exercise SUT
    String parseTree = convertSourceToParseTree(sourceCode, false);

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
            + "    PsiElement(SECTION)('[One]')(0,5)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(6,15)\n"
            + "      PsiElement(KEY)('Key')(6,9)\n"
            + "      PsiElement(SEPARATOR)('=')(9,10)\n"
            + "      UnitFileValueImpl(VALUE)(10,15)\n"
            + "        PsiElement(COMPLETED_VALUE)('Value')(10,15)";
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
            + "    PsiElement(SECTION)('[One]')(0,5)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(6,15)\n"
            + "      PsiElement(KEY)('Key')(6,9)\n"
            + "      PsiElement(SEPARATOR)('=')(9,10)\n"
            + "      UnitFileValueImpl(VALUE)(10,15)\n"
            + "        PsiElement(COMPLETED_VALUE)('Value')(10,15)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(16,37)\n"
            + "      PsiElement(KEY)('SecondKey')(16,25)\n"
            + "      PsiElement(SEPARATOR)('=')(25,26)\n"
            + "      UnitFileValueImpl(VALUE)(26,37)\n"
            + "        PsiElement(COMPLETED_VALUE)('SecondValue')(26,37)";
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
            + "    PsiElement(SECTION)('[One]')(0,5)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(6,16)\n"
            + "      PsiElement(KEY)('Key')(6,9)\n"
            + "      PsiElement(SEPARATOR)('=')(9,10)\n"
            + "      PsiElement(COMPLETED_VALUE)('Value\\n')(10,16)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(16,37)\n"
            + "      PsiElement(KEY)('SecondKey')(16,25)\n"
            + "      PsiElement(SEPARATOR)('=')(25,26)\n"
            + "      PsiElement(COMPLETED_VALUE)('SecondValue')(26,37)";
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

    String expectedPsiTree = "unit configuration file (systemd)(0,23)\n"
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,23)\n"
            + "    PsiElement(SECTION)('[One]')(0,5)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(6,23)\n"
            + "      PsiElement(KEY)('Key')(6,9)\n"
            + "      PsiElement(SEPARATOR)('=')(9,10)\n"
            + "      UnitFileValueImpl(VALUE)(10,23)\n"
            + "        PsiElement(CONTINUING_VALUE)('Value \\')(10,17)\n"
            + "        PsiElement(COMPLETED_VALUE)('Hello')(18,23)";
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
            + "    PsiElement(SECTION)('[One]')(0,5)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(6,24)\n"
            + "      PsiElement(KEY)('Key')(6,9)\n"
            + "      PsiElement(SEPARATOR)('=')(9,10)\n"
            + "      UnitFileValueImpl(VALUE)(10,24)\n"
            + "        PsiElement(CONTINUING_VALUE)('Val\\tue \\')(10,18)\n"
            + "        PsiElement(COMPLETED_VALUE)('Hello')(19,24)";
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
            + "    PsiElement(SECTION)('[One]')(0,5)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(6,24)\n"
            + "      PsiElement(KEY)('Key')(6,9)\n"
            + "      PsiElement(SEPARATOR)('=')(9,10)\n"
            + "      UnitFileValueImpl(VALUE)(10,24)\n"
            + "        PsiElement(CONTINUING_VALUE)('Value \\')(10,17)\n"
            + "        PsiElement(COMPLETED_VALUE)('\\tHello')(18,24)";
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
            + "    PsiElement(SECTION)('[One]')(0,5)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(6,25)\n"
            + "      PsiElement(KEY)('Key')(6,9)\n"
            + "      PsiElement(SEPARATOR)('=')(9,10)\n"
            + "      UnitFileValueImpl(VALUE)(10,25)\n"
            + "        PsiElement(CONTINUING_VALUE)('Value \\')(10,17)\n"
            + "        PsiElement(COMPLETED_VALUE)('Foo=Bar')(18,25)";
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

    String expectedPsiTree = "unit configuration file (systemd)(0,32)\n" +
            "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,32)\n" +
            "    PsiElement(SECTION)('[One]')(0,5)\n" +
            "    UnitFilePropertyImpl(PROPERTY)(6,32)\n" +
            "      PsiElement(KEY)('Key')(6,9)\n" +
            "      PsiElement(SEPARATOR)('=')(9,10)\n" +
            "      UnitFileValueImpl(VALUE)(10,32)\n" +
            "        PsiElement(CONTINUING_VALUE)('Value \\')(10,17)\n" +
            "        PsiComment(COMMENT)(';comment')(18,26)\n" +
            "        PsiElement(COMPLETED_VALUE)('Hello')(27,32)";
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
            + "    PsiElement(SECTION)('[One]')(0,5)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(6,25)\n"
            + "      PsiElement(KEY)('Key')(6,9)\n"
            + "      PsiElement(SEPARATOR)('=')(9,10)\n"
            + "      UnitFileValueImpl(VALUE)(10,25)\n"
            + "        PsiElement(CONTINUING_VALUE)('Value \\')(10,17)\n"
            + "        PsiElement(COMPLETED_VALUE)('[Hello]')(18,25)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(26,33)\n"
            + "      PsiElement(KEY)('Foo')(26,29)\n"
            + "      PsiElement(SEPARATOR)('=')(29,30)\n"
            + "      UnitFileValueImpl(VALUE)(30,33)\n"
            + "        PsiElement(COMPLETED_VALUE)('Bar')(30,33)";
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
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,15)\n"
            + "    PsiElement(SECTION)('[One]')(0,5)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(6,15)\n"
            + "      PsiElement(KEY)('Key')(6,9)\n"
            + "      PsiElement(SEPARATOR)('=')(9,10)\n"
            + "      UnitFileValueImpl(VALUE)(10,15)\n"
            + "        PsiElement(COMPLETED_VALUE)('Value')(10,15)";
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
            + "  PsiComment(COMMENT)('#Preamble')(0,9)\n"
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(10,52)\n"
            + "    PsiElement(SECTION)('[One]')(10,15)\n"
            + "    PsiComment(COMMENT)('#One')(16,20)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(21,30)\n"
            + "      PsiElement(KEY)('Key')(21,24)\n"
            + "      PsiElement(SEPARATOR)('=')(24,25)\n"
            + "      UnitFileValueImpl(VALUE)(25,30)\n"
            + "        PsiElement(COMPLETED_VALUE)('Value')(25,30)\n"
            + "    PsiComment(COMMENT)('#Two')(33,37)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(39,52)\n"
            + "      PsiElement(KEY)('Second')(39,45)\n"
            + "      PsiElement(SEPARATOR)('=')(45,46)\n"
            + "      UnitFileValueImpl(VALUE)(46,52)\n"
            + "        PsiElement(COMPLETED_VALUE)('Value2')(46,52)";
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
            + "  PsiComment(COMMENT)(';Preamble')(0,9)\n"
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(10,52)\n"
            + "    PsiElement(SECTION)('[One]')(10,15)\n"
            + "    PsiComment(COMMENT)(';One')(16,20)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(21,30)\n"
            + "      PsiElement(KEY)('Key')(21,24)\n"
            + "      PsiElement(SEPARATOR)('=')(24,25)\n"
            + "      UnitFileValueImpl(VALUE)(25,30)\n"
            + "        PsiElement(COMPLETED_VALUE)('Value')(25,30)\n"
            + "    PsiComment(COMMENT)(';Two')(33,37)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(39,52)\n"
            + "      PsiElement(KEY)('Second')(39,45)\n"
            + "      PsiElement(SEPARATOR)('=')(45,46)\n"
            + "      UnitFileValueImpl(VALUE)(46,52)\n"
            + "        PsiElement(COMPLETED_VALUE)('Value2')(46,52)";
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
            + "  PsiComment(COMMENT)(';Preamble')(0,9)\n"
            + "  PsiComment(COMMENT)('#Preable 2')(11,21)\n"
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(22,78)\n"
            + "    PsiElement(SECTION)('[One]')(22,27)\n"
            + "    PsiComment(COMMENT)('#One')(28,32)\n"
            + "    PsiComment(COMMENT)(';Two')(33,37)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(38,47)\n"
            + "      PsiElement(KEY)('Key')(38,41)\n"
            + "      PsiElement(SEPARATOR)('=')(41,42)\n"
            + "      UnitFileValueImpl(VALUE)(42,47)\n"
            + "        PsiElement(COMPLETED_VALUE)('Value')(42,47)\n"
            + "    PsiComment(COMMENT)(';Three')(50,56)\n"
            + "    PsiComment(COMMENT)('#Four')(58,63)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(65,78)\n"
            + "      PsiElement(KEY)('Second')(65,71)\n"
            + "      PsiElement(SEPARATOR)('=')(71,72)\n"
            + "      UnitFileValueImpl(VALUE)(72,78)\n"
            + "        PsiElement(COMPLETED_VALUE)('Value2')(72,78)";
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
            + "    PsiElement(SECTION)('[Unit]')(0,6)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(7,18)\n"
            + "      PsiElement(KEY)('EmptyValue')(7,17)\n"
            + "      PsiElement(SEPARATOR)('=')(17,18)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(19,30)\n"
            + "      PsiElement(KEY)('Before')(19,25)\n"
            + "      PsiElement(SEPARATOR)('=')(25,26)\n"
            + "      UnitFileValueImpl(VALUE)(26,30)\n"
            + "        PsiElement(COMPLETED_VALUE)('test')(26,30)";
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
            + "    PsiElement(SECTION)('[Unit]')(0,6)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(7,18)\n"
            + "      PsiElement(KEY)('EmptyValue')(7,17)\n"
            + "      PsiElement(SEPARATOR)('=')(17,18)";
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
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,18)\n"
            + "    PsiElement(SECTION)('[Unit]')(0,6)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(7,18)\n"
            + "      PsiElement(KEY)('EmptyValue')(7,17)\n"
            + "      PsiElement(SEPARATOR)('=')(17,18)\n";
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
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,18)\n"
            + "    PsiElement(SECTION)('[Unit]')(0,6)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(7,18)\n"
            + "      PsiElement(KEY)('EmptyValue')(7,17)\n"
            + "      PsiElement(SEPARATOR)('=')(17,18)\n"
            + "  PsiComment(COMMENT)('#Hello')(19,25)";
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
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,18)\n"
            + "    PsiElement(SECTION)('[Unit]')(0,6)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(7,18)\n"
            + "      PsiElement(KEY)('EmptyValue')(7,17)\n"
            + "      PsiElement(SEPARATOR)('=')(17,18)\n"
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(19,28)\n"
            + "    PsiElement(SECTION)('[Install]')(19,28)";
    /*
     * Exercise SUT
     */

    String parseTree = convertSourceToParseTree(sourceCode);

    /*
     * Verification
     */
    assertSameLines(expectedPsiTree, parseTree);
  }

  public void testIncompleteSectionHeaderHasNoParsingError() {
    String sourceCode = "[Uni\n";

    String expectedPsiTree = "unit configuration file (systemd)(0,5)\n"
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,4)\n"
            + "    PsiElement(SECTION)('[Uni')(0,4)";
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
            + "    PsiElement(SECTION)('[Unit]')(0,6)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(7,10)\n"
            + "      PsiElement(KEY)('Foo')(7,10)\n"
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
  
  public void testValueConcatenationWorksAccordingToSpec() {
  
    /*
     * Fixture Setup
     *
     */
    String sourceCode = "[Unit]\n"
                        + "KeyOne=ValueOne\n"
                        + "Foo=Alpha \\\n"
                        + ";Comment One\n"
                        + "#Test Eero\n"
                        + "Omega\n"
                        + "Oh=Noes";
  
    // Lines ending in a backslash are concatenated with the following non-comment line while reading and
    // the backslash is replaced by a space character.
    // https://www.freedesktop.org/software/systemd/man/systemd.syntax.html#
    String expectedValue = "Alpha  Omega";
    
    /*
     * Exercise SUT
     */
  
    PsiFile myFile = createPsiFile("a", sourceCode);
    ensureParsed(myFile);
    
    UnitFileSectionGroups sectionGroup = (UnitFileSectionGroups) myFile.getFirstChild();
    UnitFileProperty property = sectionGroup.getPropertyList().get(1);
    UnitFileValueType ufvt = property.getValue();
    
    /*
     * Verification
     */
    assertNotNull(ufvt);
    assertSameLines(expectedValue, ufvt.getValue());
  }

  
  
  public void testExampleCodeParsesSuccessfully() {


    String sourceCode = "[Section A]\n"
                        + "KeyOne=value 1\n"
                        + "KeyTwo=value 2\n"
                        + "\n"
                        + "# a comment\n"
                        + "\n"
                        + "[Section B]\n"
                        + "Setting=\"something\" \"some thing\" \".\"\n"
                        + "KeyTwo=value 2 \\\n"
                        + "       value 2 continued\n"
                        + "\n"
                        + "[Section C]\n"
                        + "KeyThree=value 2\\\n"
                        + "# this line is ignored\n"
                        + "; this line is ignored too\n"
                        + "       value 2 continued\n";

    String expectedPsiTree = "unit configuration file (systemd)(0,253)\n"
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,41)\n"
            + "    PsiElement(SECTION)('[Section A]')(0,11)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(12,26)\n"
            + "      PsiElement(KEY)('KeyOne')(12,18)\n"
            + "      PsiElement(SEPARATOR)('=')(18,19)\n"
            + "      UnitFileValueImpl(VALUE)(19,26)\n"
            + "        PsiElement(COMPLETED_VALUE)('value 1')(19,26)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(27,41)\n"
            + "      PsiElement(KEY)('KeyTwo')(27,33)\n"
            + "      PsiElement(SEPARATOR)('=')(33,34)\n"
            + "      UnitFileValueImpl(VALUE)(34,41)\n"
            + "        PsiElement(COMPLETED_VALUE)('value 2')(34,41)\n"
            + "  PsiComment(COMMENT)('# a comment')(43,54)\n"
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(56,146)\n"
            + "    PsiElement(SECTION)('[Section B]')(56,67)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(68,104)\n"
            + "      PsiElement(KEY)('Setting')(68,75)\n"
            + "      PsiElement(SEPARATOR)('=')(75,76)\n"
            + "      UnitFileValueImpl(VALUE)(76,104)\n"
            + "        PsiElement(COMPLETED_VALUE)('\"something\" \"some thing\" \".\"')(76,104)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(105,146)\n"
            + "      PsiElement(KEY)('KeyTwo')(105,111)\n"
            + "      PsiElement(SEPARATOR)('=')(111,112)\n"
            + "      UnitFileValueImpl(VALUE)(112,146)\n"
            + "        PsiElement(CONTINUING_VALUE)('value 2 \\')(112,121)\n"
            + "        PsiElement(COMPLETED_VALUE)('       value 2 continued')(122,146)\n"
            + "  UnitFileSectionGroupsImpl(SECTION_GROUPS)(148,252)\n"
            + "    PsiElement(SECTION)('[Section C]')(148,159)\n"
            + "    UnitFilePropertyImpl(PROPERTY)(160,252)\n"
            + "      PsiElement(KEY)('KeyThree')(160,168)\n"
            + "      PsiElement(SEPARATOR)('=')(168,169)\n"
            + "      UnitFileValueImpl(VALUE)(169,252)\n"
            + "        PsiElement(CONTINUING_VALUE)('value 2\\')(169,177)\n"
            + "        PsiComment(COMMENT)('# this line is ignored')(178,200)\n"
            + "        PsiComment(COMMENT)('; this line is ignored too')(201,227)\n"
            + "        PsiElement(COMPLETED_VALUE)('       value 2 continued')(228,252)";

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
    return convertSourceToParseTree(sourceCode, skipSpaces());
  }

  private String convertSourceToParseTree(String sourceCode, boolean skipSpaces) {
    PsiFile myFile = createPsiFile("a", sourceCode);
    ensureParsed(myFile);
    assertEquals(sourceCode, myFile.getText());

    return toParseTreeText(myFile, skipSpaces, includeRanges());
  }

  @Override
  protected String getTestDataPath() {
    return "testData/code_samples";
  }

  @Override
  protected boolean skipSpaces() {
    return true;
  }

  @Override
  protected boolean includeRanges() {
    return true;
  }
}
