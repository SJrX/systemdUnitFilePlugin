package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseNumaMaskOptionValueTest : AbstractUnitFileTest() {

  // Every value below was checked with sd-check (systemd 0f0682b773). parse_range() cuts each word at its first
  // '-', so the upper bound may be "-0" or "+1" ("0--0", "0-+1") while "-0" alone has an empty lower bound and
  // "0b-0" has the lower bound "0b" (both "Failed to parse CPU range").
  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      NUMAMask=0
      NUMAMask=all
      NUMAMask=0-3
      NUMAMask=0,2,4-7
      NUMAMask=0 1 2
      NUMAMask=0, 1,,2
      NUMAMask=0x10 010 0b11 0o7
      NUMAMask=8191
      NUMAMask="0-3"
      NUMAMask=%i
      NUMAMask=
      NUMAMask=0b+1
      NUMAMask=0-0b+1
      NUMAMask=0--0
      NUMAMask=0-+1
      NUMAMask=+1
      NUMAMask=0-0b-0
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      NUMAMask=8192
      NUMAMask=0-8192
      NUMAMask=-1
      NUMAMask=1-
      NUMAMask=all 1
      NUMAMask=ALL
      NUMAMask=foo
      NUMAMask=08
      NUMAMask=+0b1
      NUMAMask=-0
      NUMAMask=0b-0
      NUMAMask=1-2-3
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(12, highlights)
  }
}
