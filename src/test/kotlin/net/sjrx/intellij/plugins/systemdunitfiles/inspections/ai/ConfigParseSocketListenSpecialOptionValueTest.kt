package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSocketListenSpecialOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // ListenSpecial= is the one file ltype without PATH_CHECK_NON_API_VFS (systemd 8bd473ff25).
    // language="unit file (systemd)"
    val file = """
      [Socket]
      ListenSpecial=/dev/rfkill
      ListenSpecial=/proc/kmsg
      ListenSpecial=/sys/kernel/debug/foo
      ListenSpecial=/some/path
      ListenSpecial=%t/special
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Socket]
      ListenSpecial=dev/rfkill
      ListenSpecial=/dev/../rfkill
      ListenSpecial=%e
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(3, highlights)
  }
}
