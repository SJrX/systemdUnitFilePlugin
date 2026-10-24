package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseFdbInterfaceOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [BridgeFDB]
      OutgoingInterface=test1
      OutgoingInterface=dummy98
      OutgoingInterface=7
      OutgoingInterface=010
      OutgoingInterface=0b 1
      OutgoingInterface=0o 7
      OutgoingInterface=an-alternative-name-longer-than-fifteen
      OutgoingInterface=
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
      [BridgeFDB]
      OutgoingInterface=0
      OutgoingInterface=default
      OutgoingInterface=a%d
      OutgoingInterface=..
      OutgoingInterface=08
      OutgoingInterface=eth 0
      OutgoingInterface=0b -1
      OutgoingInterface=4294967297
      OutgoingInterface=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(9, highlights)
  }
}
