package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecRootHashSigOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      RootHashSignature=/var/lib/images/foo.roothash.p7s
      RootHashSignature=base64:MIIB
      RootHashSignature=base64:MIIBAA==
      RootHashSignature=base64:MIIBAAE=
      RootHashSignature=base64:MI IB AA==
      RootHashSignature=base64:-_-_
      RootHashSignature=base64:
      RootHashSignature=
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
      RootHashSignature=MIIBAA==
      RootHashSignature=base64:MII
      RootHashSignature=base64:MI*B
      RootHashSignature=base64:AB==
      RootHashSignature=base64:=AAA
      RootHashSignature=BASE64:MIIB
      RootHashSignature=base64:AA==AAAA
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(7, highlights)
  }
}
