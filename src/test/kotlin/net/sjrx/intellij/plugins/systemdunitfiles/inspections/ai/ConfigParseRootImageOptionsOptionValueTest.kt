package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRootImageOptionsOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      RootImageOptions=root:ro,noatime home:ro,dev
      RootImageOptions=root:ro,noatime,nosuid home:ro,dev nosuid,dev
      RootImageOptions=home:ro,dev nosuid,dev,%%foo
      RootImageOptions=root:nosuid
      RootImageOptions=ro
      RootImageOptions=usr:ro xbootldr:ro esp:ro srv:ro tmp:ro var:ro swap:ro
      RootImageOptions=root-verity:ro usr-verity-sig:ro root-vty:ro usr-vsig:ro
      RootImageOptions=root::ro
      RootImageOptions=root:ro:
      RootImageOptions=rootcontext=system_u\:object_r\:etc_t\:s0
      RootImageOptions=uid=0\:1
      RootImageOptions=
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
      RootImageOptions=bogus:ro
      RootImageOptions=root:ro:nosuid
      RootImageOptions=ROOT:ro
      RootImageOptions=root2:ro
      RootImageOptions=esp-vty:ro
      RootImageOptions=ro,x\qy
      RootImageOptions=rootcontext=system_u:object_r:etc_t:s0
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(7, highlights)
  }
}
