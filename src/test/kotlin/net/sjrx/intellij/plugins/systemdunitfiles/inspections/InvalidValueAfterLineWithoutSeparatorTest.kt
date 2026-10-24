package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

/**
 * A line that is not a section, comment or key=value must not stop validation of the lines after it (#562).
 * systemd logs "Missing '=', ignoring line." and keeps going; the plugin used to stop validating the rest of the file.
 */
class InvalidValueAfterLineWithoutSeparatorTest : AbstractUnitFileTest() {

  private fun invalidValueHighlightTexts(fileName: String, file: String): List<String> {
    setupFileInEditor(fileName, file)
    enableInspection(InvalidValueInspection::class.java)
    return myFixture.doHighlighting().filter { it.inspectionToolId == "InvalidValue" }.map { it.text }
  }

  fun testValuesAfterAFreeTextLineInTheSameSectionAreStillValidated() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      Nice=abc
      this line has no equals
      Nice=def
      """.trimIndent()

    assertEquals(listOf("abc", "def"), invalidValueHighlightTexts("file.service", file))
  }

  fun testLaterSectionsAfterAFreeTextLineAreStillValidated() {
    // The shape that hid ~300 invalid values in the kf6-syntax-highlighting test unit: free text inside an
    // extension section, followed by more sections.
    // language="unit file (systemd)"
    val file = """
      [X-Unit]
      name = value
      Some text in an extension section.
      The extension section ends with the next section header.

      [Service]
      Nice=abc
      [Unit]
      StartLimitIntervalSec=invalid
      """.trimIndent()

    assertEquals(listOf("abc", "invalid"), invalidValueHighlightTexts("file.service", file))
  }

  fun testFreeTextBeforeTheFirstSectionDoesNotStopValidation() {
    // language="unit file (systemd)"
    val file = """
      some free text here
      [Service]
      Nice=abc
      """.trimIndent()

    assertEquals(listOf("abc"), invalidValueHighlightTexts("file.service", file))
  }

  fun testValidFileIsUnaffected() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      Nice=5
      [Unit]
      StartLimitIntervalSec=10
      """.trimIndent()

    assertEquals(emptyList<String>(), invalidValueHighlightTexts("file.service", file))
  }
}
