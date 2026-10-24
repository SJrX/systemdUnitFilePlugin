package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBusNameOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Well-known names, a unique name (accepted by sd_bus_service_name_is_valid), and a specifier.
    // language="unit file (systemd)"
    val file = """
      [Service]
      BusName=org.freedesktop.systemd1
      BusName=org.freedesktop.NetworkManager
      BusName=com.example.my-service_2
      BusName=a.b
      BusName=_foo.-bar
      BusName=:1.42
      BusName=org.example.%i
      BusName=%N.service
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
      BusName=org
      BusName=org.
      BusName=.org.freedesktop
      BusName=org..freedesktop
      BusName=org.1freedesktop
      BusName=org.free desktop
      BusName=org.freedesktop/foo
      BusName=org.example.%e
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }
}
