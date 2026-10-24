package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePidFileOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Absolute and relative paths (relative ones are placed under /run), specifiers, and /var/run (rewritten, not refused).
    // language="unit file (systemd)"
    val file = """
      [Service]
      PIDFile=/run/foo.pid
      PIDFile=foo.pid
      PIDFile=nginx/nginx.pid
      PIDFile=./foo.pid
      PIDFile=/var/run/foo.pid
      PIDFile=%t/foo.pid
      PIDFile=/run/%i.pid
      PIDFile=/devices/foo.pid
      PIDFile=
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    // PidFileOptionWarning adds an informational note on every PIDFile= line; only value problems count here.
    val highlights = myFixture.doHighlighting().filter { it.description?.startsWith("PID files should be avoided") != true }

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      PIDFile=../foo.pid
      PIDFile=/run/../foo.pid
      PIDFile=/run/foo/..
      PIDFile=/dev/foo.pid
      PIDFile=/proc/self/foo.pid
      PIDFile=/sys
      PIDFile=/run/%e.pid
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    // PidFileOptionWarning adds an informational note on every PIDFile= line; only value problems count here.
    val highlights = myFixture.doHighlighting().filter { it.description?.startsWith("PID files should be avoided") != true }

    assertSize(7, highlights)
  }
}
