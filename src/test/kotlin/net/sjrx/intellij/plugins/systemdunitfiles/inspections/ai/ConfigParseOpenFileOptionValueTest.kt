package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseOpenFileOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // From TEST(open_file_parse) (src/test/test-open-file.c) plus escape and comma edge cases.
    // language="unit file (systemd)"
    val file = """
      [Service]
      OpenFile=/proc/1/ns/mnt:host-mount-namespace:read-only
      OpenFile=/proc/1/ns/mnt
      OpenFile=/proc/1/ns/mnt:host-mount-namespace
      OpenFile=/proc/1/ns/mnt::read-only
      OpenFile=/proc/1/ns/mnt:host-mount-namespace:append
      OpenFile=/proc/1/ns/mnt:host-mount-namespace:truncate
      OpenFile=/proc/1/ns/mnt:host-mount-namespace:graceful
      OpenFile=/proc/1/ns/mnt:host-mount-namespace:read-only,graceful
      OpenFile=/run/a\x3ab:name
      OpenFile=/run/with space/file:my name
      OpenFile=/run/x:
      OpenFile=/run/x::
      OpenFile=/run/x::,read-only,,graceful,
      OpenFile=/run/%i
      OpenFile=/run/../x
      OpenFile=
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
      OpenFile=../file.dat:file:read-only
      OpenFile=/proc/1/ns/mnt:host-mount-namespace:rw
      OpenFile=/proc/1/ns/mnt:host-mount-namespace:read-only:other
      OpenFile=relative
      OpenFile=/run/x:name:read-only append
      OpenFile=/run/x\q:name
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(6, highlights)
  }
}
