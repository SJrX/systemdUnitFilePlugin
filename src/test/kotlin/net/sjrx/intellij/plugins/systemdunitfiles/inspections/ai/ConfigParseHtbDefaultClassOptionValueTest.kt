package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseHtbDefaultClassOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [HierarchyTokenBucket]
      DefaultClass=30
      DefaultClass=0x30
      DefaultClass=ff
      DefaultClass=FFFF
      DefaultClass=ffffffff
      DefaultClass=0
      DefaultClass=+a
      DefaultClass=0b1
      DefaultClass=
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [HierarchyTokenBucket]
      DefaultClass=100000000
      DefaultClass=0x100000000
      DefaultClass=0o7
      DefaultClass=g
      DefaultClass=-1
      DefaultClass=1 2
      DefaultClass=0b 1
      DefaultClass=0x 1
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }

}
