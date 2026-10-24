package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExtensionImagesOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      ExtensionImages=/tmp/app0.raw
      ExtensionImages=/tmp/app-data-only.raw
      ExtensionImages=-/srv/ext.raw /srv/ext2.raw:root:ro
      ExtensionImages=/srv/ext.raw:x-systemd.relax-extension-release-check
      ExtensionImages=/srv/ext.raw:root:ro,nosuid:usr:nodev
      ExtensionImages=/srv/ext.raw:nosuid:
      ExtensionImages=%S/ext.raw
      ExtensionImages=/srv/a\:b.raw
      ExtensionImages=
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
      ExtensionImages=ext.raw
      ExtensionImages=/dev/sda1
      ExtensionImages=/proc/x.raw
      ExtensionImages=/srv/ext.raw:bogus:ro
      ExtensionImages=/srv/../ext.raw
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }
}
