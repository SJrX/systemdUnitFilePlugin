package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseNumaMaskOptionValueTest : AbstractUnitFileTest() {

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
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(9, highlights)
  }
}
