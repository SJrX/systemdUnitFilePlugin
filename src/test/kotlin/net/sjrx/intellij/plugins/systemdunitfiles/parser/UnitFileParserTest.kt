package net.sjrx.intellij.plugins.systemdunitfiles.parser

import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileValueType

/**
 * Tests for parsing of systemd unit files.
 *
 *
 * Note: Unlike the tutorial (http://www.jetbrains.org/intellij/sdk/docs/tutorials/writing_tests_for_plugins/parsing_test.html) we
 * keep the source and result _in_ the test. The author feels this is more readable and easier to debug then a bunch of random
 * files lying around, even it breaks convention.
 *
 *
 * One idea might be to use a different language for this other than Java, the author played with Groovy, but didn't like the multi-line
 * support in IntelliJ. Scala has problems subtyping ParsingTestCase as protected static members can't be used. Kotlin was also tried
 * but gradle was finicky, and I guess this is something that can be changed in the future.
 *
 *
 */
class UnitFileParserTest : ParsingTestCase("", "service", UnitFileParserDefinition()) {
  fun testEmptyFileParsesFine() {
    /*
     * Fixture Setup
     */
    val sourceCode = ""
    val expectedPsiTree = """unit configuration file (systemd)(0,0)
  <empty list>"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testTwoNewLinesParsesFine() {
    // Fixture Setup
    val sourceCode = "\n\n"
    val expectedPsiTree = """unit configuration file (systemd)(0,2)
  PsiWhiteSpace('\n\n')(0,2)"""


    // Exercise SUT
    val parseTree = convertSourceToParseTree(sourceCode, false)

    // Verification
    assertSameLines(expectedPsiTree, parseTree)
  }

  fun testSingleLineCommentParsesFine() {
    // Fixture Setup
    val sourceCode = "#Hello"
    val expectedPsiTree = """unit configuration file (systemd)(0,6)
  PsiComment(COMMENT)('#Hello')(0,6)"""

    // Exercise SUT
    val parseTree = convertSourceToParseTree(sourceCode)

    // Verification
    assertSameLines(expectedPsiTree, parseTree)
  }

  fun testSingleLineCommentWithSubsequentWhitespaceParsesFine() {

    // Fixture Setup
    val sourceCode = "#Hello  \n \n \n"
    val expectedPsiTree = """unit configuration file (systemd)(0,13)
  PsiComment(COMMENT)('#Hello  ')(0,8)
  PsiWhiteSpace('\n \n \n')(8,13)"""

    // Exercise SUT
    val parseTree = convertSourceToParseTree(sourceCode, false)

    // Verification
    assertSameLines(expectedPsiTree, parseTree)
  }

  fun testSingleLineCommentWithPrecedingWhiteSpaceParsesFine() {
    // Fixture Setup
    val sourceCode = "\n \n \n#Hello  "
    val expectedPsiTree = """unit configuration file (systemd)(0,13)
  PsiWhiteSpace('\n \n \n')(0,5)
  PsiComment(COMMENT)('#Hello  ')(5,13)"""


    // Exercise SUT
    val parseTree = convertSourceToParseTree(sourceCode, false)

    // Verification
    assertSameLines(expectedPsiTree, parseTree)
  }

  fun testOneSectionAndOneKeyValuePairMatchesExpectedValue() {
    /*
     * Fixture Setup
     */
    val sourceCode = """
           [One]
           Key=Value
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,15)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,15)
    PsiElement(SECTION)('[One]')(0,5)
    UnitFilePropertyImpl(PROPERTY)(6,15)
      PsiElement(KEY)('Key')(6,9)
      PsiElement(SEPARATOR)('=')(9,10)
      UnitFileValueImpl(VALUE)(10,15)
        PsiElement(COMPLETED_VALUE)('Value')(10,15)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testOneSectionAndTwoKeyValuePairMatchesExpectedValue() {
    /*
     * Fixture Setup
     */
    val sourceCode = """
           [One]
           Key=Value
           SecondKey=SecondValue
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,37)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,37)
    PsiElement(SECTION)('[One]')(0,5)
    UnitFilePropertyImpl(PROPERTY)(6,15)
      PsiElement(KEY)('Key')(6,9)
      PsiElement(SEPARATOR)('=')(9,10)
      UnitFileValueImpl(VALUE)(10,15)
        PsiElement(COMPLETED_VALUE)('Value')(10,15)
    UnitFilePropertyImpl(PROPERTY)(16,37)
      PsiElement(KEY)('SecondKey')(16,25)
      PsiElement(SEPARATOR)('=')(25,26)
      UnitFileValueImpl(VALUE)(26,37)
        PsiElement(COMPLETED_VALUE)('SecondValue')(26,37)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  /*
   * This test won't pass presently, because we pushed the new line processing into the Lexer.
   */
  private fun ignoredTestOneSectionAndKeyValueOnSameLineResultsInAnError() {
    /*
     * Fixture Setup
     */
    val sourceCode = "[One] Key=Value"
    val expectedPsiTree = """unit configuration file (systemd)(0,37)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,37)
    PsiElement(SECTION)('[One]')(0,5)
    UnitFilePropertyImpl(PROPERTY)(6,16)
      PsiElement(KEY)('Key')(6,9)
      PsiElement(SEPARATOR)('=')(9,10)
      PsiElement(COMPLETED_VALUE)('Value\n')(10,16)
    UnitFilePropertyImpl(PROPERTY)(16,37)
      PsiElement(KEY)('SecondKey')(16,25)
      PsiElement(SEPARATOR)('=')(25,26)
      PsiElement(COMPLETED_VALUE)('SecondValue')(26,37)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testLineContinuationSimple() {
    /*
     * Fixture Setup
     */
    val sourceCode = """
           [One]
           Key=Value \
           Hello
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,23)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,23)
    PsiElement(SECTION)('[One]')(0,5)
    UnitFilePropertyImpl(PROPERTY)(6,23)
      PsiElement(KEY)('Key')(6,9)
      PsiElement(SEPARATOR)('=')(9,10)
      UnitFileValueImpl(VALUE)(10,23)
        PsiElement(CONTINUING_VALUE)('Value \')(10,17)
        PsiElement(COMPLETED_VALUE)('Hello')(18,23)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testLineContinuationWithTabInlineSimple() {
    /*
     * Fixture Setup
     */
    val sourceCode = ("[One]\n"
      + "Key=Val\tue \\\n"
      + "Hello")
    val expectedPsiTree = """unit configuration file (systemd)(0,24)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,24)
    PsiElement(SECTION)('[One]')(0,5)
    UnitFilePropertyImpl(PROPERTY)(6,24)
      PsiElement(KEY)('Key')(6,9)
      PsiElement(SEPARATOR)('=')(9,10)
      UnitFileValueImpl(VALUE)(10,24)
        PsiElement(CONTINUING_VALUE)('Val\tue \')(10,18)
        PsiElement(COMPLETED_VALUE)('Hello')(19,24)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testLineContinuationWithWhitespaceStartingNextLine() {
    /*
     * Fixture Setup
     */
    val sourceCode = ("[One]\n"
      + "Key=Value \\\n"
      + "\tHello")
    val expectedPsiTree = """unit configuration file (systemd)(0,24)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,24)
    PsiElement(SECTION)('[One]')(0,5)
    UnitFilePropertyImpl(PROPERTY)(6,24)
      PsiElement(KEY)('Key')(6,9)
      PsiElement(SEPARATOR)('=')(9,10)
      UnitFileValueImpl(VALUE)(10,24)
        PsiElement(CONTINUING_VALUE)('Value \')(10,17)
        PsiElement(COMPLETED_VALUE)('\tHello')(18,24)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testLineContinuationWithKeys() {
    /*
     * Fixture Setup
     */
    val sourceCode = """
           [One]
           Key=Value \
           Foo=Bar
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,25)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,25)
    PsiElement(SECTION)('[One]')(0,5)
    UnitFilePropertyImpl(PROPERTY)(6,25)
      PsiElement(KEY)('Key')(6,9)
      PsiElement(SEPARATOR)('=')(9,10)
      UnitFileValueImpl(VALUE)(10,25)
        PsiElement(CONTINUING_VALUE)('Value \')(10,17)
        PsiElement(COMPLETED_VALUE)('Foo=Bar')(18,25)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testLineContinuationWithComment() {
    /*
     * Fixture Setup
     */
    val sourceCode = """
           [One]
           Key=Value \
           ;comment
           Hello
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,32)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,32)
    PsiElement(SECTION)('[One]')(0,5)
    UnitFilePropertyImpl(PROPERTY)(6,32)
      PsiElement(KEY)('Key')(6,9)
      PsiElement(SEPARATOR)('=')(9,10)
      UnitFileValueImpl(VALUE)(10,32)
        PsiElement(CONTINUING_VALUE)('Value \')(10,17)
        PsiComment(COMMENT)(';comment')(18,26)
        PsiElement(COMPLETED_VALUE)('Hello')(27,32)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testLineContinuationWithSection() {
    /*
     * Fixture Setup
     */
    val sourceCode = """
           [One]
           Key=Value \
           [Hello]
           Foo=Bar
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,33)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,33)
    PsiElement(SECTION)('[One]')(0,5)
    UnitFilePropertyImpl(PROPERTY)(6,25)
      PsiElement(KEY)('Key')(6,9)
      PsiElement(SEPARATOR)('=')(9,10)
      UnitFileValueImpl(VALUE)(10,25)
        PsiElement(CONTINUING_VALUE)('Value \')(10,17)
        PsiElement(COMPLETED_VALUE)('[Hello]')(18,25)
    UnitFilePropertyImpl(PROPERTY)(26,33)
      PsiElement(KEY)('Foo')(26,29)
      PsiElement(SEPARATOR)('=')(29,30)
      UnitFileValueImpl(VALUE)(30,33)
        PsiElement(COMPLETED_VALUE)('Bar')(30,33)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testOneSectionAndOneKeyValuePairWithNewLineAtEndMatchesExpectedValue() {
    /*
     * Fixture Setup
     */
    val sourceCode = """
           [One]
           Key=Value
           
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,16)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,15)
    PsiElement(SECTION)('[One]')(0,5)
    UnitFilePropertyImpl(PROPERTY)(6,15)
      PsiElement(KEY)('Key')(6,9)
      PsiElement(SEPARATOR)('=')(9,10)
      UnitFileValueImpl(VALUE)(10,15)
        PsiElement(COMPLETED_VALUE)('Value')(10,15)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testCommentsInSectionParseSuccessfully() {
    /*
     * Fixture Setup
     */
    val sourceCode = """
           #Preamble
           [One]
           #One
           Key=Value
           
           
           #Two
           
           Second=Value2
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,52)
  PsiComment(COMMENT)('#Preamble')(0,9)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(10,52)
    PsiElement(SECTION)('[One]')(10,15)
    PsiComment(COMMENT)('#One')(16,20)
    UnitFilePropertyImpl(PROPERTY)(21,30)
      PsiElement(KEY)('Key')(21,24)
      PsiElement(SEPARATOR)('=')(24,25)
      UnitFileValueImpl(VALUE)(25,30)
        PsiElement(COMPLETED_VALUE)('Value')(25,30)
    PsiComment(COMMENT)('#Two')(33,37)
    UnitFilePropertyImpl(PROPERTY)(39,52)
      PsiElement(KEY)('Second')(39,45)
      PsiElement(SEPARATOR)('=')(45,46)
      UnitFileValueImpl(VALUE)(46,52)
        PsiElement(COMPLETED_VALUE)('Value2')(46,52)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testSemiColonCommentsInSectionParseSuccessfully() {
    /*
     * Fixture Setup
     */
    val sourceCode = """
           ;Preamble
           [One]
           ;One
           Key=Value
           
           
           ;Two
           
           Second=Value2
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,52)
  PsiComment(COMMENT)(';Preamble')(0,9)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(10,52)
    PsiElement(SECTION)('[One]')(10,15)
    PsiComment(COMMENT)(';One')(16,20)
    UnitFilePropertyImpl(PROPERTY)(21,30)
      PsiElement(KEY)('Key')(21,24)
      PsiElement(SEPARATOR)('=')(24,25)
      UnitFileValueImpl(VALUE)(25,30)
        PsiElement(COMPLETED_VALUE)('Value')(25,30)
    PsiComment(COMMENT)(';Two')(33,37)
    UnitFilePropertyImpl(PROPERTY)(39,52)
      PsiElement(KEY)('Second')(39,45)
      PsiElement(SEPARATOR)('=')(45,46)
      UnitFileValueImpl(VALUE)(46,52)
        PsiElement(COMPLETED_VALUE)('Value2')(46,52)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testMixedCommentStylesParsesSuccessfully() {
    /*
     * Fixture Setup
     */
    val sourceCode = """
           ;Preamble
           
           #Preable 2
           [One]
           #One
           ;Two
           Key=Value
           
           
           ;Three
           
           #Four
           
           Second=Value2
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,78)
  PsiComment(COMMENT)(';Preamble')(0,9)
  PsiComment(COMMENT)('#Preable 2')(11,21)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(22,78)
    PsiElement(SECTION)('[One]')(22,27)
    PsiComment(COMMENT)('#One')(28,32)
    PsiComment(COMMENT)(';Two')(33,37)
    UnitFilePropertyImpl(PROPERTY)(38,47)
      PsiElement(KEY)('Key')(38,41)
      PsiElement(SEPARATOR)('=')(41,42)
      UnitFileValueImpl(VALUE)(42,47)
        PsiElement(COMPLETED_VALUE)('Value')(42,47)
    PsiComment(COMMENT)(';Three')(50,56)
    PsiComment(COMMENT)('#Four')(58,63)
    UnitFilePropertyImpl(PROPERTY)(65,78)
      PsiElement(KEY)('Second')(65,71)
      PsiElement(SEPARATOR)('=')(71,72)
      UnitFileValueImpl(VALUE)(72,78)
        PsiElement(COMPLETED_VALUE)('Value2')(72,78)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testEmptyValueInSectionWithStuffAfterItParsesSuccessfully() {
    val sourceCode = """
           [Unit]
           EmptyValue=
           Before=test
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,30)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,30)
    PsiElement(SECTION)('[Unit]')(0,6)
    UnitFilePropertyImpl(PROPERTY)(7,18)
      PsiElement(KEY)('EmptyValue')(7,17)
      PsiElement(SEPARATOR)('=')(17,18)
    UnitFilePropertyImpl(PROPERTY)(19,30)
      PsiElement(KEY)('Before')(19,25)
      PsiElement(SEPARATOR)('=')(25,26)
      UnitFileValueImpl(VALUE)(26,30)
        PsiElement(COMPLETED_VALUE)('test')(26,30)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testEmptyValueBeforeEofParsesSuccessfully() {
    val sourceCode = """
           [Unit]
           EmptyValue=
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,18)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,18)
    PsiElement(SECTION)('[Unit]')(0,6)
    UnitFilePropertyImpl(PROPERTY)(7,18)
      PsiElement(KEY)('EmptyValue')(7,17)
      PsiElement(SEPARATOR)('=')(17,18)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testEmptyValueBeforeNewLineAndEofParsesSuccessfully() {
    val sourceCode = """
           [Unit]
           EmptyValue=
           
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,19)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,18)
    PsiElement(SECTION)('[Unit]')(0,6)
    UnitFilePropertyImpl(PROPERTY)(7,18)
      PsiElement(KEY)('EmptyValue')(7,17)
      PsiElement(SEPARATOR)('=')(17,18)
"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testEmptyValueBeforeCommentParsesSuccessfully() {
    val sourceCode = """
           [Unit]
           EmptyValue=
           #Hello
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,25)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,18)
    PsiElement(SECTION)('[Unit]')(0,6)
    UnitFilePropertyImpl(PROPERTY)(7,18)
      PsiElement(KEY)('EmptyValue')(7,17)
      PsiElement(SEPARATOR)('=')(17,18)
  PsiComment(COMMENT)('#Hello')(19,25)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testEmptyValueBeforeSectionParsesSuccessfully() {
    val sourceCode = """
           [Unit]
           EmptyValue=
           [Install]
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,28)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,18)
    PsiElement(SECTION)('[Unit]')(0,6)
    UnitFilePropertyImpl(PROPERTY)(7,18)
      PsiElement(KEY)('EmptyValue')(7,17)
      PsiElement(SEPARATOR)('=')(17,18)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(19,28)
    PsiElement(SECTION)('[Install]')(19,28)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testIncompleteSectionHeaderHasNoParsingError() {
    val sourceCode = "[Uni\n"
    val expectedPsiTree = """unit configuration file (systemd)(0,5)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,4)
    PsiElement(SECTION)('[Uni')(0,4)"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testKeyWithNoSeparatorHasError() {
    val sourceCode = """
           [Unit]
           Foo
           """.trimIndent()
    val expectedPsiTree = """unit configuration file (systemd)(0,10)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,10)
    PsiElement(SECTION)('[Unit]')(0,6)
    UnitFilePropertyImpl(PROPERTY)(7,10)
      PsiElement(KEY)('Foo')(7,10)
      PsiErrorElement:<key-value separator (=)> expected(10,10)
        <empty list>"""
    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  fun testValueConcatenationWorksAccordingToSpec() {

    /*
     * Fixture Setup
     *
     */
    val sourceCode = """
           [Unit]
           KeyOne=ValueOne
           Foo=Alpha \
           ;Comment One
           #Test Eero
           Omega
           Oh=Noes
           """.trimIndent()

    // Lines ending in a backslash are concatenated with the following non-comment line while reading and
    // the backslash is replaced by a space character.
    // https://www.freedesktop.org/software/systemd/man/systemd.syntax.html#
    val expectedValue = "Alpha  Omega"

    /*
     * Exercise SUT
     */
    val myFile = createPsiFile("a", sourceCode)
    ensureParsed(myFile)
    val sectionGroup = myFile.firstChild as UnitFileSectionGroups
    val property = sectionGroup.propertyList[1]
    val ufvt: UnitFileValueType? = property.value

    /*
     * Verification
     */TestCase.assertNotNull(ufvt)
    assertSameLines(expectedValue, ufvt!!.value)
  }

  fun testExampleCodeParsesSuccessfully() {
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
    val expectedPsiTree = """unit configuration file (systemd)(0,253)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(0,41)
    PsiElement(SECTION)('[Section A]')(0,11)
    UnitFilePropertyImpl(PROPERTY)(12,26)
      PsiElement(KEY)('KeyOne')(12,18)
      PsiElement(SEPARATOR)('=')(18,19)
      UnitFileValueImpl(VALUE)(19,26)
        PsiElement(COMPLETED_VALUE)('value 1')(19,26)
    UnitFilePropertyImpl(PROPERTY)(27,41)
      PsiElement(KEY)('KeyTwo')(27,33)
      PsiElement(SEPARATOR)('=')(33,34)
      UnitFileValueImpl(VALUE)(34,41)
        PsiElement(COMPLETED_VALUE)('value 2')(34,41)
  PsiComment(COMMENT)('# a comment')(43,54)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(56,146)
    PsiElement(SECTION)('[Section B]')(56,67)
    UnitFilePropertyImpl(PROPERTY)(68,104)
      PsiElement(KEY)('Setting')(68,75)
      PsiElement(SEPARATOR)('=')(75,76)
      UnitFileValueImpl(VALUE)(76,104)
        PsiElement(COMPLETED_VALUE)('"something" "some thing" "."')(76,104)
    UnitFilePropertyImpl(PROPERTY)(105,146)
      PsiElement(KEY)('KeyTwo')(105,111)
      PsiElement(SEPARATOR)('=')(111,112)
      UnitFileValueImpl(VALUE)(112,146)
        PsiElement(CONTINUING_VALUE)('value 2 \')(112,121)
        PsiElement(COMPLETED_VALUE)('       value 2 continued')(122,146)
  UnitFileSectionGroupsImpl(SECTION_GROUPS)(148,252)
    PsiElement(SECTION)('[Section C]')(148,159)
    UnitFilePropertyImpl(PROPERTY)(160,252)
      PsiElement(KEY)('KeyThree')(160,168)
      PsiElement(SEPARATOR)('=')(168,169)
      UnitFileValueImpl(VALUE)(169,252)
        PsiElement(CONTINUING_VALUE)('value 2\')(169,177)
        PsiComment(COMMENT)('# this line is ignored')(178,200)
        PsiComment(COMMENT)('; this line is ignored too')(201,227)
        PsiElement(COMPLETED_VALUE)('       value 2 continued')(228,252)"""

    /*
     * Exercise SUT
     */
    val parseTree = convertSourceToParseTree(sourceCode)

    /*
     * Verification
     */assertSameLines(expectedPsiTree, parseTree)
  }

  /**
   * Converts the source code supplied as an argument to a parse tree.
   *
   * @param sourceCode - the input source code to read
   * @return a string representation of the PsiTree
   */
  private fun convertSourceToParseTree(sourceCode: String, skipSpaces: Boolean = skipSpaces()): String {
    val myFile = createPsiFile("a", sourceCode)
    ensureParsed(myFile)
    TestCase.assertEquals(sourceCode, myFile.text)
    return toParseTreeText(myFile, skipSpaces, includeRanges())
  }

  override fun getTestDataPath(): String {
    return "testData/code_samples"
  }

  override fun skipSpaces(): Boolean {
    return true
  }

  override fun includeRanges(): Boolean {
    return true
  }
}
