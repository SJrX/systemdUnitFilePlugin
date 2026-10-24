package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCakeAckFilterOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [CAKE]
      AckFilter=aggressive
      AckFilter=yes
      AckFilter=no
      AckFilter=true
      AckFilter=false
      AckFilter=1
      AckFilter=0
      AckFilter=on
      AckFilter=off
      AckFilter=YES
      AckFilter=No
      AckFilter=Yes
      AckFilter=TRUE
      AckFilter=oFf
      AckFilter=tRUE
      AckFilter=N
      AckFilter=
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
      [CAKE]
      AckFilter=Aggressive
      AckFilter=maybe
      AckFilter=aggresive
      AckFilter=2
      AckFilter=filter
      AckFilter=AGGRESSIVE
      AckFilter=aggressivE
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(7, highlights)
  }

}
