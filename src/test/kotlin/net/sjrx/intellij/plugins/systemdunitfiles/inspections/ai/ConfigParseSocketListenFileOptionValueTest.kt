package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSocketListenFileOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Socket]
      ListenFIFO=/run/acpi_fakekey
      ListenFIFO=/run/cloud-init/share/hook-hotplug-cmd
      ListenFIFO=%t/wob.sock
      ListenFIFO=/var/run/legacy.fifo
      ListenFIFO=/run//double/./slash
      ListenFIFO=/devices/not-api-vfs
      ListenMessageQueue=/queue
      ListenMessageQueue=/%i
      ListenUSBFunction=/run/usb-ffs/adb
      ListenUSBFunction=/some/path
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // Relative paths, ".." components, API VFS paths (PATH_CHECK_NON_API_VFS) and an unknown specifier.
    // language="unit file (systemd)"
    val file = """
      [Socket]
      ListenFIFO=relative/path
      ListenFIFO=/run/../etc/fifo
      ListenFIFO=/dev/foo
      ListenFIFO=/proc
      ListenMessageQueue=queue
      ListenMessageQueue=/sys/fs/mqueue/q
      ListenUSBFunction=/dev/usb-ffs/adb
      ListenUSBFunction=%e/ffs
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }
}
