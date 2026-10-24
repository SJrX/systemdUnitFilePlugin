package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseFqCodelBoolOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [FairQueueingControlledDelay]
      ECN=yes
      ECN=no
      ECN=1
      ECN=0
      ECN=true
      ECN=false
      ECN=on
      ECN=off
      ECN=y
      ECN=n
      ECN=t
      ECN=f
      ECN=YES
      ECN=False
      ECN=On
      ECN=Yes
      ECN=TRUE
      ECN=oFf
      ECN=Y
      ECN=
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
      [FairQueueingControlledDelay]
      ECN=maybe
      ECN=2
      ECN=enable
      ECN=nope
      ECN=Maybe
      ECN=ENABLE
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(6, highlights)
  }

}
